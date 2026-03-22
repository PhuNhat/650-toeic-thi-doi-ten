package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TokenRing {

    private static final String META_COORDINATOR_EPOCH = "coordinator_epoch";
    private static final String META_HIGHEST_EPOCH = "highest_seen_epoch";
    private static final String META_HIGHEST_SEQUENCE = "highest_seen_sequence";

    private final int nodeId;
    private final RoutingTable routing;
    private final Database db;
    private final AtomicLong lamportClock;
    private final Queue<ProcessData> pendingJobs = new ArrayDeque<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final long tokenPassDelayMs;
    private final long tokenMonitorIntervalMs;
    private final long tokenLossTimeoutMs;
    private final int connectTimeoutMs;

    private volatile boolean hasToken;
    private volatile long tokenEpoch;
    private volatile long tokenSequence;
    private volatile long highestSeenEpoch;
    private volatile long highestSeenSequence;
    private volatile long lastTokenActivityAt = System.currentTimeMillis();
    private volatile long recoveryCount = 0;
    private ScheduledFuture<?> scheduledPass;

    public TokenRing(int nodeId, RoutingTable routing, Database db, AtomicLong lamportClock,
                     long tokenPassDelayMs, long tokenMonitorIntervalMs, long tokenLossTimeoutMs,
                     int connectTimeoutMs) {
        this.nodeId = nodeId;
        this.routing = routing;
        this.db = db;
        this.lamportClock = lamportClock;
        this.tokenPassDelayMs = tokenPassDelayMs;
        this.tokenMonitorIntervalMs = tokenMonitorIntervalMs;
        this.tokenLossTimeoutMs = tokenLossTimeoutMs;
        this.connectTimeoutMs = connectTimeoutMs;
        this.highestSeenEpoch = db.getLongMetadata(META_HIGHEST_EPOCH, 0L);
        this.highestSeenSequence = db.getLongMetadata(META_HIGHEST_SEQUENCE, 0L);
    }

    public synchronized void start() {
        scheduler.scheduleAtFixedRate(this::checkTokenHealth,
                tokenMonitorIntervalMs, tokenMonitorIntervalMs, TimeUnit.MILLISECONDS);

        if (nodeId == 1 && db.getLongMetadata(META_COORDINATOR_EPOCH, 0L) == 0L) {
            issueNewToken("khoi dong ban dau");
        }
    }

    public synchronized String submitPrintJob(String payload) {
        long requestLamport = lamportClock.incrementAndGet();
        ProcessData request = ProcessData.parsePrintPayload(payload, nodeId, requestLamport);
        if (request == null) {
            return "LOI|Cu phap dung: PRINT|jobId|noiDungTaiLieu";
        }

        pendingJobs.add(request);
        System.out.println("[Node " + nodeId + "] Da dua job in " + request.getJobId()
                + " vao hang doi tai dong ho Lamport " + requestLamport
                + " | noi dung: " + request.getDocumentContent());

        if (hasToken) {
            processPendingJobsLocked();
            scheduleTokenPassLocked(tokenPassDelayMs);
        }

        return hasToken
                ? "OK|Da nhan job in va nut nay dang giu token"
                : "OK|Da xep job in vao hang doi, cho token den";
    }

    public synchronized String submitCancelJob(String jobId) {
        long requestLamport = lamportClock.incrementAndGet();
        ProcessData request = ProcessData.cancelRequest(jobId, nodeId, requestLamport);
        if (request == null) {
            return "LOI|Cu phap dung: CANCEL|jobId";
        }

        pendingJobs.add(request);
        System.out.println("[Node " + nodeId + "] Da dua yeu cau huy job " + request.getJobId()
                + " vao hang doi tai dong ho Lamport " + requestLamport);

        if (hasToken) {
            processPendingJobsLocked();
            scheduleTokenPassLocked(tokenPassDelayMs);
        }

        return hasToken
                ? "OK|Da nhan yeu cau huy va nut nay dang giu token"
                : "OK|Da xep yeu cau huy vao hang doi, cho token den";
    }

    public synchronized boolean receiveToken(int fromNode, long fromLamport, long epoch, long sequence) {
        if (!isNewerToken(epoch, sequence)) {
            System.out.println("[Node " + nodeId + "] Bo qua token cu hoac trung lap epoch=" + epoch
                    + " sequence=" + sequence);
            return false;
        }

        lamportClock.set(Math.max(lamportClock.get(), fromLamport) + 1);
        highestSeenEpoch = epoch;
        highestSeenSequence = sequence;
        tokenEpoch = epoch;
        tokenSequence = sequence;
        hasToken = true;
        lastTokenActivityAt = System.currentTimeMillis();
        persistHighestTokenState();

        System.out.println("[Node " + nodeId + "] Da nhan token tu Node " + fromNode
                + " | epoch=" + epoch + " | sequence=" + sequence
                + " | lamport=" + lamportClock.get());

        processPendingJobsLocked();
        scheduleTokenPassLocked(tokenPassDelayMs);
        return true;
    }

    public String getJobLog() {
        return db.getAllJobs();
    }

    public synchronized String getStatus() {
        return "STATUS|node=" + nodeId
                + "|hasToken=" + hasToken
                + "|pendingJobs=" + pendingJobs.size()
                + "|lamport=" + lamportClock.get()
                + "|tokenEpoch=" + tokenEpoch
                + "|tokenSequence=" + tokenSequence
                + "|highestSeenEpoch=" + highestSeenEpoch
                + "|highestSeenSequence=" + highestSeenSequence
                + "|recoveries=" + recoveryCount
                + "|ringSize=" + routing.size();
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    private synchronized void processPendingJobsLocked() {
        while (hasToken && !pendingJobs.isEmpty()) {
            ProcessData request = pendingJobs.peek();
            if (request == null) {
                continue;
            }

            long processedLamport = lamportClock.incrementAndGet();
            long processedAt = System.currentTimeMillis();
            boolean success;

            if (request.getOperation() == ProcessData.Operation.PRINT) {
                System.out.println("[Node " + nodeId + "] Dang in job " + request.getJobId()
                        + " | noi dung: " + request.getDocumentContent()
                        + " | lamportGui=" + request.getSubmittedLamport()
                        + " | lamportXuLy=" + processedLamport);
                success = db.recordPrintedJob(request, nodeId, processedLamport, processedAt);
            } else {
                System.out.println("[Node " + nodeId + "] Dang huy job " + request.getJobId()
                        + " | lamportGui=" + request.getSubmittedLamport()
                        + " | lamportXuLy=" + processedLamport);
                success = db.recordCancelledJob(request, nodeId, processedLamport, processedAt);
            }

            if (success) {
                pendingJobs.poll();
            } else {
                System.out.println("[Node " + nodeId + "] CSDL chua san sang, giu nguyen yeu cau trong hang doi");
                break;
            }
        }
    }

    private synchronized void scheduleTokenPassLocked(long delayMs) {
        if (!hasToken) {
            return;
        }

        if (scheduledPass != null && !scheduledPass.isDone()) {
            scheduledPass.cancel(false);
        }

        scheduledPass = scheduler.schedule(this::passTokenSafely, delayMs, TimeUnit.MILLISECONDS);
    }

    private void passTokenSafely() {
        synchronized (this) {
            if (!hasToken) {
                return;
            }

            if (routing.size() <= 1) {
                System.out.println("[Node " + nodeId + "] He thong chi co 1 nut, token duoc giu tai cho");
                processPendingJobsLocked();
                lastTokenActivityAt = System.currentTimeMillis();
                return;
            }

            long nextSequence = tokenSequence + 1;
            int totalNodes = routing.size();
            int nextNodeId = nodeId;

            for (int attempt = 1; attempt < totalNodes; attempt++) {
                nextNodeId = (nextNodeId % totalNodes) + 1;
                VirtualCircle nextNode = routing.getPeer(nextNodeId);
                if (trySendToken(nextNodeId, nextNode, tokenEpoch, nextSequence)) {
                    tokenSequence = nextSequence;
                    highestSeenEpoch = tokenEpoch;
                    highestSeenSequence = nextSequence;
                    persistHighestTokenState();
                    hasToken = false;
                    lastTokenActivityAt = System.currentTimeMillis();
                    return;
                }
            }

            System.out.println("[Node " + nodeId + "] Chua chuyen duoc token, se giu lai va thu lai");
            scheduleTokenPassLocked(Math.max(tokenPassDelayMs, 2000L));
        }
    }

    private boolean trySendToken(int targetNodeId, VirtualCircle targetNode, long epoch, long sequence) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(targetNode.getDestination(), targetNode.getPort()), connectTimeoutMs);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            long currentLamport = lamportClock.incrementAndGet();
            out.println("TOKEN|" + nodeId + "|" + currentLamport + "|" + epoch + "|" + sequence);
            System.out.println("[Node " + nodeId + "] Da chuyen token sang Node " + targetNodeId
                    + " | epoch=" + epoch + " | sequence=" + sequence);
            return true;
        } catch (IOException ex) {
            System.out.println("[Node " + nodeId + "] Khong the chuyen token sang Node " + targetNodeId
                    + ": " + ex.getMessage());
            return false;
        }
    }

    private void checkTokenHealth() {
        synchronized (this) {
            if (hasToken) {
                return;
            }

            long idleFor = System.currentTimeMillis() - lastTokenActivityAt;
            if (idleFor < tokenLossTimeoutMs) {
                return;
            }

            if (nodeId == 1) {
                System.out.println("[Node 1] Bo giam sat phat hien token bi mat sau " + idleFor + " ms");
                issueNewToken("phuc hoi boi watchdog");
            }
        }
    }

    private void issueNewToken(String reason) {
        long nextEpoch = Math.max(db.getLongMetadata(META_COORDINATOR_EPOCH, 0L), highestSeenEpoch) + 1;
        long currentLamport = lamportClock.incrementAndGet();

        db.putLongMetadata(META_COORDINATOR_EPOCH, nextEpoch);
        tokenEpoch = nextEpoch;
        tokenSequence = 1L;
        highestSeenEpoch = nextEpoch;
        highestSeenSequence = 1L;
        hasToken = true;
        if (!"initial startup".equals(reason)) {
            recoveryCount++;
        }
        lastTokenActivityAt = System.currentTimeMillis();
        persistHighestTokenState();

        System.out.println("[Node " + nodeId + "] Da tao token moi | ly do=" + reason
                + " | epoch=" + tokenEpoch + " | sequence=" + tokenSequence
                + " | lamport=" + currentLamport);

        processPendingJobsLocked();
        scheduleTokenPassLocked(tokenPassDelayMs);
    }

    private boolean isNewerToken(long epoch, long sequence) {
        return epoch > highestSeenEpoch || (epoch == highestSeenEpoch && sequence > highestSeenSequence);
    }

    private void persistHighestTokenState() {
        db.putLongMetadata(META_HIGHEST_EPOCH, highestSeenEpoch);
        db.putLongMetadata(META_HIGHEST_SEQUENCE, highestSeenSequence);
    }
}

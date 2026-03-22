package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeHandler implements Runnable {

    private final Socket clientSocket;
    private final int nodeId;
    private final TokenRing tokenRing;

    public NodeHandler(Socket socket, int nodeId, TokenRing tokenRing) {
        this.clientSocket = socket;
        this.nodeId = nodeId;
        this.tokenRing = tokenRing;
    }

    @Override
    public void run() {
        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String message = in.readLine();
            if (message == null || message.trim().isEmpty()) {
                out.println("LOI|Yeu cau rong");
                return;
            }

            System.out.println("[Node " + nodeId + "] Nhan duoc: " + message);

            if (message.startsWith("TOKEN|")) {
                handleTokenMessage(message, out);
            } else if ("QUERY".equalsIgnoreCase(message)) {
                String data = tokenRing.getJobLog();
                out.print(data);
                if (!data.endsWith("\n")) {
                    out.print("\n");
                }
                out.flush();
            } else if ("STATUS".equalsIgnoreCase(message) || "PING".equalsIgnoreCase(message)) {
                out.println(tokenRing.getStatus());
            } else if (message.startsWith("PRINT|") || message.startsWith("INSERT|")) {
                logPrintRequest(message);
                out.println(tokenRing.submitPrintJob(message.substring(message.indexOf('|') + 1)));
            } else if (message.startsWith("CANCEL|") || message.startsWith("DELETE|")) {
                out.println(tokenRing.submitCancelJob(message.substring(message.indexOf('|') + 1)));
            } else {
                out.println("LOI|Lenh khong hop le");
            }
        } catch (IOException e) {
            System.out.println("[Node " + nodeId + "] Loi xu ly ket noi: " + e.getMessage());
        }
    }

    private void handleTokenMessage(String message, PrintWriter out) {
        String[] parts = message.split("\\|", 6);
        if (parts.length < 5) {
            out.println("LOI|Goi token khong hop le");
            return;
        }

        try {
            int fromNode = Integer.parseInt(parts[1]);
            long fromLamport = Long.parseLong(parts[2]);
            long epoch = Long.parseLong(parts[3]);
            long sequence = Long.parseLong(parts[4]);

            boolean accepted = tokenRing.receiveToken(fromNode, fromLamport, epoch, sequence);
            out.println(accepted ? "DA_NHAN_TOKEN" : "BO_QUA_TOKEN");
        } catch (NumberFormatException ex) {
            out.println("LOI|Metadata token khong hop le");
        }
    }

    private void logPrintRequest(String message) {
        String payload = message.substring(message.indexOf('|') + 1);
        String[] parts = payload.split("\\|", 2);
        if (parts.length >= 2) {
            System.out.println("[Node " + nodeId + "] Nhan lenh in job " + parts[0].trim()
                    + " voi noi dung: " + parts[1].trim());
        }
    }
}

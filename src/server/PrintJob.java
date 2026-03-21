package server;

public class PrintJob {

    String id, content, priority, status, timestamp;

    public PrintJob(String message) {
        String[] parts = message.split("\\|");
        if (parts.length >= 5) {
            this.id = parts[0];
            this.content = parts[1];
            this.priority = parts[2];
            this.status = parts[3];
            this.timestamp = parts[4];
        } else {
            this.id = "default";
            this.content = message;
            this.priority = "normal";
            this.status = "pending";
            this.timestamp = String.valueOf(System.currentTimeMillis());
        }
    }

    public String getId() { return id; }
    public String getContent() { return content; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return id + "|" + content + "|" + priority + "|" + status + "|" + timestamp;
    }
}
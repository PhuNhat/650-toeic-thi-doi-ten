package server;

public class RoutingTable {

    public VirtualCircle table[];
    public int max = 6;

    public RoutingTable(String peers) {
        table = new VirtualCircle[6];
        
        // Handle empty, null, or placeholder PEERS
        if (peers == null || peers.trim().isEmpty() || peers.contains("(will be set by PM later)")) {
            System.out.println("[WARNING] PEERS not set - using localhost defaults");
            peers = "localhost:8080,localhost:8080,localhost:8080,localhost:8080,localhost:8080,localhost:8080";
        }
        
        String[] peerList = peers.split(",");
        
        // Check if we have exactly 6 peers
        if (peerList.length != 6) {
            System.out.println("[ERROR] Expected 6 peers, got " + peerList.length);
            System.out.println("[ERROR] PEERS input: " + peers);
            throw new IllegalArgumentException("PEERS must contain exactly 6 comma-separated values (hostname:port)");
        }
        
        for (int i = 0; i < 6; i++) {
            String[] parts = peerList[i].trim().split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid peer format at index " + i + ": " + peerList[i] + " (expected hostname:port)");
            }
            table[i] = new VirtualCircle(parts[0].trim(), Integer.parseInt(parts[1].trim()), "Server" + (i+1));
        }
        max = 6;
    }
}
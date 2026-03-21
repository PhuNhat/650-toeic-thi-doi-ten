package server;

import java.io.*;
import java.net.Socket;

public class PrintHandler implements Runnable {

    private Socket clientSocket;

    public PrintHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String jobMessage = in.readLine();
            System.out.println("Print Server received job: " + jobMessage);

            // Process job (simulate printing)
            PrintJob job = new PrintJob(jobMessage);
            System.out.println("Printing job: " + job.getContent());
            // Here you can add actual printing logic, e.g., write to file or send to printer

            out.println("Job printed: " + job.getId());

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
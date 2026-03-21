package client;

import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;

public class WebClient {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Web client started on port 8080");
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<html><body><h1>Distributed System Web Client</h1><form method='POST' action='/submit'>"
                    + "Position: <input name='pos'><br>"
                    + "License: <input name='lic'><br>"
                    + "Type: <input name='type'><br>"
                    + "Color: <input name='color'><br>"
                    + "Time: <input name='time'><br>"
                    + "Action: <select name='act'><option value='I'>Insert</option><option value='D'>Delete</option></select><br>"
                    + "<input type='submit'></form></body></html>";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
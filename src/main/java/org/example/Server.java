package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final int maxThreads;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public Server(int port, int maxThreads) throws IOException {
        this.port = port;
        this.maxThreads = maxThreads;
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newFixedThreadPool(maxThreads);
    }

    public void start() {
        System.out.println("Server listening on port " + port);
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ConnectionHandler(clientSocket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
        threadPool.shutdown();
    }

    public static void main(String[] args) {
        int port = 8080;
        int maxThreads = 64;
        try {
            Server server = new Server(port, maxThreads);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ConnectionHandler implements Runnable {
    private final Socket clientSocket;

    public ConnectionHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Handling connection from " + clientSocket.getInetAddress());
        // Handle connection logic here
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

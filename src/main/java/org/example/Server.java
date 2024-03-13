package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private static final int PORT = 9999;
    private static final int THREAD_POOL_SIZE = 64;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) {
        try (final var serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try {
                    final var socket = serverSocket.accept();
                    executorService.submit(() -> handleConnection(socket));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleConnection(Socket socket) {
        try (
            socket;
            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                sendErrorResponse(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                handleClassicHtml(filePath, out, mimeType);
            } else {
                handleRegularFile(filePath, out, mimeType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendErrorResponse(OutputStream out) throws IOException {
        out.write((
            "HTTP/1.1 404 Not Found\r\n" +
            "Content-Length: 0\r\n" +
            "Connection: close\r\n" +
            "\r\n"
        ).getBytes());
        out.flush();
    }

    private static void handleClassicHtml(Path filePath, OutputStream out, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
            "{time}",
            LocalDateTime.now().toString()
        ).getBytes();
        sendResponse(out, mimeType, content);
    }

    private static void handleRegularFile(Path filePath, OutputStream out, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        sendResponse(out, mimeType, Files.readAllBytes(filePath), length);
    }

    private static void sendResponse(OutputStream out, String mimeType, byte[] content, long contentLength) throws IOException {
        out.write((
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: " + mimeType + "\r\n" +
            "Content-Length: " + contentLength + "\r\n" +
            "Connection: close\r\n" +
            "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }
}

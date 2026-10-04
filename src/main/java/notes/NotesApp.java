package notes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NotesApp {
    private final Map<Integer, String> notes = new ConcurrentSkipListMap<>();
    private final AtomicInteger seq = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        int port = parsePort(System.getenv("PORT"));
        new NotesApp().start(port);
        System.out.println("Listening on port " + port);
    }

    static int parsePort(String value) {
        try {
            return (value == null || value.isBlank()) ? 8080 : Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 8080;
        }
    }

    public HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/healthz", ex -> send(ex, 200, "text/plain", "ok"));
        server.createContext("/notes", this::handleNotes);
        server.createContext("/", ex -> {
            if (ex.getRequestURI().getPath().equals("/")) {
                send(ex, 200, "text/plain", "Notes API: GET/POST /notes, GET /healthz");
            } else {
                send(ex, 404, "text/plain", "not found");
            }
        });
        server.start();
        return server;
    }

    private void handleNotes(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equals("GET")) {
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Map.Entry<Integer, String> e : notes.entrySet()) {
                if (!first) sb.append(",");
                sb.append(json(e.getKey(), e.getValue()));
                first = false;
            }
            sb.append("]");
            send(ex, 200, "application/json", sb.toString());
        } else if (method.equals("POST")) {
            String text = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (text.isEmpty()) {
                send(ex, 400, "text/plain", "empty note");
                return;
            }
            int id = seq.incrementAndGet();
            notes.put(id, text);
            send(ex, 201, "application/json", json(id, text));
        } else {
            send(ex, 405, "text/plain", "method not allowed");
        }
    }

    private static String json(int id, String text) {
        String esc = text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return "{\"id\":" + id + ",\"text\":\"" + esc + "\"}";
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
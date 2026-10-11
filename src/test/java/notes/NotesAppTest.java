package notes;

import com.sun.net.httpserver.HttpServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NotesAppTest {
    static int total = 0, passed = 0;
    static HttpClient client = HttpClient.newHttpClient();
    static String base;

    static HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path))
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    interface Check { boolean run() throws Exception; }

    static void test(String name, Check c) {
        total++;
        try {
            if (c.run()) { passed++; System.err.println("ok   - " + name); }
            else System.err.println("FAIL - " + name);
        } catch (Exception e) {
            System.err.println("FAIL - " + name + ": " + e);
        }
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = new NotesApp().start(0); // 0 = случайный свободный порт
        base = "http://localhost:" + server.getAddress().getPort();

        test("GET / returns 200", () -> get("/").statusCode() == 200);
        test("GET /healthz returns 200 ok", () -> {
            var r = get("/healthz");
            return r.statusCode() == 200 && r.body().equals("ok");
        });
        test("POST /notes creates a note", () -> {
            var r = post("/notes", "buy milk");
            return r.statusCode() == 201 && r.body().contains("buy milk");
        });
        test("GET /notes lists created note", () ->
                get("/notes").body().contains("buy milk"));
        test("POST empty note returns 400", () -> post("/notes", "  ").statusCode() == 400);
        test("unknown path returns 404", () -> get("/nope").statusCode() == 404);
        test("PORT parsing falls back to 8080", () ->
                NotesApp.parsePort(null) == 8080 && NotesApp.parsePort("abc") == 8080
                        && NotesApp.parsePort("9123") == 9123);

        server.stop(0);
        System.out.println("TESTS: " + passed + "/" + total);
        System.exit(passed == total ? 0 : 1);
    }
}
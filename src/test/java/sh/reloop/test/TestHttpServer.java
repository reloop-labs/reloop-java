package sh.reloop.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import sh.reloop.ReloopClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** JDK HttpServer helper for SDK route tests. */
public final class TestHttpServer implements AutoCloseable {
    public static final class RecordedRequest {
        public final String method;
        public final String path;
        public final String query;
        public final String apiKey;
        public final String body;

        public RecordedRequest(String method, String path, String query, String apiKey, String body) {
            this.method = method;
            this.path = path;
            this.query = query;
            this.apiKey = apiKey;
            this.body = body;
        }
    }

    private final HttpServer server;
    private final List<RecordedRequest> requests = new ArrayList<>();
    private final AtomicInteger hitCount = new AtomicInteger();
    private volatile int statusCode = 200;
    private volatile String responseBody = "{}";
    private volatile Consumer<HttpExchange> handlerOverride;

    public TestHttpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    private void handle(HttpExchange exchange) throws IOException {
        hitCount.incrementAndGet();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        synchronized (requests) {
            requests.add(new RecordedRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI().getPath(),
                    exchange.getRequestURI().getRawQuery(),
                    exchange.getRequestHeaders().getFirst("x-api-key"),
                    body));
        }
        if (handlerOverride != null) {
            handlerOverride.accept(exchange);
            return;
        }
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    public ReloopClient client() {
        return new ReloopClient("rl_test", baseUrl());
    }

    public void respond(int status, String json) {
        this.statusCode = status;
        this.responseBody = json;
    }

    public int hits() {
        return hitCount.get();
    }

    public RecordedRequest last() {
        synchronized (requests) {
            if (requests.isEmpty()) {
                return null;
            }
            return requests.get(requests.size() - 1);
        }
    }

    public List<RecordedRequest> all() {
        synchronized (requests) {
            return List.copyOf(requests);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}

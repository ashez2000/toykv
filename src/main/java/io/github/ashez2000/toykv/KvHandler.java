package io.github.ashez2000.toykv;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.ashez2000.toykv.storage.Store;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class KvHandler implements HttpHandler {

    private final HttpClient http = HttpClient.newHttpClient();

    private final Store store = new Store();
    private final String[] peers;

    public KvHandler(String[] peers) {
        this.peers = peers;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        boolean isReplicated = "true".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-Replicated"));

        String[] tokens = body.split(" ");
        String cmd = tokens[0];
        String response = "";

        switch (cmd.toUpperCase()) {
            case "GET" -> {
                String key = tokens[1];
                response = store.get(key).orElse("");
            }
            case "SET" -> {
                String key = tokens[1];
                String value = tokens[2];
                store.set(key, value);

                if (!isReplicated) {
                    for (String peer : peers) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(peer))
                                .header("X-Replicated", "true")
                                .POST(HttpRequest.BodyPublishers.ofString(body))
                                .build();
                        try {
                            http.send(request, HttpResponse.BodyHandlers.ofString());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                response = "OK";
            }
            case "DEL" -> {
                String key = tokens[1];
                boolean ok = store.del(key);

                if (!isReplicated) {
                    for (String peer : peers) {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(peer))
                                .header("X-Replicated", "true")
                                .POST(HttpRequest.BodyPublishers.ofString(body))
                                .build();
                        try {
                            http.send(request, HttpResponse.BodyHandlers.ofString());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                response = ok ? "TRUE" : "FALSE";
            }
            case "PING" -> {
                response = "PONG";
            }
            default -> {
                response = "ERROR: " + cmd + " not found";
            }
        }

        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}

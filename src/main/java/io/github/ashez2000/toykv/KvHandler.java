package io.github.ashez2000.toykv;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.ashez2000.toykv.storage.Store;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class KvHandler implements HttpHandler {

    private final Store store = new Store();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

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
                response = "OK";
            }
            case "DEL" -> {
                String key = tokens[1];
                boolean ok = store.del(key);
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

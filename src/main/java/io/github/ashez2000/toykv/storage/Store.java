package io.github.ashez2000.toykv.storage;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Store {

    private final Map<String, Item> entries = new HashMap<>();

    public Optional<String> get(String key) {
        Item item = entries.get(key);
        if (item == null) {
            return Optional.empty();
        }

        if (item.isExpired()) {
            entries.remove(key);
            return Optional.empty();
        }

        return Optional.of(item.toString());
    }

    public void set(String key, String value) {
        Instant expiresAt = Instant.now().plus(Duration.ofSeconds(30));
        entries.put(key, new Item(value, expiresAt));
    }

    public boolean del(String key) {
        return entries.remove(key) != null;
    }

    public record Item(String value, Instant expiresAt) {
        public boolean isExpired() {
            return expiresAt.isBefore(Instant.now());
        }
    }

}

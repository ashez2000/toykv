package io.github.ashez2000.toykv.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Store {

    private final Map<String, String> entries = new HashMap<>();

    public Optional<String> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    public void set(String key, String value) {
        entries.put(key, value);
    }

    public boolean del(String key) {
        return entries.remove(key) != null;
    }

}

package it.loneliness.mc.automatihelper.Model;

import java.util.LinkedHashMap;
import java.util.Map;

public class FIFOCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public FIFOCache(int capacity) {
        super(capacity, 0.75f, false);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
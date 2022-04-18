package xyz.destiall.caramel.app.utils;

import java.util.Map;

public class Pair<K,V> implements Map.Entry<K, V> {
    private K key;
    private V value;
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public V setValue(V value) {
        V previous = this.value;
        this.value = value;
        return previous;
    }
}

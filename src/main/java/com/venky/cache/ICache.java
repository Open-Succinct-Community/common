package com.venky.cache;

public interface ICache<K,V> {
    public V get(K key);
}

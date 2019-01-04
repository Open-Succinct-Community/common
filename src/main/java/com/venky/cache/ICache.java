package com.venky.cache;

public interface ICache<V> {
    public V get(Object key);
}

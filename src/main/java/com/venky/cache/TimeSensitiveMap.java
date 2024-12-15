package com.venky.cache;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeSensitiveMap<K,V> implements Serializable {
    Map<K,Entry<V>> cache = new HashMap<>();
    Duration ttl ;
    public TimeSensitiveMap(Duration ttl){
        this.ttl = ttl;
    }

    public Duration getTtl() {
        return ttl;
    }

    @SuppressWarnings("all")
    public V put(K key, V value) {
        Entry<V> entry = new Entry<V>();
        entry.expiry = System.currentTimeMillis() + ttl.toMillis();
        entry.value = value;
        Entry oldValue = null;
        if (value == null){
            oldValue = cache.remove(key);
        }else {
            oldValue = cache.put(key, entry);
        }
        return oldValue == null ? null : (V)oldValue.value;
    }

    public void clear(){
        cache.clear();
    }


    @SuppressWarnings("all")
    public V get(K key){
        V value;
        long now = System.currentTimeMillis();

        Entry<V> entry = cache.get(key);
        if (entry != null){
            if (now > entry.expiry){
                cache.remove(key);
                entry = null;
            }
        }

        return  (entry == null)? null : (V)entry.value;
    }

    public V remove(K k){
        V value = null;
        Entry<V> entry = cache.remove(k);
        if (entry != null) {
            long now = System.currentTimeMillis();
            if (now < entry.expiry) {
                value = entry.value;
            }
        }
        return value;
    }

    public void gc(){
        List<K> keys = new ArrayList<>(cache.keySet());
        for (K k : keys){
            get(k); // Just clear expired keys
        }
    }
    public static class Entry<V> {
        V value;
        long   expiry;

    }
}

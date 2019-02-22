package com.venky.cache;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class UnboundedCache<K,V> extends ConcurrentHashMap<K, V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -279156550678934353L;
	@SuppressWarnings("unchecked")
	public V get(Object key){
		K k = (K)key;
		V v = super.get(k);
		if (v == null){
			v = getValue(k); //Wastful getValue is better than blocking GetValue.
			if (v != null){
				synchronized (this){
					V vExisting = super.get(k);
					if (vExisting == null){
						super.put(k, v);
					}else {
						v = vExisting;
					}
				}
			}
		}
		return v;
	}
	
	protected abstract V getValue(K key);
}

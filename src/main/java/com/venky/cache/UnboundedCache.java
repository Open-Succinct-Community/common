package com.venky.cache;

import java.util.HashMap;

public abstract class UnboundedCache<K,V> extends HashMap<K, V>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -279156550678934353L;
	@SuppressWarnings("unchecked")
	public V get(Object key){
		K k = (K)key;
		V v = super.get(k);
		if (v == null && !containsKey(k)){
			synchronized (this) {
				v = super.get(k);
				if (v == null && !containsKey(k)){
					v = getValue(k);
					super.put(k, v);
				}
			}
		}
		return v;
	}
	
	protected abstract V getValue(K key);
}

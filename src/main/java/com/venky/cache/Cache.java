package com.venky.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.venky.core.checkpoint.Mergeable;
import com.venky.core.util.Bucket;
import com.venky.core.util.ObjectUtil;

public abstract class Cache<K,V> implements Mergeable<Cache<K,V>> , Serializable ,Map<K,V>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4801418262910565684L;
	public static final int MAX_ENTRIES_DEFAULT = 1000;
	public static final int MAX_ENTRIES_UNLIMITED = 0;
	public static final double PRUNE_FACTOR_DEFAULT = 0.8;
	private Bucket fakeTime = new Bucket(); 
			
	protected Cache(){
		this(MAX_ENTRIES_DEFAULT,PRUNE_FACTOR_DEFAULT);
	}
	
	private final int maxEntries ;
	private final double pruneFactor ;
	protected Cache(int maxEntries,double pruneFactor){
		this.maxEntries = maxEntries;
		this.pruneFactor = pruneFactor;
		if (this.pruneFactor > 1 || this.pruneFactor < 0 ){
			throw new IllegalArgumentException("Prune factor must be between 0.0 than 1.0");
		}
	}

	public void makeSpace(){
		if (maxEntries != MAX_ENTRIES_UNLIMITED && size() >= maxEntries){
			int numEntriesToRemove = (int)(size() * pruneFactor);
			if (numEntriesToRemove <= 0){
				return;
			}
			if (pruneFactor == 1){
				clear();
				return;
			}
			SortedMap<Long,List<K>> keysAccessedByTime = new TreeMap<Long, List<K>>(); 
			for (K key : accessTimeMap.keySet()){
				Long time = accessTimeMap.get(key);
				List<K> keys = keysAccessedByTime.get(time);
				if (keys == null){
					keys = new ArrayList<K>();
					keysAccessedByTime.put(time, keys);
				}
				keys.add(key);
			}
			int numEntriesRemoved = 0;
			for (Long time: keysAccessedByTime.keySet()){//We will read in the order of being Accessed.
				for (K key : keysAccessedByTime.get(time)){
					remove(key);
					numEntriesRemoved ++;
				}
				if (numEntriesRemoved >= numEntriesToRemove){
					break;
				}
			}
 		}
	}

	@SuppressWarnings("unchecked")
	public Cache<K,V> clone(){
		try {
			Cache<K,V> clone = (Cache<K,V>)super.clone();
			clone.accessTimeMap = (HashMap<K, Long>)accessTimeMap.clone();
			clone.cacheMap = (HashMap<K, V>)cacheMap.clone();
			for (K k :clone.cacheMap.keySet()){
				clone.cacheMap.put(k, ObjectUtil.clone(clone.get(k)));
			}
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void merge(Cache<K,V> another){
		ObjectUtil.mergeValues(another.accessTimeMap,this.accessTimeMap);
		ObjectUtil.mergeValues(another.cacheMap,this.cacheMap);
	}
	public int size(){
		return cacheMap.size();
	}
	
	public Set<K> keySet(){
		return Collections.unmodifiableSet(cacheMap.keySet());
	}
	
	@Override
	public boolean containsKey(Object key){
		return cacheMap.containsKey(key);
	}
	public boolean isEmpty() {
		return cacheMap.isEmpty();
	}
	public boolean containsValue(Object value) {
		return cacheMap.containsValue(value);
	}
	
	private HashMap<K,V> cacheMap = new HashMap<K, V>();
	private HashMap<K,Long> accessTimeMap = new HashMap<K, Long>();
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key){
		V v = cacheMap.get(key);
		if (v == null && !cacheMap.containsKey(key)){
			synchronized (cacheMap) {
				v = cacheMap.get(key);
				if (v == null && !cacheMap.containsKey(key)){
					v = getValue((K)key);
					makeSpace();
					cacheMap.put((K)key, v);
				}
			}
		}
		fakeTime.increment();
		accessTimeMap.put((K)key, fakeTime.longValue());
		return v;
	}
	
	
	public V remove(Object key){
		V previous = null;
		synchronized (cacheMap) {
			previous = cacheMap.remove(key);
			accessTimeMap.remove(key);
		}
		return previous;
	}
	public void clear(){
		synchronized (cacheMap) {
			cacheMap.clear();
			accessTimeMap.clear();
		}
	}
	
	public V put(K key,V value){
		V previous = null;
		synchronized (cacheMap) {
			previous = cacheMap.put(key, value);
			fakeTime.increment();
			accessTimeMap.put(key, fakeTime.longValue());
		}
		return previous;
	}
	
	protected abstract V getValue(K k);
	public Long accessTime(K k){
		return accessTimeMap.get(k);
	}
	
	public Collection<V> values(){
		return cacheMap.values();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<K,V> e: entrySet()){ 
			put(e.getKey(),e.getValue());
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(cacheMap.entrySet());
	}
}

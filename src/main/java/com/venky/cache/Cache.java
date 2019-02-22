package com.venky.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.venky.core.checkpoint.Mergeable;
import com.venky.core.math.DoubleUtils;
import com.venky.core.util.Bucket;
import com.venky.core.util.ObjectUtil;

public abstract class Cache<K,V> implements ICache<V> , Mergeable<Cache<K,V>> , Serializable ,Map<K,V>{
	
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
	
	private int maxEntries ;
	private double pruneFactor ;
	private int MIN_ENTRIES_TO_EVICT ;
	public void reconfigure(int maxEntries,double pruneFactor) {
		synchronized (this){
			this.maxEntries = maxEntries;
			this.pruneFactor = pruneFactor;
			if (this.pruneFactor > 1 || this.pruneFactor < 0 ){
				throw new IllegalArgumentException("Prune factor must be between 0.0 than 1.0");
			}
			this.MIN_ENTRIES_TO_EVICT = (int) (maxEntries * pruneFactor);
			makeSpace();
		}
	}
	protected Cache(int maxEntries,double pruneFactor){
		reconfigure(maxEntries, pruneFactor);
	}

	public void makeSpace(){
		if (maxEntries == MAX_ENTRIES_UNLIMITED || DoubleUtils.equals(0,pruneFactor) || DoubleUtils.equals(maxEntries * pruneFactor , 0 ) || cacheMap.size() < maxEntries) {
			return ;
		}
		synchronized (this) {
			if (cacheMap.size() >= maxEntries){
				if (pruneFactor == 1){
					evictKeys(new ArrayList<K>(cacheMap.keySet()));
					return;
				}
				int numEntriesToRemove = cacheMap.size() - maxEntries  + (int)(pruneFactor * maxEntries) ;


				List<K> keysToRemove = new ArrayList<>();
				for (Long time: keysAccessedByTime.keySet()){//We will read in the order of being Accessed.
					
					for (K key : keysAccessedByTime.get(time)) {
						if (isEvictable(key)) {
							keysToRemove.add(key);
						}
					}
					if (keysToRemove.size() >= numEntriesToRemove){
						break;
					}
				}
				evictKeys(keysToRemove);
	 		}
		}
	}

	protected void evictKeys(List<K> keysToRemove) {
		for(K k : keysToRemove) {
			evictKey(k);
		}
	}
	protected V evictKey(K key){
		return removeEntry(key);
	}

	protected boolean isEvictable(K lruKey) {
		return true;
	}

	@SuppressWarnings("unchecked")
	public Cache<K,V> clone(){
		try {
			synchronized (this) {
				Cache<K,V> clone = (Cache<K,V>)super.clone();
				clone.accessTimeMap = (HashMap<K, Long>)accessTimeMap.clone();
				clone.keysAccessedByTime = (TreeMap<Long, Set<K>>) keysAccessedByTime.clone();
				clone.cacheMap = (HashMap<K, V>)cacheMap.clone();
				for (K k :clone.cacheMap.keySet()){
					clone.cacheMap.put(k, ObjectUtil.clone(clone.get(k)));
				}
				return clone;
			}
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void merge(Cache<K,V> another){
		ObjectUtil.mergeValues(another.accessTimeMap,this.accessTimeMap);
		ObjectUtil.mergeValues(another.cacheMap,this.cacheMap);
		ObjectUtil.mergeValues(another.keysAccessedByTime,this.keysAccessedByTime);
	}
	public int size(){
		synchronized (this) {
			return cacheMap.size();
		}
	}
	public boolean isEmpty() {
		return size() == 0 ; 
	}
	
	public Set<K> keySet(){
		return Collections.unmodifiableSet(cacheMap.keySet());
	}
	
	@Override
	public boolean containsKey(Object key){
		synchronized (this) {
			return cacheMap.containsKey(key);
		}
	}

	public boolean containsValue(Object value) {
		synchronized (this) {
			return cacheMap.containsValue(value);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key){
		V v = cacheMap.get(key) ;
		if (v != null){
			updateAccessTime((K)key);
			return v;
		}

		if (!containsKey(key)){
			v = getValue((K)key); //Can be too expensive. Blocking can be disastrous.
			synchronized (this){
				V v1 = cacheMap.get(key);
				if (v1 == null){
					put((K) key, v);
				}else {
					v = v1;
					updateAccessTime((K)key);
				}
			}
		}else {
			updateAccessTime((K)key);
		}

		return v;
	}

	@SuppressWarnings("unchecked")
	public V remove(Object key){
		return removeEntry(key);
	}
	private V removeEntry(Object key){
		V previous = null;
		synchronized (this) {
			previous = cacheMap.remove(key);
			removePreviousAccessTime((K)key);
		}
		return previous;
	}
	public void clear(){
		clearEntries();
	}
	private void clearEntries() { 
		synchronized (this) {
			cacheMap.clear();
			accessTimeMap.clear();
			keysAccessedByTime.clear();
		}
	}
	
	public V put(K key,V value){
		V previous = null;
		synchronized (this) {
			makeSpace();
			previous = cacheMap.put(key, value);
			updateAccessTime(key);
		}
		return previous;
	}
	
	protected abstract V getValue(K k);
	
	public Collection<V> values(){
		synchronized (this) {
			return cacheMap.values();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Entry<K,V> e: entrySet()){ 
			put(e.getKey(),e.getValue());
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		synchronized (this) {
			return Collections.unmodifiableSet(cacheMap.entrySet());
		}
	}
	
	
	@Override
	public String toString() {
		synchronized (this) {
			return cacheMap.toString();
		}
	}
	private HashMap<K,V> cacheMap = new HashMap<K, V>();
	private HashMap<K,Long> accessTimeMap = new HashMap<K, Long>();
	private TreeMap<Long,Set<K>> keysAccessedByTime = new TreeMap<Long, Set<K>>() ;
	private Set<K> accessedKeys(Long epoch){ 
		Set<K> keys = keysAccessedByTime.get(epoch);
		if (keys == null) {
			keys = new HashSet<>();
			keysAccessedByTime.put(epoch, keys);
		}
		return keys;
	}
	private void removeEpoch(Long  epoch) { 
		keysAccessedByTime.remove(epoch);
	}
	private Long removePreviousAccessTime(K key) { 
		Long oldEpoch = accessTimeMap.remove(key);
		if (oldEpoch != null) {
			Set<K> keys = accessedKeys(oldEpoch); 
			keys.remove(key);
			if (keys.isEmpty()) {
				removeEpoch(oldEpoch);
			}
		}
		return oldEpoch;
	}
	private void createNewAccessTime(K key) { 
		fakeTime.increment();
		Long newEpoch = fakeTime.longValue() / Math.max(1, MIN_ENTRIES_TO_EVICT);
		
		accessTimeMap.put((K)key, newEpoch);
		accessedKeys(newEpoch).add(key);
	}
	
	private void updateAccessTime(K key) {
		synchronized (this){
			removePreviousAccessTime(key);
			createNewAccessTime(key);
		}
	}
	
	

}

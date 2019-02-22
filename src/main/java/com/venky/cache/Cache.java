package com.venky.cache;

import com.venky.core.checkpoint.Mergeable;
import com.venky.core.math.DoubleUtils;
import com.venky.core.util.ObjectUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Cache<K,V>  extends LinkedHashMap<K,V> implements ICache<V> , Mergeable<Cache<K,V>> , Serializable ,Map<K,V> {

	private int maxEntries;
	private double pruneFactor;
  	public static final int MAX_ENTRIES_DEFAULT = 1000;
  	public static final double PRUNE_FACTOR_DEFAULT = 0.2;
  	public static final int MAX_ENTRIES_UNLIMITED = 0;


    public Cache(){
        this(MAX_ENTRIES_DEFAULT,PRUNE_FACTOR_DEFAULT);
    }
  	public Cache(int maxEntries,double pruneFactor){
  		super(16,0.75f,true);
  		this.maxEntries = maxEntries;
  		this.pruneFactor = pruneFactor;
	}
	public void makeSpace(){
		if (maxEntries == MAX_ENTRIES_UNLIMITED || DoubleUtils.equals(0,pruneFactor) || DoubleUtils.equals(maxEntries * pruneFactor , 0 ) || size() <= maxEntries) {
            return ;
        }
        if (pruneFactor >= 1.0){
            evictKeys(new ArrayList<K>(keySet()));
            return ;
        }

        int numEntriesToRemove = (int)(maxEntries * pruneFactor);
        Iterator<Map.Entry<K,V>> i = entrySet().iterator();
        List<K> keys = new ArrayList<>();
        while (i.hasNext() && numEntriesToRemove > 0){
            Map.Entry<K,V> entry = i.next();
            if (isEvictable(entry.getKey())){
                keys.add(entry.getKey());
                numEntriesToRemove --;
            }
        }
        evictKeys(keys);


    }
    protected void evictKeys(List<K> keysToRemove) {
        for (K k : keysToRemove){
            evictKey(k);
        }
    }
    protected V evictKey(K key) {
        return remove(key);
    }

    protected boolean isEvictable(K lruKey){
        return  true;
    }
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key){
		V v = super.get(key) ;
		if (v != null){
			return v;
		}

		if (!containsKey(key)){
			v = getValue((K)key); //Can be too expensive. Blocking can be disastrous.
			synchronized (this){
				V v1 = super.get(key);
				if (v1 == null){
					put((K) key, v);
				}else {
					v = v1;
				}
			}
		}

		return v;
	}

    protected abstract V getValue(K key) ;

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        makeSpace();
        return false;
  	}
	@Override
	public void merge(Cache<K, V> another) {
		ObjectUtil.mergeValues(another,this);
	}
    public Cache<K,V> clone() {
        Cache<K,V> clone = (Cache<K,V>)super.clone();
        for (K k : keySet()){
            clone.put(k,ObjectUtil.clone(clone.get(k)));
        }
        return clone;
    }
    public void reconfigure(int maxEntries,double pruneFactor){
  	    this.maxEntries = maxEntries;
  	    this.pruneFactor = pruneFactor;
    }

    

}

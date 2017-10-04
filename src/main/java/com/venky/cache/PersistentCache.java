package com.venky.cache;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.esotericsoftware.kryo.KryoException;
import com.venky.core.util.MultiException;
import com.venky.core.util.ObjectUtil;

public abstract class PersistentCache<K,V> extends Cache<K, V>{

	private static final long serialVersionUID = -1707252397643517532L;
	public PersistentCache() {
		this(Cache.MAX_ENTRIES_DEFAULT,Cache.PRUNE_FACTOR_DEFAULT);
	}
	public PersistentCache(int maxEntries,double pruneFactor) { 
		super(maxEntries,pruneFactor);
		loadIndex();
	}
	
	public int size() { 
		return indexMap.size();
	}

	public boolean containsKey(Object key) { 
		return indexMap.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {
		for (K k : keySet()) {
			if (get(k).equals(value)) { 
				return true;
			}
		}
		return false;
	}
	@Override
	public Set<K> keySet(){ 
		return new AbstractSet<K>() {
			
			
			@Override
			public Iterator<K> iterator() {
				Iterator<K> ki = new Iterator<K>() { 
					Iterator<K> keys = indexMap.keySet().iterator();

					@Override
					public boolean hasNext() {
						return keys.hasNext();
					}

					@Override
					public K next() {
						return keys.next();
					}
					
				};
				return ki;
			}

			@Override
			public int size() {
				return indexMap.size();
			} 
			
		};
	}
	@Override 
	public Set<V> values(){
		Set<V> set = new AbstractSet<V>() {
			
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<K> keys = indexMap.keySet().iterator();

					@Override
					public boolean hasNext() {
						return keys.hasNext();
					}

					@Override
					public V next() {
						return get(keys.next());
					}
				};
				
			}

			@Override
			public int size() {
				return indexMap.size();
			} 
			
		};
		return set;
	}
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key){
		V v = null;
		if (super.containsKey(key)) {
			v = super.get(key);
		}else {
			synchronized (this) { 
				if (super.containsKey(key)) {
					v = super.get(key);
				}else {
					v = getPersistedValue((K)key);
					put((K)key, v);
				}
			}
		}
		return v;
	}
	
	public V put(K key,V value){ 
		V ret = super.put(key,value);
		if (autoPersist()) {
			persist(key,value);
		}else {
			indexMap.put(key, -1L);
		}
		return ret;
	}
	
	public synchronized V remove(Object key){
		Long position = indexMap.get(key);
		V v = null;
		if (position != null) {
			KryoStore cacheStore = getCacheStore();
			cacheStore.setReaderPosition(position);
			K k = cacheStore.read();
			V pv = cacheStore.read();
			if (k.equals(key)) {
				indexMap.remove(key);//Will need to repersist.
				v = pv;
			}
			compactIndex();
		}
		return v;

	}

	
	private KryoStore indexStore = null ; 
	private KryoStore cacheStore = null ;
	private KryoStore getIndexStore() { 
		if (indexStore == null ) { 
			indexStore = new KryoStore(getIndexDB());
		}
		return indexStore;
	}
	private KryoStore getCacheStore() { 
		if (cacheStore == null) {
			cacheStore  = new KryoStore(getCacheDB());
		}
		return cacheStore;
	}
	
	private Map<K,Long> indexMap = new HashMap<>();
	
	
	protected synchronized void loadIndex() {
		KryoStore indexStore = getIndexStore();
		while (! indexStore.eof() ) {
			K k = indexStore.read();
			Long v  = indexStore.read();
			indexMap.put(k,v);
		}
	}
	
	
	public synchronized void persist(K k, V v) { 
		KryoStore indexStore = getIndexStore(); 
		KryoStore cacheStore = getCacheStore();
		
		long iPos = indexStore.getWriterPosition();
		long cPos = cacheStore.getWriterPosition(); 
		MultiException mex = new MultiException("Cache could not be persisted!");
		try {
			if (indexMap.containsKey(k) && indexMap.get(k) > 0) {
				return;
			} 
			indexStore.write(k);
			indexStore.write(new Long(cPos));
			indexStore.flush();
			
			cacheStore.write(k);
			cacheStore.write(v);
			cacheStore.flush(); 
			indexMap.put(k, cPos);
		} catch (KryoException e) {
			mex.add(e);
			try { 
				indexStore.setWriterPosition(iPos);
				cacheStore.setWriterPosition(cPos);
				indexStore.truncate(iPos);
				cacheStore.truncate(cPos);
			}catch(KryoException ke) {
				mex.add(ke);
				//Cache is corrupt. 
				indexStore.delete();
				cacheStore.delete();
				this.indexStore = null; 
				this.cacheStore = null;
			}
			throw mex;
		}
	}
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return new AbstractSet<Map.Entry<K,V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return new Iterator<Entry<K,V>>(){

					Iterator<K> keys = keySet().iterator();
					@Override
					public boolean hasNext() {
						return keys.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						K key = keys.next();
						V v = get(key);
						return new Entry<K, V>() {

							@Override
							public K getKey() {
								return key;
							}

							@Override
							public V getValue() {
								return v;
							}

							@Override
							public V setValue(V value) {
								if (!ObjectUtil.equals(v, value)) {
									indexMap.remove(key);
									put(key,value);
								}
								return v;
								
							}
						};
						
					}
					
				};
			}

			@Override
			public int size() {
				return indexMap.size();
			}
			
		};
	}
	
	protected V getPersistedValue(K key) { 
		Long position = indexMap.get(key);
		V v = null;
		if (position == null) { 
			v = getValue(key);
		}else {
			KryoStore cacheStore = getCacheStore();
			cacheStore.setReaderPosition(position);
			K k = cacheStore.read();
			V pv = cacheStore.read();
			if (k.equals(key)) {
				v = pv;
			}else { 
				indexMap.remove(key);//Will need to repersist.
				v = getValue(key);
			}
		}
		return v;
	}
	public  void close() { 
		getIndexStore().close();
		getCacheStore().close();
		this.cacheStore = null ; 
		this.indexStore = null ;
	}

	protected File getTempCacheDB() { 
		return new File(getCacheDirectory(),"data.work.db");
	}
	protected File getTempIndexDB() { 
		return new File(getCacheDirectory(),"index.work.db");
	}
	

	protected File getCacheDB() { 
		return new File(getCacheDirectory(),"data.db");
	}
	protected File getIndexDB() { 
		return new File(getCacheDirectory(),"index.db");
	}
	
	protected File getCacheDirectory(){
		String cacheDir  = getCacheDirectoryName();
		if (cacheDir == null) { 
			throw new NullPointerException("Cache Directory not passed");
		}else { 
			File cd = new File(cacheDir); 
			cd.mkdirs();
			if (!cd.isDirectory()) {
				throw new RuntimeException(cacheDir + " is not a directory!");
			}else if (!cd.exists()) { 
				throw new RuntimeException("Unable to create directory " + cacheDir );
			}
			return cd;
		}
	}
	
	protected abstract Logger getLogger() ;
	protected abstract String getCacheDirectoryName();
	@Override
	protected abstract V getValue(K key) ; 
	
	
	protected boolean autoPersist() { 
		return true;
	}

	public void persist() {
		for (K k : keySet()) {
			persist(k,get(k));
		}
	}
	public void persist(K k) { 
		persist(k,get(k));
	}
	
	public synchronized void compact() { 
		KryoStore oldStore = getCacheStore();
		KryoStore oldIndexStore = getIndexStore(); 
		KryoStore newStore = new KryoStore(getTempCacheDB());
		KryoStore newIndexStore = new KryoStore(getTempIndexDB());
		
		indexMap.forEach((k,p) -> {
			oldStore.setReaderPosition(p);
			K pk  = oldStore.read();
			V v  = oldStore.read();
			if (k.equals(pk)) {
				newStore.write(k);
				newStore.write(v);
				newStore.flush(); 
				newIndexStore.write(k);
				newIndexStore.write(new Long(newStore.getWriterPosition()));
				newIndexStore.flush();
			}
		});
		oldStore.close();
		oldIndexStore.close();
		newStore.close();
		newIndexStore.close();
		move(getTempCacheDB(),getCacheDB());
		move(getTempIndexDB(),getIndexDB());
		this.cacheStore = null; 
		this.indexStore = null;
	}
	protected synchronized void compactIndex() { 
		KryoStore indexStore = new KryoStore(getTempIndexDB());
		indexMap.forEach((k,p)->{
			indexStore.write(k); 
			indexStore.write(p);
		});
		indexStore.close();
		move(getTempIndexDB(),getIndexDB());
		this.indexStore = null;
	}
	protected void move (File source , File target) {
		if (!target.equals(source)){
			try {
				target.delete();
				FileUtils.moveFile(source, target);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	
}

package com.venky.cache;

import com.esotericsoftware.kryo.KryoException;
import com.venky.core.util.MultiException;
import com.venky.core.util.ObjectUtil;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class PersistentCache<K,V> extends Cache<K, V>{

	private static final long serialVersionUID = -1707252397643517532L;
	public PersistentCache() {
		this(Cache.MAX_ENTRIES_DEFAULT,Cache.PRUNE_FACTOR_DEFAULT);
	}
	public PersistentCache(int maxEntries,double pruneFactor) {
		super(maxEntries,pruneFactor);
	}

	public boolean exists(){
		return getIndexDB().exists() && getCacheDB().exists();
	}

	public int size() {
		ensureOpen();
		return indexMap.size();
	}

	public boolean containsKey(Object key) {
		ensureOpen();
		return indexMap.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {
		ensureOpen();
		for (K k : keySet()) {
			if (get(k).equals(value)) { 
				return true;
			}
		}
		return false;
	}
	@Override
	public Set<K> keySet(){
		ensureOpen();
		synchronized (this){
			return new AbstractSet<K>() {


				@Override
				public Iterator<K> iterator() {
					Iterator<K> ki = new Iterator<K>() {
						Iterator<K> keys = new ArrayList<>(indexMap.keySet()).iterator();

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
	}
	@Override 
	public Set<V> values(){
		ensureOpen();
		synchronized (this){
			Set<V> set = new AbstractSet<V>() {

				@Override
				public Iterator<V> iterator() {
					return new Iterator<V>() {
						Iterator<K> keys = new ArrayList<>(indexMap.keySet()).iterator();

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
	}
	@SuppressWarnings("unchecked")
	@Override
	public V get(Object key){
		ensureOpen();
		V v = null;
		synchronized (this){
			if (super.containsKey(key)) {
				v = super.get(key);
			}else {
				v = getPersistedValue((K)key);
				put((K)key, v, false);
			}
		}
		return v;
	}
	public V put(K key,V value){
		synchronized (this){
			return put(key,value,true);
		}
	}
	private V put(K key,V value,boolean forcePersist){
		ensureOpen();
		V ret = super.put(key,value);
		if (forcePersist){
			indexMap.remove(key);
		}

		if (!indexMap.containsKey(key)){
			if (isAutoPersist()) {
				persist(key,value);
			}else {
				indexMap.put(key, -1L);
			}
		}
		return ret;
	}

	@Override
	protected void evictKeys(List<K> keys){
		super.evictKeys(keys);
		flush();
	}
	@Override
	protected V evictKey(K key){
		V value = super.evictKey(key);
		if (!indexMap.containsKey(key) || indexMap.get(key) < 0) {
			persist(key, value, false);
		}
		return value;
	}
	public V remove(Object key){
		ensureOpen();
		super.remove(key);
		Long position = indexMap.get(key);
		V v = null;

		if (position != null){
			if (position > 0) {
				synchronized (this) {
					KryoStore cacheStore = getCacheStore();
					cacheStore.position(position);
					K k = cacheStore.read();
					V pv = cacheStore.read();
					if (k.equals(key)) {
						indexMap.remove(key);//Will need to repersist.
						v = pv;
						compactIndex();
					}
				}
			}else {
				indexMap.remove(key);
			}
		}
		return v;

	}

	
	private KryoStore indexStore = null ; 
	private KryoStore cacheStore = null ;
	private KryoStore getIndexStore() {
		synchronized (this){
			if (indexStore == null ) {
				indexStore = new KryoStore(getIndexDB());
			}
		}
		return indexStore;
	}
	private KryoStore getCacheStore() {
		synchronized (this){
			if (cacheStore == null) {
				cacheStore  = new KryoStore(getCacheDB());
			}
		}
		return cacheStore;
	}
	
	private Map<K,Long> indexMap = null;



	public void persist(K k, V v) {
		persist(k,v,true);
	}
	private void persist(K k, V v,boolean flush) {
		ensureOpen();
		synchronized (this){
			KryoStore indexStore = getIndexStore();
			KryoStore cacheStore = getCacheStore();

			if (cacheStore.getWriterPosition() < cacheStore.size()){
				cacheStore.position(cacheStore.size());
			}
			if (indexStore.getWriterPosition() < indexStore.size()) {
				indexStore.position(getIndexStore().size());
			}

			long iPos = indexStore.getWriterPosition();
			long cPos = cacheStore.getWriterPosition();
			MultiException mex = new MultiException("Cache could not be persisted!");
			try {
				if (indexMap.containsKey(k) && indexMap.get(k) > 0) {
					return;
				}
				indexStore.write(k);
				indexStore.write(new Long(cPos));
				if (flush) {
					indexStore.flush();
				}

				cacheStore.write(k);
				cacheStore.write(v);
				if (flush) {
					cacheStore.flush();
				}
				indexMap.put(k, cPos);
			} catch (KryoException e) {
				mex.add(e);
				try {
					indexStore.position(iPos);
					cacheStore.position(cPos);
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
	}
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		ensureOpen();
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
						final K key = keys.next();
						final V v = get(key);
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
			synchronized (this) {
				KryoStore cacheStore = getCacheStore();
				cacheStore.position(position);
				K k = cacheStore.read();
				V pv = cacheStore.read();
				if (k != null && k.equals(key)) {
					v = pv;
				}
			}
			if (v == null){
				indexMap.remove(key);//Will need to repersist.
				v = getValue(key);
			}
		}
		return v;
	}
	private void ensureOpen(){
		if (indexMap != null){
			return;
		}
		synchronized (this){
			if (indexMap != null){
				return;
			}
			Map<K,Long> tmp = new HashMap<>();
			KryoStore indexStore = getIndexStore();
			while (! indexStore.eof() ) {
				K k = indexStore.read();
				Long v  = indexStore.read();
				tmp.put(k,v);
			}
			indexMap = tmp;
		}
	}

	public boolean isClosed(){
		return indexMap == null;
	}
	/**
	 * Ensure closing without opening should notbe an issue.
	 */
	public  void close() {
		super.clear();
		if (indexMap != null){
			indexMap.clear();
			indexMap = null;
		}
		if (this.indexStore != null){
			getIndexStore().close();
			this.indexStore = null ;
		}
		if (this.cacheStore != null){
			getCacheStore().close();
			this.cacheStore = null ;
		}
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
	
	protected abstract String getCacheDirectoryName();
	@Override
	protected abstract V getValue(K key) ;


	private boolean autoPersist = true;
	public boolean isAutoPersist() {
		return autoPersist;
	}
	public void setAutoPersist(boolean autoPersist){
		this.autoPersist = autoPersist;
	}

	public void persist() {
		List<K> keys = new ArrayList<>();
		synchronized (this) {
			keys.addAll(keySet());
		}
		for (K k : keys) { //Avoid concurrent modification exception.
			persist(k,get(k),false);
		}
		flush();

	}
	private void flush(){
		getIndexStore().flush();
		getCacheStore().flush();
	}
	public void persist(K k) { 
		persist(k,get(k));
	}
	
	public synchronized void compact() {
		persist(); //IF anything is not persisted. First persist. Then compact.

		KryoStore oldStore = getCacheStore();
		KryoStore oldIndexStore = getIndexStore(); 
		KryoStore newStore = new KryoStore(getTempCacheDB());
		KryoStore newIndexStore = new KryoStore(getTempIndexDB());
		
		for (Map.Entry<K,Long> entry : indexMap.entrySet() ){
			K k= entry.getKey();
			Long p= entry.getValue();
			oldStore.position(p);
			K pk  = oldStore.read();
			V v  = oldStore.read();
			if (k.equals(pk)) {
				newIndexStore.write(k);
				newIndexStore.write(new Long(newStore.getWriterPosition()));
				newStore.write(k);
				newStore.write(v);
			}
		};
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
		for( Map.Entry<K,Long> entry : indexMap.entrySet() ) {
			K k  = entry.getKey();
			Long p = entry.getValue();
			indexStore.write(k); 
			indexStore.write(p);
		};
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


	public void clear(){
		synchronized (this){
			super.clear();
			if (indexMap != null){
				indexMap.clear();
			}else {
				indexMap = new HashMap<>();
			}
			compact();
		}
	}
	
	
}

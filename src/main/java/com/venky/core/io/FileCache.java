package com.venky.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;

import de.javakaffee.kryoserializers.JdkProxySerializer;
import org.apache.commons.io.FileUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.venky.core.io.Locker.LockMode;
import com.venky.core.util.MultiException;

public abstract class FileCache<T> {

		
	private final String cacheName;
	protected FileCache(String cacheName){
		this.cacheName = cacheName;
	}
	
	public static enum CacheMode {
		READ,
		WORK(){
			public String cacheSuffix(){
				return ".work";
			}
		};

		public String cacheSuffix() {
			return "";
		}
	}

	public T get() {
		T t = getFromCache(CacheMode.READ);
		if (t == null || isCacheInValidated(t)){
			T fresh = getValue(t);
			writeToCache(fresh);
			if (t == null || isCachingComplete()){
				t = fresh;
			}
		}
		return t;
	}

	public boolean isCacheInValidated(T t){
		return isCacheInValidated();
	}

	protected String getCacheName(){
		return cacheName;
	}
	
	protected File getCacheDirectory(){
		File cache = new File(getCacheDirectoryName());
		if (!cache.exists()) {
			cache.mkdirs();
		}
		return cache;
	}

	public File getCacheFile(CacheMode cacheMode){
		return new File(getCacheDirectory(), cacheName + cacheMode.cacheSuffix());
	}
	
	@SuppressWarnings("unchecked")
	public T getFromCache(CacheMode cacheMode) {
		File cacheFile = getCacheFile(cacheMode);
		T cache = null;
		Logger cat = getLogger();
		if (cacheFile.exists()) {
			Kryo kryo = createCryo();
			Input in = null;
			Lock lock = Locker.instance().getLock(getCacheFile(CacheMode.READ).getName(), LockMode.READ);
			try {
				lock.lock();
				in = new Input(new FileInputStream(cacheFile));
				cache = (T) kryo.readClassAndObject(in);
			} catch (Exception e) {
				cat.warning("Cache " + cacheName + " is corrupted!, deleting it");
				try {
					cacheFile.delete();
				}catch(SecurityException ex){
					cat.warning("Cache " + cacheName + " delete failed due to " + ex.getMessage());
				}
			} finally {
				if (lock != null) {
					lock.unlock();
				}
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}finally {
						in = null;
					}
				}
				kryo = null;
			}
		}
		return cache;
	}

	
	public boolean isCacheInValidated(){
		File f = getCacheFile(CacheMode.READ);
		return !f.exists() || (f.lastModified() < (System.currentTimeMillis() - getCacheValidityTime()));
	}
	
	private long cacheValidityTime = Long.MAX_VALUE;
	protected long getCacheValidityTime() {
		return cacheValidityTime;
	}
	
	protected void setCacheValidityTime(long cacheValidityTime){ 
		this.cacheValidityTime = cacheValidityTime;
	}
	
	public void writeToCache(T cache) {
		Kryo kryo = createCryo();
		Lock lock = Locker.instance().getLock(cacheName,LockMode.EXCLUSIVE);
		try {
			File cacheFile = getCacheFile(CacheMode.WORK);
			lock.lock();
			FileOutputStream fos = new FileOutputStream(cacheFile);
			Output output = new Output(fos);
			kryo.writeClassAndObject(output, cache);
			output.close();
			afterCacheWrite();
		} catch (IOException e) {
			MultiException m = new MultiException();
			m.add(e);
			try {
				getCacheFile(CacheMode.WORK).delete();
			}catch (SecurityException ex){
				m.add(ex);
			}
			throw m;
		}finally {
			kryo = null; //gc 
			if (lock != null){
				lock.unlock();
				lock = null;
			}
		}
	}
	public boolean isCachingComplete(){
		File workFile = getCacheFile(CacheMode.WORK);
		File readFile = getCacheFile(CacheMode.READ);
		if (!readFile.equals(workFile)){
			if (workFile.exists() || !readFile.exists()){
				return false;
			}
		}
		return true;
	}
	
	protected void afterCacheWrite() {
		File workFile = getCacheFile(CacheMode.WORK);
		File readFile = getCacheFile(CacheMode.READ);
		move(workFile,readFile);
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
	protected void copy (File source , File target) {
		if (!target.equals(source)){
			try {
				FileUtils.copyFile(source, target,false);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	private Kryo createCryo(){
		Kryo kryo = new Kryo();
		kryo.setClassLoader(getClass().getClassLoader());
		kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
		kryo.register(InvocationHandler.class,new JdkProxySerializer());
		return kryo;
	}
	
	protected abstract T getValue(T oldCache);
	protected abstract Logger getLogger() ;
	protected abstract String getCacheDirectoryName();

	public void drop(){
		Lock lock = Locker.instance().getLock(getCacheFile(CacheMode.READ).getName(), LockMode.EXCLUSIVE);
		try {
			lock.lock();
			File workFile = getCacheFile(CacheMode.WORK);
			File readFile = getCacheFile(CacheMode.READ);
			if (workFile.exists()) {
				workFile.delete();
			}
			if (readFile.exists()) {
				readFile.delete();
			}
		}finally {
			if (lock != null) {
				lock.unlock();
				lock = null;
			}
		}
	}
	
}

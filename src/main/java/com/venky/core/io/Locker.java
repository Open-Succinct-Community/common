package com.venky.core.io;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.venky.cache.Cache;

public class Locker {
	private Locker(){
		
	}
	private static Locker locker = null; 
	public static final Locker instance(){
		if (locker != null) {
			return locker;
		}
		synchronized (Locker.class) {
			if (locker == null){
				locker = new Locker();
			}
		}
		return locker;
	}
	
	private Cache<String,ReentrantReadWriteLock> locks = new Cache<String, ReentrantReadWriteLock>(0,0) {
		private static final long serialVersionUID = 803384103127949894L;

		@Override
		protected ReentrantReadWriteLock getValue(String name) {
			return new ReentrantReadWriteLock();
		}
	};
	
	public static enum LockMode {
		EXCLUSIVE,
		READ;
	}
	
	public Lock getLock(String lockName,LockMode mode){
		ReentrantReadWriteLock lockCategory = locks.get(lockName);
		return  mode == LockMode.EXCLUSIVE ? lockCategory.writeLock() : lockCategory.readLock() ;
	}
}

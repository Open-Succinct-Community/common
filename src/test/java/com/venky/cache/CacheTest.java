package com.venky.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Test;

public class CacheTest {

	@Test
	public void test() {
		RandomNumberCache cache = new RandomNumberCache(5,0.2);
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		for (int i = 1 ; i <= 6; i ++ ){
			map.put(i, cache.get(i));
			assertEquals(map.get(i), cache.get(i));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				//
			}
		}
		// first would have gone.
		for (int i = 6 ; i >= 2; i -- ){
			assertEquals(map.get(i), cache.get(i));
		}
		assertTrue("First Entry should have been replaced in cache!" ,!map.get(1).equals(cache.get(1)));
	}
	public static class RandomNumberCache extends Cache<Integer, Integer>{
		/**
		 * 
		 */
		private static final long serialVersionUID = -2999714588055843718L;
		public RandomNumberCache(int maxEntries, double pruneFactor){
			super(maxEntries,pruneFactor);
		}
		static Random r = new Random();
		@Override
		protected Integer getValue(Integer k) {
			return r.nextInt();
		}
		
	}
	@Test
	public void testPersistentCache() {
		PersistentRandomNumberCache cache = new PersistentRandomNumberCache(5,0.2);
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		for (int i = 1 ; i <= 6; i ++ ){
			map.put(i, cache.get(i));
			assertEquals(map.get(i), cache.get(i));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				//
			}
		}
		// first would have gone.
		for (int i = 2 ; i <= 6; i ++ ){
			assertEquals(map.get(i), cache.get(i));
		}
		assertTrue(cache.size() == map.size());
		assertTrue("First Entry should be same as the cache is persisted !" ,map.get(1).equals(cache.get(1)));
		assertTrue(cache.size() == map.size());
		
	}
	
	@SuppressWarnings("unused")
	private static class ListInvocationHandler<T> implements InvocationHandler {
		private List<T> inner ; 
		public ListInvocationHandler() {
		
		}
		public ListInvocationHandler(List<T> list) {
			this.inner = list;
		}
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return method.invoke(inner,args);
		} 
		
	}
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testJDKProxySerialization() { 
		List<String> s = (List<String>) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {List.class}, new ListInvocationHandler<String>(new ArrayList<String>()));
		
		s.add("Hello");
		s.add("World");
		KryoStore store = new KryoStore("target/cache/hw");
		store.write(s);
		store.flush();
		store.setReaderPosition(0);
		List<String> ps = store.read();
		
		assertEquals("Persisted Proxy Problem" , ps, s);
		store.delete();
	}
	
	
	public static class PersistentRandomNumberCache extends PersistentCache<Integer,Integer>{
		
		private static final long serialVersionUID = -6434256684314981897L;
		static Random r = new Random();
		public PersistentRandomNumberCache(int maxEntries, double pruneFactor) {
			super(maxEntries,pruneFactor);
		}
		@Override
		protected Integer getValue(Integer key) {
			return r.nextInt();
		}
		@Override
		public Logger getLogger() {
			return Logger.getGlobal();
		}
		@Override
		protected String getCacheDirectoryName() {
			return "target/cache/random";
		}
		
	}
	
}

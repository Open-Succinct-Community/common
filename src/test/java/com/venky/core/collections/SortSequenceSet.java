package com.venky.core.collections;

import java.util.Collections;

import org.junit.Test;

import junit.framework.Assert;

public class SortSequenceSet {

	@Test
	public void test() {
		SequenceSet<Integer> s = new SequenceSet<Integer>();
		s.add(0,1);
		s.add(1,5);
		s.add(2,2);
		System.out.println(s);
		Collections.sort(s);
		System.out.println(s);
		s.add(2, 0);
		System.out.println(s);
		Collections.sort(s);
		System.out.println(s);
	}
	
	@Test
	public void testReversal() {
		SequenceSet<Integer> s = new SequenceSet<Integer>(); 
		for (int i = 0 ; i < 10;  i ++) {
			s.add(i);
		}
		SequenceSet<Integer> n = s.reverse();
		for (int i = 0 ; i < 10 ; i ++ ){
			Assert.assertEquals(9-i,n.get(i).intValue());
		}
		System.out.println(n);
	}
	@Test
	public void testReversalMap() {
		SequenceMap<Integer,String> s = new SequenceMap<Integer,String>(); 
		for (int i = 0 ; i < 10;  i ++) {
			s.put(i,String.valueOf(i));
		}
		SequenceMap<Integer,String> n = s.reverse();
		for (int i = 0 ; i < 10 ; i ++ ){
			Assert.assertEquals(9-i,n.indexOf(i));
		}
		System.out.println(n);
	}
}

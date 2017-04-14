package com.venky.core.collections;

import java.util.Iterator;

import org.junit.Test;

public class RemoveFromSequenceSet {
	@Test
	public void test(){ 
		SequenceSet<String> s = new SequenceSet<String>();
		s.add("1");
		s.add("2");
		s.add("3");
		s.add("4");
		
		for (Iterator<String> i = s.iterator(); i.hasNext() ; ){
			String s1 = i.next();
			System.out.println(s1);
			i.remove();
		}
		
		System.out.println(s);
		
		
	}
}

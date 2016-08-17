package com.venky.core.math;

import java.util.Comparator;

public class MinCalculator<T> {
	private final Comparator<T> ourComparator;
	public MinCalculator(){
		this(null);
	}
	public MinCalculator(final Comparator<T> comparator){
		this.ourComparator = new Comparator<T>() {

    		@Override
    		public int compare(T o1, T o2) {
    			if (comparator != null) {
            		return comparator.compare(o1, o2);
            	}
            	@SuppressWarnings("unchecked")
    			Comparable<? super T> k1 = (Comparable<? super T>) o1;
            	return k1.compareTo(o2);
    		}
    	};
	}
	
	private T min = null;
	public void add(T item){
		if (min == null || ourComparator.compare(min, item) > 0){
			min = item;
		}
	}
	public T min(){
		return min;
	}
}

package com.venky.core.math;

import java.util.Comparator;

public class SpreadCalculator<T> {
	private final Comparator<T> ourComparator;
	public SpreadCalculator(){
		this(null);
	}
	public SpreadCalculator(final Comparator<T> comparator){
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
	private T max = null ;
	public void add(T item){
		if (min == null || ourComparator.compare(min, item) > 0){
			min = item;
		}
		if (max == null || ourComparator.compare(max, item) < 0) {
			max = item;
		}
	}
	public T min(){
		return min;
	}
	public T max(){
		return max;
	}
}

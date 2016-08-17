package com.venky.core.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.venky.core.math.DoubleUtils;

/**
 * Created by venky on 8/5/16.
 */
public class PercentileCalculator<T> {
    List<T> left = null;
    List<T> right = null; 
    T leftRoot = null;
    T rightRoot = null;
    private Comparator<T> ourComparator ;

    public void clear(){
    	leftRoot = null ;
    	rightRoot = null; 
    	left.clear();
    	right.clear();
    }
    private T leftRoot() {
        if (left.isEmpty()) {
            return null;
        }
        return Collections.max(left, ourComparator);
    }

    private T rightRoot() {
        if (right.isEmpty()) {
            return null;
        }
        return Collections.min(right, ourComparator);
    }

    double dPercentile;

    public PercentileCalculator(double dPercentile) {
    	this(dPercentile,null);
    }
    public PercentileCalculator(double dPercentile, final Comparator<? super T> comparator){
    	this(dPercentile,comparator,DEFAULT_ESTIMATED_POPULATION_SIZE);
    }
    private static final int DEFAULT_ESTIMATED_POPULATION_SIZE = 10 ;
    public PercentileCalculator(double percentile, final Comparator<? super T> comparator, int populationSize){
    	populationSize = Math.max(DEFAULT_ESTIMATED_POPULATION_SIZE, populationSize); 
    	this.dPercentile = percentile / 100.0;
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
    	int leftSize = (int)(this.dPercentile * populationSize) ;
    	int rightSize =(int)(populationSize - leftSize) ;
    	this.left =  new ArrayList<>(leftSize + 10); //Just some buffer!! 
    	this.right = new ArrayList<>(rightSize + 10);
    	
    }
    
    public void setDebug(boolean debug){
    	this.debug = debug;
    }
    boolean debug = false;
    public void print(){
    	print(debug);
    }
    public void print(boolean debug){
    	if (debug){
	        System.out.println("Sample Left:" + left);
	        System.out.println("Percentile :" + getPercentile());
	        System.out.println("Sample Right:" + right);
    	}
    }
    public T getPercentile() {
    	if (balance() > 0) { // Left has more weight than needed.
            return leftRoot;
        } else {
            return rightRoot;
        }
    }
    
    public void addAll(Collection<T> items){
    	for (T item:items){
    		add(item);
    	}
    }
    public int balance(){
    	if (left.isEmpty() && right.isEmpty()) {
    		return 0;
    	}
    	return DoubleUtils.compareTo(left.size() * 1.0 / (left.size() + right.size()), dPercentile); 
    }
    public void removeAll(T item){
    	remove(item,true);
    }
    public void remove(T item){
    	remove(item,false);
    }
    public void remove(T item,boolean all){
    	boolean removedFromLeft = false; 
    	boolean removedFromRight = false;
    	T percentile = getPercentile();
    	if (percentile == null){
    		return ;
    	}
    	
    	if (ourComparator.compare(item,getPercentile()) < 0){
    		removedFromLeft = remove(item, left, all);
    	}else if (ourComparator.compare(item,getPercentile()) >0 ){
    		removedFromRight = remove(item,right,all);
    	}else {
    		if (balance() > 0) {
    			removedFromLeft = remove(item, left, all); 
    			if (all) {
    				removedFromRight = remove(item,right,all);
    			}
			}else { 
				removedFromRight = remove(item,right,all);
				if (all){
					removedFromLeft = remove(item,left,all);
				}
			}
    	}

    	if (removedFromLeft){
    		leftRoot = leftRoot();
    	}
    	if (removedFromRight) {
    		rightRoot = rightRoot();
    	}
    	if (removedFromLeft || removedFromRight) {
    		rebalance();
    	}
    }
    private void rebalance(){
    	int diff = balance() ;
    	
	    if (diff < 0) {
	    	while (diff < 0){
	            addLeft(rightRoot);
	            right.remove(rightRoot);
	            rightRoot = rightRoot();
	            diff = balance();
	    	}
	    }else if ( diff > 0) {
	    	while (diff > 0) {
		        addRight(leftRoot);
		        left.remove(leftRoot);
		        leftRoot = leftRoot();
		        diff = balance();
	    	}
	    }
    }
    public void add(T item) {
        T percentile = getPercentile();
        if (percentile == null){
        	percentile = item;
        }

        if (ourComparator.compare(percentile ,item) < 0) {
            addRight(item);
        } else if (ourComparator.compare(percentile ,item) > 0) {
            addLeft(item);
        } else if (balance() < 0) {
            addLeft(item);
        } else {
            addRight(item);
        }
        rebalance();
        print();
    }

    private void addLeft(T item) {
        left.add(item);
        if (leftRoot == null || ourComparator.compare(leftRoot ,item)< 0) {
            leftRoot = item;
        }
    }
    private static <T> boolean remove(T item, Collection<T> collection, boolean all ) {
    	Iterator<T> i = collection.iterator(); 
    	boolean removed = false;
		while (i.hasNext()) {
			T t  = i.next(); 
			if (t.equals(item)){
				i.remove();
				removed = true;
				if (!all){
					break;
				}
			}
		}
		return removed;
    }
    private void addRight(T item) {
        right.add(item);
        if (rightRoot == null || ourComparator.compare(rightRoot ,item) > 0) {
            rightRoot = item;
        }
    }
    
    public List<T> left(){
    	return left;
    }
    public List<T> right(){ 
    	return right;
    }
}

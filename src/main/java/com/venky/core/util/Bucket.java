/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venky.core.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.venky.core.checkpoint.Mergeable;
import com.venky.core.math.DoubleUtils;

/**
 *
 * @author venky
 */
public class Bucket extends Number implements Cloneable, Serializable, Mergeable<Bucket>, Comparable<Bucket>, ChangeObservable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3777328906346580670L;
	private double counter;
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	public void registerChangeListener(ChangeListener listener){
		synchronized (this) {
			listeners.add(listener);
		}
	}
	
    public Bucket(){
        this(0.0);
    }
    public Bucket(double start){
        this.counter = start;
    }
    public void increment(){
        increment(1.0);
    }
    public void decrement(){ 
        increment(-1.0);
    }
    public void decrement(double by){
        increment(-1.0 * by);
    }
    
    private boolean eventFired = false;
    public final void increment(double by){
        synchronized (this){
            double old = counter;
        	counter += by;
            if (DoubleUtils.equals(counter,0.0)){
                //Account for round off!
                counter = 0.0;
            }
        	if (!eventFired) {
        		eventFired = true;
        		try{
	                for (ChangeListener listener:listeners){
	                	listener.hasChanged(old, counter);
	                }
        		}finally{
        			eventFired = false;
        		}
        	}
        }
    }
    public double value() {
        return counter;
    }
    public int intValue(){
    	return (int)counter;
    }

    @Override
    public String toString() {
        return String.valueOf(counter);
    }
    
    @Override
    public Bucket clone() {
        Object clone; 
        try {
            clone = super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        return (Bucket)clone;
    }
    
	public void merge(Bucket another) {
		this.counter = another.counter;
	}
	@Override
	public long longValue() {
		return (long)this.counter;
	}
	@Override
	public float floatValue() {
		return (float)this.counter;
	}
	@Override
	public double doubleValue() {
		return counter;
	}
	@Override
	public int compareTo(Bucket o) {
		return Double.compare(value(), o.value());
	}

	@Override
    public boolean equals(Object obj) {
        return (obj instanceof Bucket)
               && (compareTo((Bucket)obj) == 0);
    }
       
}

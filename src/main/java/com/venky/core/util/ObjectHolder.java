package com.venky.core.util;

public class ObjectHolder<T> {
    T object;

    public ObjectHolder(T o){
        set(o);
    }

    public T get() {
        synchronized (this) {
            return object;
        }
    }

    public void set(T object) {
        synchronized (this){
            this.object = object;
            this.notifyAll();
        }
    }

}
package com.venky.core.util;

public class ObjectHolder<T> {
    T object;

    public ObjectHolder(T o){
        set(o);
    }

    public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }

}
package com.venky.core.math;

import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Range<T  extends  Number> implements Iterable<T>{
    AnyNumber<T> start, end , current, step;
    Class<T> numberClass ;

    public Range(T start, T end){
        this(start,end,start);
    }
    public Range(T start, T end ,T current){
        this(start,end,current, 1.0);
    }
    @SuppressWarnings("unchecked")
    public Range(T start, T end ,T current, double step){
        this.start = new AnyNumber<>(start);
        this.end = new AnyNumber<>(end);
        this.current = new AnyNumber<>(current);
        this.numberClass = (Class<T>)this.start.value().getClass();
        this.step = AnyNumber.valueOf(step,numberClass);
    }
    private boolean isPresent(double value){
        return start != null && end != null && start.doubleValue() <= value && end.doubleValue() >= value;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return isPresent( current.doubleValue()  );
            }

            @Override
            public T next() {
                if (!hasNext()){
                    throw  new NoSuchElementException();
                }
                T o = current.value();
                current = AnyNumber.valueOf(current.doubleValue() + step.doubleValue(), numberClass);
                return o;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Iterable.super.spliterator();
    }
}

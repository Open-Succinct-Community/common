package com.venky.core.math;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public class AnyNumber<T extends Number> extends Number implements Comparable<AnyNumber<T>>{
    private final T number;
    private final Class<T> numberClass ;
    @SuppressWarnings("unchecked")
    public AnyNumber(T number){
        this.number = number;
        numberClass = (Class<T>)number.getClass();
    }

    @Override
    public int intValue() {
        return number.intValue();
    }

    @Override
    public long longValue() {
        return number.longValue();
    }

    @Override
    public float floatValue() {
        return number.floatValue();
    }

    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Number> AnyNumber<T> valueOf(Object aNumber,Class<T> numberClass){
        if (aNumber == null){
            return null;
        }
        T inner = null;
        if (numberClass.equals(aNumber.getClass())){
            inner = (T)aNumber;
        }else if (aNumber instanceof Number){
            if (numberClass.equals(Integer.class)){
                inner = (T)(Integer.valueOf(((Number)aNumber).intValue()));
            }else if (numberClass.equals(Short.class)){
                inner =  (T)(Short.valueOf(((Number)aNumber).shortValue()));
            }else if (numberClass.equals(Long.class)){
                inner = (T)(Long.valueOf(((Number)aNumber).longValue()));
            }else if (numberClass.equals(Byte.class)){
                inner = (T)(Byte.valueOf(((Number)aNumber).byteValue()));
            }else if (numberClass.equals(Float.class)){
                inner = (T)(Float.valueOf(((Number)aNumber).floatValue()));
            }else if (numberClass.equals(Double.class)){
                inner = (T)(Double.valueOf(((Number)aNumber).doubleValue()));
            }
        }
        try {
            if (inner == null) {
                inner = numberClass.getConstructor(String.class).newInstance(String.valueOf(aNumber));
            }
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
        return new AnyNumber<>(inner);
    }

    public T value(){
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnyNumber)) return false;

        AnyNumber<?> anyNumber = (AnyNumber<?>) o;

        if (!Objects.equals(number, anyNumber.number)) return false;
        return Objects.equals(numberClass, anyNumber.numberClass);
    }

    @Override
    public int hashCode() {
        int result = number != null ? number.hashCode() : 0;
        result = 31 * result + (numberClass != null ? numberClass.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(AnyNumber<T> o) {
        T oNumber = o.number;
        return (int)(this.number.doubleValue() - o.number.doubleValue());
    }
}

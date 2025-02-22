package com.venky.core.collections;

import org.junit.Test;

public class IgnoreCaseMapTest {
    @Test
    public void test(){
        IgnoreCaseMap<String> x = new IgnoreCaseMap<>();
        x.putIfAbsent("x","1");
        System.out.println(x);
    }
}

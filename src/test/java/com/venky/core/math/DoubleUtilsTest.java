package com.venky.core.math;

import org.junit.Assert;
import org.junit.Test;

public class DoubleUtilsTest {
    @Test
    public void testRound(){
        Assert.assertEquals(0,DoubleUtils.compareTo(1299.19, 1299.2,0));


    }
}

package com.venky.core.math;

import junit.framework.Assert;

import org.junit.Test;


public class GCDFinderTest  {

	GCDFinder finder = GCDFinder.getInstance();
	/*
	 * Test method for 'com.yantra.tools.algorithms.math.GCDFinder.gcd(int, int)'
	 */
	
	@Test
	public void testGcdIntInt() {
		Assert.assertEquals(finder.gcd(2,4), 2);
		Assert.assertEquals(finder.gcd(1,5), 1);
		Assert.assertEquals(finder.gcd(2,5), 1);
		Assert.assertEquals(finder.gcd(6,14), 2);
		Assert.assertEquals(finder.gcd(6,6), 6);
		Assert.assertEquals(finder.gcd(6,0), 6);
	}

	/*
	 * Test method for 'com.yantra.tools.algorithms.math.GCDFinder.gcd(int[])'
	 */
	@Test
	public void testGcdIntArray() {
		Assert.assertEquals(finder.gcd(new int[] { }), 0);
		Assert.assertEquals(finder.gcd(new int[] { 2 }), 2);
		Assert.assertEquals(finder.gcd(new int[] { 4,6}), 2);
		Assert.assertEquals(finder.gcd(new int[] { -16,-8,12}), 4);
		Assert.assertEquals(finder.gcd(new int[] { -1,-1,-2}), 1);
		Assert.assertEquals(finder.gcd(new int[] { -1,-1,-1}), 1);
		
	}

}

package com.venky.core.math;


public class DoubleUtils {
	public static int compareTo(final double d1, final double d2) {
		return compareTo(d1,d2,8);
	}
	public static boolean equals(final double d1, final double d2) {
		return equals(d1,d2,8);
	}
	public static double min(final double d1, final double d2) {
		return min(d1,d2,8);
	}
	public static double max(final double d1, final double d2) {
		return max(d1,d2,8);
	}

	public static int compareTo(final double d1, final double d2, int scale) {
		final DoubleHolder bd1 = new DoubleHolder(d1,scale);
		final DoubleHolder bd2 = new DoubleHolder(d2,scale);
		return bd1.compareTo(bd2);
	}

	public static boolean equals(final double d1, final double d2,int scale) {
		return 0 == compareTo(d1, d2,scale);
	}
	public static double min(final double d1, final double d2, int scale) {
		return compareTo(d1, d2,scale) < 0 ? d1 : d2;
	}
	public static double max(final double d1, final double d2, int scale) {
		return compareTo(d1, d2,scale) < 0 ? d2 : d1;
	}
}

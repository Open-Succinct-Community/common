package com.venky.clustering.geography;

import com.venky.clustering.Metric;
import com.venky.geo.GeoCoordinate;

public class GeoMetric implements Metric<GeoCoordinate>{

	@Override
	public double distance(GeoCoordinate p1, GeoCoordinate p2) {
		return p1.distanceTo(p2);
	}

}

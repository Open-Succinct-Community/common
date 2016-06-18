package com.venky.clustering.geography;

import com.venky.clustering.CenterFinder;
import com.venky.clustering.CenterFinderBuilder;
import com.venky.clustering.Cluster;
import com.venky.geo.GeoLocation;
import com.venky.geo.GeoLocationBuilder;

public class GeoCentroidFinderBuilder<L extends GeoLocation> implements CenterFinderBuilder<L> {

	GeoLocationBuilder<L> geoLocationBuilder ;
	public GeoCentroidFinderBuilder(GeoLocationBuilder<L> builder) {
		this.geoLocationBuilder = builder;
	}
	@Override
	public CenterFinder<L> build(Cluster<L> cluster) {
		return new GeoCentroidFinder<L>(cluster,geoLocationBuilder);
	}
 
}

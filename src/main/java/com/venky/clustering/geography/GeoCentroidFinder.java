package com.venky.clustering.geography;

import com.venky.clustering.CenterFinder;
import com.venky.clustering.Cluster;
import com.venky.core.util.Bucket;
import com.venky.geo.GeoLocation;
import com.venky.geo.GeoLocationBuilder;

public class GeoCentroidFinder<L extends GeoLocation> implements CenterFinder<L>{
	private GeoLocationBuilder<L> geoLocationBuilder;
	private Cluster<L> cluster;
	private L center;
	public GeoCentroidFinder(Cluster<L> cluster,GeoLocationBuilder<L> builder){
		this.cluster = cluster;
		this.geoLocationBuilder = builder;
	}
	@Override
	public L center() {
		Bucket x = new Bucket();
		Bucket y = new Bucket();
		Bucket z = new Bucket();
		for (L loc: cluster.getPoints()){
			double cosLat = Math.cos(loc.getLatitude()*Math.PI / 180.0);
			double sinLat = Math.sin(loc.getLatitude()*Math.PI / 180.0);
			double cosLng = Math.cos(loc.getLongitude()*Math.PI / 180.0);
			double sinLng = Math.sin(loc.getLongitude()*Math.PI / 180.0);
			
			x.increment(cosLat * cosLng);
			y.increment(cosLat * sinLng);
			z.increment(sinLat);
		}
		int numPoints = cluster.getPoints().size();
		float lat = (float)(Math.asin(z.value()/numPoints)*180.0/Math.PI);
		float lng = (float)(Math.atan(y.value() / x.value()) * 180.0/Math.PI);
		this.center =  geoLocationBuilder.create(lat,lng);
		return center;
	}

	@Override
	public L center( L newPoint) {
		Bucket lat = new Bucket();
		Bucket lng = new Bucket();
		
		int numOldPoints = cluster.getPoints().size()-1;
		if (center != null){
			lat.increment(center.getLatitude() * numOldPoints);
			lng.increment(center.getLongitude() * numOldPoints);
		}
		lat.increment(newPoint.getLatitude());
		lng.increment(newPoint.getLongitude());
		center = geoLocationBuilder.create(lat.floatValue()/(numOldPoints + 1), lng.floatValue() / (numOldPoints + 1));
		return center;
	}

}

package com.venky.clustering.geography;

import com.venky.cache.Cache;
import com.venky.clustering.CenterFinder;
import com.venky.clustering.Cluster;
import com.venky.core.util.Bucket;
import com.venky.geo.GeoCoordinate;
import com.venky.geo.Vector3d;


public class GeoCentroidFinder implements CenterFinder<GeoCoordinate>{
	protected final Cluster<GeoCoordinate> cluster;
	private GeoCoordinate center;
	public GeoCentroidFinder(Cluster<GeoCoordinate> cluster){
		this.cluster = cluster;
	}
	
	@Override
	public GeoCoordinate center() {
		Cache<String,Bucket> centerTracker = new Cache<String, Bucket>(0,0) {
			private static final long serialVersionUID = -7958590967148503171L;

			@Override
			protected Bucket getValue(String k) {
				return new Bucket();
			}
		}; 
		
		for (GeoCoordinate loc: cluster.getPoints()){
			computeCentroidAttributes(centerTracker, loc,1);
		}
		center = center(centerTracker);
		return center;
	}
	protected void computeCentroidAttributes(Cache<String,Bucket> centerTracker, GeoCoordinate loc, int wt){
		Vector3d tmp = loc.toVector();
		Bucket x = centerTracker.get("x");
		Bucket y = centerTracker.get("y");
		Bucket z = centerTracker.get("z");
		
		x.increment(tmp.x * wt);
		y.increment(tmp.y * wt);
		z.increment(tmp.z * wt);
	}
	
	protected GeoCoordinate center(Cache<String,Bucket> centerTracker){
		Bucket x = centerTracker.get("x");
		Bucket y = centerTracker.get("y");
		Bucket z = centerTracker.get("z");
		int numPoints = cluster.getPoints().size();
		return center(x,y,z,numPoints);
	}
	
	
	private GeoCoordinate center(Bucket xTot, Bucket yTot, Bucket zTot, int numPoints ) {
		double xavg = xTot.doubleValue()/numPoints; 
		double yavg = yTot.doubleValue()/numPoints; 
		double zavg = zTot.doubleValue()/numPoints;
		return new GeoCoordinate(new Vector3d(xavg, yavg, zavg));
	}

	@Override
	public GeoCoordinate center(GeoCoordinate afterAddingNewPoint) {
		if (center == null) {
			center = center();
		}else {
			Cache<String,Bucket> centerTracker = new Cache<String, Bucket>(0,0) {
				private static final long serialVersionUID = -7958590967148503171L;

				@Override
				protected Bucket getValue(String k) {
					return new Bucket();
				}
			}; 
			int numPoints = cluster.getPoints().size();
			int previousSize = numPoints - 1;

			computeCentroidAttributes(centerTracker, center , previousSize);
			computeCentroidAttributes(centerTracker, afterAddingNewPoint, 1);

			center = center(centerTracker);
		}
		return center;
	}
}

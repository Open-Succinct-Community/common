package com.venky.clustering.euclidean;

import java.util.ArrayList;
import java.util.List;

import com.venky.clustering.CenterFinder;
import com.venky.clustering.Cluster;
import com.venky.core.util.Bucket;

public class EuclideanCenterFinder implements CenterFinder<EuclideanPoint>{
	Cluster<EuclideanPoint> cluster;
	public EuclideanCenterFinder(Cluster<EuclideanPoint> cluster) {
		this.cluster = cluster;
	}
	EuclideanPoint center;
	@Override
	public EuclideanPoint center() {
		
		List<Bucket> coordinates = new ArrayList<Bucket>();
		double numPoints = cluster.getPoints().size();
		
		for (EuclideanPoint p : cluster.getPoints()){
			int dim = p.coordinates.length;
			while (coordinates.size() < dim){
				coordinates.add(new Bucket());
			}
			for (int i = 0 ; i < dim ; i ++ ){
				coordinates.get(i).increment(p.coordinates[i]/numPoints);
			}
		}
		
		center = new EuclideanPoint(coordinates.toArray(new Bucket[]{}));
		return center;
	}

	@Override
	public EuclideanPoint center(EuclideanPoint newPoint) {
		List<Bucket> coordinates = new ArrayList<Bucket>();
		
		int dim = Math.max(center.coordinates.length,newPoint.coordinates.length);

		while (coordinates.size() < dim){
			coordinates.add(new Bucket());
		}
		
		double numOldPoints = cluster.getPoints().size() - 1;
		for (int i = 0 ; i < dim ; i ++ ){
			double c = ( center.coordinates[i] * numOldPoints + newPoint.coordinates[i] )/ ( numOldPoints + 1 ) ;
			coordinates.get(i).increment(c);
		}
		
		center= new EuclideanPoint(coordinates.toArray(new Bucket[]{}));
		return center;
	}

}

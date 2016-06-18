package com.venky.clustering.euclidean;

import com.venky.clustering.CenterFinder;
import com.venky.clustering.CenterFinderBuilder;
import com.venky.clustering.Cluster;

public class EuclideanCenterFinderBuilder implements CenterFinderBuilder<EuclideanPoint> {

	@Override
	public CenterFinder<EuclideanPoint> build(Cluster<EuclideanPoint> cluster) {
		return new EuclideanCenterFinder(cluster);
	}

}

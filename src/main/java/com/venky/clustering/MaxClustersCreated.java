package com.venky.clustering;

import java.util.List;

public class MaxClustersCreated<T> implements StopCriteria<T>{

	int maxClusters = 1;
	public MaxClustersCreated(){
		this(2);
	}
	public MaxClustersCreated(int maxClusters){
		this.maxClusters = maxClusters;
		if (maxClusters < 1) {
			throw new IllegalArgumentException("MaxClusters must be a positive number");
		}
	}

	public boolean canStop(List<Cluster<T>> currentClusters) {
		return currentClusters.size() <= maxClusters ;
	}

}

package com.venky.clustering;

import java.util.List;

public interface StopCriteria<T> {
	public boolean canStop(List<Cluster<T>> currentClusters,Cluster<T> clusterGrown, Cluster<T> clusterDestroyed);
}

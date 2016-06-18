package com.venky.clustering;

public interface CenterFinderBuilder<T> {
	public CenterFinder<T> build(Cluster<T> cluster);
}

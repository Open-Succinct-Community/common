package com.venky.clustering;

public interface ClusterBuilder<T> {
    public Cluster<T> init(CenterFinderBuilder<T> centerFinderBuilder, Metric<T> metric);
}
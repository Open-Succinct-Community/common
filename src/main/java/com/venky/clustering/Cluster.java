package com.venky.clustering;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.venky.core.string.StringUtil;

public class Cluster<T> {

    private CenterFinder<T> centerFinder;
    private Metric<T> metric;
    private int id;
    private static AtomicInteger fakeId = new AtomicInteger();

    
    public Cluster(T fixedCentroid , Metric<T> m) {
        this((CenterFinderBuilder<T>)null,m);
        this.centroid = fixedCentroid;
    }
    public Cluster(CenterFinderBuilder<T> cf, Metric<T> m) {
        this.centerFinder = cf == null ? null : cf.build(this);
        this.metric = m;
        this.id = fakeId.addAndGet(1);
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        return StringUtil.valueOf(id);
    }

    public boolean equals(Cluster<T> cluster) {
        return id == cluster.id;
    }

    private T centroid = null;

    public T centroid() {
        if (centroid == null && centerFinder != null) {
            centroid = centerFinder.center();
        }
        return centroid;
    }

    public void addPoint(T t) {
        getPoints().add(t);
        if (centerFinder != null) {
            centroid = centerFinder.center(t);
        }
    }

    public double centroidDistance(T point) {
        T centroid = centroid();
        if (centroid != null) {
            return metric.distance(centroid, point);
        }
        return Double.POSITIVE_INFINITY;
    }

    public double centroidDistance(Cluster<T> cluster) {
        T centroid = centroid();
        if (centroid != null) {
            return metric.distance(centroid, cluster.centroid());
        }
        return Double.POSITIVE_INFINITY;
    }

    public Distance distance(T point) {
        Distance distance = new Distance();
        distance.distanceFromCentroid = centroidDistance(point);

        distance.minDistance = Double.POSITIVE_INFINITY;
        distance.maxDistance = Double.NEGATIVE_INFINITY;
        for (T p : getPoints()) {
            double d = metric.distance(p, point);
            if (d < distance.minDistance) {
                distance.minDistance = d;
            }
            if (d > distance.maxDistance) {
                distance.maxDistance = d;
            }
        }
        return distance;
    }
    private List<T> points = new ArrayList<T>();

    public List<T> getPoints() {
        return points;
    }

    public Distance distance(Cluster<T> cluster) {
        Distance distance = new Distance();
        distance.distanceFromCentroid = centroidDistance(cluster);
        distance.minDistance = Double.POSITIVE_INFINITY;
        distance.maxDistance = Double.NEGATIVE_INFINITY;

        for (Iterator<T> i = getPoints().iterator(); i.hasNext() && distance.maxDistance < Double.POSITIVE_INFINITY;) {
            T aPointInThisCluster = i.next();
            for (Iterator<T> otherClusterPointIterator = cluster.getPoints().iterator(); otherClusterPointIterator.hasNext() && distance.maxDistance < Double.POSITIVE_INFINITY;) {
                T aPointInOtherCluster = otherClusterPointIterator.next();
                double d = metric.distance(aPointInThisCluster, aPointInOtherCluster);
                if (d < distance.minDistance) {
                    distance.minDistance = d;
                }
                if (d > distance.maxDistance) {
                    distance.maxDistance = d;
                }
            }
        }

        return distance;
    }

    public void addAll(Cluster<T> cluster) {
        for (T p : cluster.getPoints()) {
            addPoint(p);
        }
    }

    public static class Distance {

        private double distanceFromCentroid;
        private double minDistance;
        private double maxDistance;

        public Distance() {

        }

        public Distance(double minDistance, double maxDistance, double distanceFromCentroid) {
            this.maxDistance = maxDistance;
            this.minDistance = minDistance;
            this.distanceFromCentroid = distanceFromCentroid;
        }

        public double getDistanceFromCentroid() {
            return this.distanceFromCentroid;
        }

        public double getMinDistance() {
            return this.minDistance;
        }

        public double getMaxDistance() {
            return this.maxDistance;
        }

    }

}

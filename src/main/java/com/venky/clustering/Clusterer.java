package com.venky.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clusterer<T> {
    private Metric<T> metric;
    private CenterFinderBuilder<T> centerFinderBuilder ;
    
    private static class DefaultCenterFinderBuilder<T> implements CenterFinderBuilder<T> {
	    private CenterFinder<T> centerFinder;
    	public DefaultCenterFinderBuilder(CenterFinder<T> finder) {
    		this.centerFinder = finder;
		}
		@Override
		public CenterFinder<T> build(Cluster<T> cluster) {
			return this.centerFinder;
		}
    	
    }
    public Clusterer(CenterFinderBuilder<T> cf, Metric<T> m){
    	this.centerFinderBuilder = cf;
    	this.metric = m;
    }
    
    public Clusterer(CenterFinder<T> cf, Metric<T> m){
    	this(new DefaultCenterFinderBuilder<T>(cf),m);
    }
    public Clusterer(Metric<T> m){
    	this(new DefaultCenterFinderBuilder<T>(null),m);
    }

    public Metric<T> getMetric(){
        return metric;
    }
    
    public List<Cluster<T>> cluster(Collection<T> points, List<T> centroids){
        List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
        for (T centroid: centroids){
            Cluster<T> init = new Cluster<T>(centerFinderBuilder,metric);
            init.addPoint(centroid);
            clusters.add(init);
        }
        
        for (T p : points){
            int closestCentroidIndex = -1;
            double distanceToClosestCentroid = Double.POSITIVE_INFINITY;
            
            for (int i = 0; i < centroids.size() ; i ++ ){
                double distanceToCentroid = metric.distance(centroids.get(i),p);
                if (distanceToClosestCentroid > distanceToCentroid){
                    distanceToClosestCentroid = distanceToCentroid; 
                    closestCentroidIndex = i ;
                }
            }
            if (closestCentroidIndex >= 0 ){
                clusters.get(closestCentroidIndex).addPoint(p);
            }
        }
        return clusters;
    }
    
    public List<Cluster<T>> cluster(Collection<T> points, int maxClusters){
        return cluster(points,new MaxClustersCreated<T>(maxClusters));
    }
    
    public List<Cluster<T>> cluster(Collection<T> points,StopCriteria<T> stopCriteria){
        List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
        for (T point: points){
            Cluster<T> init = new Cluster<T>(centerFinderBuilder,metric);
            init.addPoint(point);
            clusters.add(init);
        }
        
        Map<Cluster<T>,Map<Cluster<T>,Double>> distances = new HashMap<Cluster<T>, Map<Cluster<T>,Double>>();
        Cluster<T> clusterGrown = null; 
        Cluster<T> clusterDestroyed = null;
        while (!stopCriteria.canStop(clusters,clusterGrown,clusterDestroyed)){

            int indexGrown = 0; 
            int indexDestroyed = 0; 
            double minDistance = Double.POSITIVE_INFINITY;
            for (int i = 0 ; i < clusters.size() ; i ++ ){
                Cluster<T> inspected = clusters.get(i);
                Map<Cluster<T>,Double> inspectedDistances = distances.get(inspected);
                if (inspectedDistances == null){ 
                    inspectedDistances = new HashMap<Cluster<T>, Double>();
                    distances.put(inspected, inspectedDistances);
                }
                for (int j = i +1 ; j < clusters.size() ; j ++){
                    Cluster<T> other = clusters.get(j);
                    Double distance = inspectedDistances.get(other); 
                    if (distance == null){
                        if (inspected.centroid() != null){
                            distance = inspected.centroidDistance(other);
                        }else {
                            distance = inspected.distance(other).getMaxDistance();
                        }
                        inspectedDistances.put(other, distance);
                    }
                    
                    if (minDistance >  distance){
                        minDistance = distance;
                        indexGrown = i ;
                        indexDestroyed = j ;
                    }
                }
            }
            if (minDistance == Double.POSITIVE_INFINITY){
                break; 
            }
            
            clusterGrown = clusters.get(indexGrown);
            clusterDestroyed = clusters.remove(indexDestroyed); //Note index Destroyed >= indexGrown.
            for (T p : clusterDestroyed.getPoints()){
                clusterGrown.addPoint(p);
            }
            distances.remove(clusterGrown);
            distances.remove(clusterDestroyed);
            for (Cluster<T> remaining: distances.keySet()){
                distances.get(remaining).remove(clusterGrown);
                distances.get(remaining).remove(clusterDestroyed);
            }

        }
        return clusters;
    }
}

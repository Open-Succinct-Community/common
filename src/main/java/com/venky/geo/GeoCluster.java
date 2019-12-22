package com.venky.geo;

import com.venky.clustering.CenterFinderBuilder;
import com.venky.clustering.Cluster;
import com.venky.clustering.ClusterBuilder;
import com.venky.clustering.Metric;
import com.venky.clustering.geography.GeoCentroidFinder;
import com.venky.clustering.geography.GeoMetric;
import com.venky.core.math.DoubleUtils;
import com.venky.core.math.SpreadCalculator;
import com.venky.geo.GeoCoordinate;
import com.venky.geo.GeoLocation;

import java.util.*;

public class GeoCluster extends Cluster<GeoCoordinate> {

    public GeoCluster(GeoCoordinate fixedCentroid, Metric<GeoCoordinate> m) {
        super(fixedCentroid, m);
    }

    public static final ClusterBuilder<GeoCoordinate> BUILDER = new ClusterBuilder<GeoCoordinate>() {
        @Override
        public Cluster<GeoCoordinate> init(CenterFinderBuilder<GeoCoordinate> centerFinderBuilder, Metric<GeoCoordinate> metric) {
            return new GeoCluster(centerFinderBuilder,metric);
        }

        @Override
        public Cluster<GeoCoordinate> init(GeoCoordinate fixedCentroid, Metric<GeoCoordinate> metric) {
            return new GeoCluster(fixedCentroid, metric);
        }
    };
    public GeoCluster(CenterFinderBuilder<GeoCoordinate> centerFinderBuilder,Metric<GeoCoordinate> metric) {
        super(centerFinderBuilder,metric);
    }

    @Override
    public void addAll(Cluster<GeoCoordinate> cluster) {
        GeoCluster other = (GeoCluster) cluster;
        GeoCoordinate otherFirst = other.getPoints().get(0);
        GeoCoordinate otherLast = other.getPoints().get(other.getPoints().size() - 1);

        GeoCoordinate first = getPoints().get(0);
        GeoCoordinate last = getPoints().get(getPoints().size() - 1);

        double dfl = first.distanceTo(otherLast);
        double dlf = last.distanceTo(otherFirst);

        if (dlf <= dfl) {
            for  (int i = 0 ; i <other.getPoints().size() ; i ++) {
                addPoint(other.getPoints().get(i));
            }
        } else {
            for (int i = other.getPoints().size() - 1; i >= 0; i--) {
                addPoint(other.getPoints().get(i));
            }
        }
    }

    private List<GeoCoordinate> coordinateList = new ArrayList<GeoCoordinate>() {
        public boolean add(GeoCoordinate coordinate){
            boolean ret = false;
            if (size() <= 1) {
                ret = super.add(coordinate);
            }else {
                if (get(0).distanceTo(coordinate) < get(size()-1).distanceTo(coordinate)){
                    super.add(0,coordinate);
                    ret = true;
                }else {
                    ret = super.add(coordinate);
                }
            }
            return ret;
        }
    };
    @Override
    public List<GeoCoordinate> getPoints(){
        return coordinateList;
    }

    @Override
    public Distance distance(Cluster<GeoCoordinate> cluster) {
        if (getPoints().isEmpty() || cluster.getPoints().isEmpty()) {
            return new Distance(Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }
        GeoCluster other = (GeoCluster)cluster;
        GeoCoordinate otherFirst = other.getPoints().get(0) ;
        GeoCoordinate otherLast = other.getPoints().get(other.getPoints().size()-1);

        GeoCoordinate first = getPoints().get(0);
        GeoCoordinate last = getPoints().get(getPoints().size()-1);

        double dfl = first.distanceTo(otherLast);
        double dlf = last.distanceTo(otherFirst);
        double dff = first.distanceTo(otherFirst);
        double dll = last.distanceTo(otherLast);

        SpreadCalculator<Double> calculator = new SpreadCalculator<>();
        calculator.add(dfl);
        calculator.add(dff);
        calculator.add(dlf);
        calculator.add(dll);
        return  new Distance(calculator.min(), calculator.max(), centroidDistance(cluster));
    }

    @Override
    public Distance distance(GeoCoordinate point) {
        GeoCoordinate first = getPoints().get(0);
        GeoCoordinate last = getPoints().get(getPoints().size()-1);

        double dFirst = first.distanceTo(point);
        double dLast = last.distanceTo(point);

        if (Double.isInfinite(dFirst) && Double.isInfinite(dLast)){
            return  new Distance(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
        }

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        if (dFirst < dLast){
            min = dFirst;
            max = dLast;
        }else {
            min = dLast;
            max = dFirst;
        }
        return new Distance(min,max,centroidDistance(point));

    }
}

package com.venky.geo;

import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.venky.core.util.ObjectUtil;
import com.venky.xml.XMLDocument;
import com.venky.xml.XMLElement;


public class GeoDistance {
	static final double R = 6378.1370  ; //Equitorial Radius of Earth.

	public static double distanceKms(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2 ){
		return new GeoCoder().getDrivingDistanceKms(lat1,lng1,lat2,lng2,new HashMap<>());
	}
	public static double getDrivingDistanceKms(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2, Map<String,String> params){
		return new GeoCoder().getDrivingDistanceKms(lat1,lng1,lat2,lng2,params);
	}
}

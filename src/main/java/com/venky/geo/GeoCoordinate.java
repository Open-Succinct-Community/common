package com.venky.geo;

import java.math.BigDecimal;

public class GeoCoordinate implements GeoLocation{
	private double latInRadians; 
	private double lngInRadians;
	public static final double R = 6378.1370  ; //Equitorial Radius of Earth.
	private GeoLocation inner ; 
	@Deprecated
	/**
	 * deprecated use GeoCoordinate(GeoLocation ref) ;
	 * Kept for Serialization routines.
	 */
	public GeoCoordinate(){
		
	}
	public GeoCoordinate(GeoLocation ref){
		setInner(ref);
	}
	public void setInner(GeoLocation inner){
		this.inner = inner;
		if (inner != null){ 
			this.setLat(inner.getLat());
			this.setLng(inner.getLng());
		}
	}
	@SuppressWarnings("unchecked")
	public <T extends GeoLocation> T inner(){
		return (T)inner;
	}
	
	
	public GeoCoordinate(BigDecimal lat,BigDecimal lng){
		setLat(lat);
		setLng(lng);
	}
	public GeoCoordinate(double lat,double lng){
		this(new BigDecimal(lat),new BigDecimal(lng));
	}
	public GeoCoordinate(Vector3d d3Vector){ 
		double lng = Math.atan2(d3Vector.y, d3Vector.x);
		double hyp = Math.sqrt(d3Vector.x * d3Vector.x  + d3Vector.y * d3Vector.y );
		double lat = Math.atan2(d3Vector.z , hyp);
		this.latInRadians = lat;
		this.lngInRadians = lng;
	}
	
	public BigDecimal getLat(){
		return new BigDecimal(latInRadians * 180.0/Math.PI) ;
	}
	public BigDecimal getLng(){
		return new BigDecimal(lngInRadians * 180.0/Math.PI) ;
	}
	public BigDecimal getLatInRadians(){
		return new BigDecimal(latInRadians);
	}
	public BigDecimal getLngInRadians(){
		return new BigDecimal(lngInRadians);
	}

	public void setLatInRadians(BigDecimal latInRadians){ 
		this.latInRadians = latInRadians.doubleValue();
	}
	public void setLngInRadians(BigDecimal lngInRadians){ 
		this.latInRadians = lngInRadians.doubleValue();
	}
	
	
	public Vector3d toVector(){
		double x = R * Math.cos(latInRadians)*Math.cos(lngInRadians);
		double y = R * Math.cos(latInRadians)*Math.sin(lngInRadians);
		double z = R * Math.sin(latInRadians);
		return new Vector3d(x,y,z);
	}
	
	public double distanceTo(GeoCoordinate another){
		double dlat = (latInRadians - another.latInRadians);
		double dlng = (lngInRadians - another.lngInRadians);
		
		double a =  Math.sin(dlat/2)* Math.sin(dlat/2) + Math.cos(latInRadians)*Math.cos(another.latInRadians) * Math.sin(dlng/2) * Math.sin(dlng/2);
		
		return 2 * R * Math.asin(Math.min(1, Math.sqrt(a)));
	}
	
	public double distanceTo(GeoCoordinate start, GeoCoordinate end){
		Vector3d gc = start.toVector().cross(end.toVector());
		
		
		double angleBetweenThisAndGCVector = Math.abs(this.toVector().angle(gc)); // 0 to pi
		
		
		double angleBetweenThisAndGCPlane = Math.abs((Math.PI/2) - angleBetweenThisAndGCVector) ;
		
		
		return R * angleBetweenThisAndGCPlane;
	}

	@Override
	public void setLat(BigDecimal latitude) {
		latInRadians = latitude.doubleValue() * Math.PI/180.0 ;
	}

	@Override
	public void setLng(BigDecimal longitude) {
		lngInRadians = longitude.doubleValue() * Math.PI/ 180.0;
	}
}

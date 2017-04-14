package com.venky.geo;


public class Vector3d {
	public final double x; 
	public final double y; 
	public final double z;
	public Vector3d (double x , double y , double z){
		this.x = x; 
		this.y = y; 
		this.z = z;
	}
	Double norm = null;
	public double norm(){
		if (norm == null){
			norm = Math.sqrt(x*x + y*y + z*z);
		}
		return norm ;
	}
	public double dot(Vector3d another){
		return (x*another.x + y*another.y + z *another.z); 
	}
	
	/**
	 * 
	 * @param another
	 * @return angle between this and another ( Range -Pi to Pi )
	 */
	public double angle(Vector3d another){
		Vector3d cross = cross(another);
		double rsin = cross.norm();
		double rcos = dot(another);
		return Math.atan2(rsin, rcos);
	}
	
	public Vector3d cross(Vector3d another){
		double ox = (y * another.z) - (another.y * z);
		double oy = (z * another.x) - (another.z * x);
		double oz = (x * another.y) - (another.x * y);
		return new Vector3d(ox,oy,oz);
	}
	
	public Vector3d add(Vector3d another){
		return new Vector3d(x + another.x, y + another.y, z + another.z);
	}
	
	public Vector3d multiply(double scalar){
		return new Vector3d(x*scalar,y*scalar,z*scalar);
	}
	
}

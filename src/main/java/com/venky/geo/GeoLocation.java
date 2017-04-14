package com.venky.geo;

import java.math.BigDecimal;

public interface GeoLocation {
	public BigDecimal getLat(); 
	public void setLat(BigDecimal latitude);
	
	public BigDecimal getLng();
	public void setLng(BigDecimal longitude);
}

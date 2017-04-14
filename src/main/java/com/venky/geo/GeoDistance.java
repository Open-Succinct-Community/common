package com.venky.geo;

import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;

import com.venky.core.util.ObjectUtil;
import com.venky.xml.XMLDocument;
import com.venky.xml.XMLElement;


public class GeoDistance {
	static final double R = 6378.1370  ; //Equitorial Radius of Earth.

	public static double distanceKms(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2 ){
		return new GeoCoordinate(lat1, lng1).distanceTo(new GeoCoordinate(lat2, lng2));
	}
	public static double getDrivingDistanceKms(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2){
		String url = "http://open.mapquestapi.com/directions/v1/route?outFormat=xml&unit=k&from="+lat1+","+lng1 +"&to=" + lat2 + "," + lng2 ;
		try {
			 URL u = new URL(url);
	         URLConnection connection = u.openConnection();
	         XMLDocument doc = XMLDocument.getDocumentFor(connection.getInputStream());
	         XMLElement status = doc.getDocumentRoot().getChildElement("info").getChildElement("statusCode");
	         if (ObjectUtil.equals("0",status.getNodeValue())){
	        	 XMLElement distance = doc.getDocumentRoot().getChildElement("route").getChildElement("distance");
	        	 return Double.valueOf(distance.getNodeValue()).doubleValue();
	         }
		}catch(Exception e){
			// Nothing to do.
		}
		return distanceKms(lat1, lng1, lat2, lng2);
	}
}

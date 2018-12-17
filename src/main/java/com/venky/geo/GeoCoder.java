/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venky.geo;

import com.venky.core.util.ObjectUtil;
import com.venky.xml.XMLDocument;
import com.venky.xml.XMLElement;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.Format;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author venky
 */
public class GeoCoder {
    
    private static final Map<String,GeoSP> availableSps = new HashMap<String,GeoSP>();
    static { 
    	//registerGeoSP("yahoo",new Yahoo());
    	registerGeoSP("google",new Google());
    	registerGeoSP("openstreetmap",new Nominatim());
    	registerGeoSP( "here",new Here());
    }
    
    private static void registerGeoSP(String sp,GeoSP geoSP){
    	availableSps.put(sp, geoSP);
    }

    private GeoSP preferredServiceProvider = null;
    public GeoCoder(String preferedSP){
    	preferredServiceProvider = availableSps.get(preferedSP);
    }
    public GeoCoder(){
    	this(null);
    }
    
    public void fillGeoInfo(String address,GeoLocation location,Map<String,String> params){
    	GeoLocation result = getLocation(address,params);
    	if (result != null){
	    	location.setLat(result.getLat());
	    	location.setLng(result.getLng());
    	}
    }
    
    Collection<GeoSP> sps = null ;
	public GeoLocation getLocation(String address,Map<String,String> params){
    	if (preferredServiceProvider == null ){
    		sps = Arrays.asList(availableSps.get("here"), availableSps.get("openstreetmap"), availableSps.get("google"));
    	}else {
    		sps = Arrays.asList(preferredServiceProvider); 
    	}
    	for (GeoSP sp : sps){
           GeoLocation loc = sp.getLocation(address,params);
           if (loc != null){
        	   Logger.getLogger(GeoCoder.class.getName()).info("Lat,Lon found using " + sp.getClass().getSimpleName());
        	   return loc;
           }
    	}
    	return null;
    }
    private static interface GeoSP {
    	public GeoLocation getLocation(String address,Map<String,String> params);
    }

    private static class Google implements GeoSP {
    	private static final String WSURL = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false&key=%saddress=%s";

		public GeoLocation getLocation(String address,Map<String,String> params) {
        	try {
        		String apiKey = params.get("google.api_key");
        		if (!ObjectUtil.isVoid(apiKey)) {
					String url = String.format(WSURL, apiKey, URLEncoder.encode(address, "UTF-8"));
					URL u = new URL(url);
					URLConnection connection = u.openConnection();
					XMLDocument doc = XMLDocument.getDocumentFor(connection.getInputStream());
					XMLElement status = doc.getDocumentRoot().getChildElement("status");
					if ("OK".equals(status.getNodeValue())) {
						Logger.getLogger(getClass().getName()).info("URL:" + url);
						XMLElement location = doc.getDocumentRoot().getChildElement("result").getChildElement("geometry").getChildElement("location");
						float lat = -1;
						float lng = -1;
						for (Iterator<XMLElement> nodeIterator = location.getChildElements(); nodeIterator.hasNext(); ) {
							XMLElement node = nodeIterator.next();
							if (node.getNodeName().equals("lat")) {
								lat = Float.valueOf(node.getChildren().next().getNodeValue());
							} else if (node.getNodeName().equals("lng")) {
								lng = Float.valueOf(node.getChildren().next().getNodeValue());
							}
						}
						return new GeoCoordinate(new BigDecimal(lat), new BigDecimal(lng));
					}
				}
	        } catch (IOException e) {
	           Logger.getLogger(getClass().getName()).warning(e.getMessage());
	        }
        	return null;
		}
    	
    }
    private static class Nominatim implements GeoSP {
    	private static final String WSURL = "http://nominatim.openstreetmap.org/search?format=xml&polygon=0&q=%s";
		public GeoLocation getLocation(String address,Map<String,String> params) {
			try {
	            String url = String.format(WSURL ,URLEncoder.encode(address,"UTF-8"));
	            URL u = new URL(url);
	            URLConnection connection = u.openConnection();
	            XMLDocument doc = XMLDocument.getDocumentFor(connection.getInputStream());
	            XMLElement place = doc.getDocumentRoot().getChildElement("place");
	            
	            if (place != null){
	            	Logger.getLogger(getClass().getName()).info("URL:" + url);
	                return new GeoCoordinate(new BigDecimal(place.getAttribute("lat")),new BigDecimal(place.getAttribute("lon")));
	            }
	        } catch (IOException e) {
	           Logger.getLogger(getClass().getName()).warning(e.getMessage());
	        }
        	return null;		
    	}
    }

	private static class Here implements GeoSP {
		private static final String WSURL = "https://geocoder.api.here.com/6.2/geocode.json?app_id=%s&app_code=%s&searchtext=%s";
		public GeoLocation getLocation(String address,Map<String,String> params) {
			try {
				String appId = params.get("here.app_id");
				String appCode = params.get("here.app_code");
				if (!ObjectUtil.isVoid(appCode) && !ObjectUtil.isVoid(appId)){
					String url = String.format(WSURL ,appId, appCode, URLEncoder.encode(address,"UTF-8"));
					URL u = new URL(url);
					URLConnection connection = u.openConnection();
					XMLDocument doc = XMLDocument.getDocumentFor(connection.getInputStream());
					XMLElement place = doc.getDocumentRoot().getChildElement("place");

					if (place != null){
						Logger.getLogger(getClass().getName()).info("URL:" + url);
						return new GeoCoordinate(new BigDecimal(place.getAttribute("lat")),new BigDecimal(place.getAttribute("lon")));
					}
				}
			} catch (IOException e) {
				Logger.getLogger(getClass().getName()).warning(e.getMessage());
			}
			return null;
		}
	}
}

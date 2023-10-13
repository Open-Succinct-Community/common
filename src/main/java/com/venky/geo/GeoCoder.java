/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venky.geo;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.xml.XMLDocument;
import com.venky.xml.XMLElement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;
import org.w3c.dom.DOMException;

/**
 *
 * @author venky
 */
public class GeoCoder {

    private static final Map<String, GeoSP> availableSps = new HashMap<String, GeoSP>();

    static {
        //registerGeoSP("yahoo",new Yahoo());
        registerGeoSP("google", new Google());
        registerGeoSP("openstreetmap", new Nominatim());
        registerGeoSP("here", new Here());
    }

    private static void registerGeoSP(String sp, GeoSP geoSP) {
        availableSps.put(sp, geoSP);
    }

    private GeoSP preferredServiceProvider = null;

    public GeoCoder(String preferedSP) {
        preferredServiceProvider = availableSps.get(preferedSP);
    }

    public GeoCoder() {
        this(null);
    }

    public void fillGeoInfo(String address, GeoLocation location, Map<String, String> params) {
        GeoLocation result = getLocation(address, params);
        if (result != null) {
            location.setLat(result.getLat());
            location.setLng(result.getLng());
        }
    }

    Collection<GeoSP> sps = null;
    public boolean isEnabled(Map<String,String> params){
        if (preferredServiceProvider != null){
            return preferredServiceProvider.isEnabled(params);
        }else {
            for (String spName: availableSps.keySet()){
                GeoSP sp = availableSps.get(spName);
                if (sp != null && sp.isEnabled(params)){
                    return true;
                }
            }
            return false;
        }
    }
    public Collection<GeoSP> getSps(){
        loadSps();
        return sps;
    }
    private void loadSps(){
        if (sps != null){
            return;
        }
        if (preferredServiceProvider == null) {
            sps = Arrays.asList(availableSps.get("here"), availableSps.get("openstreetmap"), availableSps.get("google"));
        } else {
            sps = Arrays.asList(preferredServiceProvider);
        }
    }
    public GeoLocation getLocation(String address, Map<String, String> params) {
        for (GeoSP sp : getSps()) {
            GeoLocation loc = sp.getLocation(address, params);
            if (loc != null) {
                Logger.getLogger(GeoCoder.class.getName()).info("Lat,Lon found using " + sp.getClass().getSimpleName());
                return loc;
            }
        }
        return null;
    }
    public GeoAddress getAddress(GeoLocation gps, Map<String, String> params) {
        for (GeoSP sp : getSps()) {
            GeoAddress loc = sp.getAddress(gps, params);
            if (loc != null) {
                Logger.getLogger(GeoCoder.class.getName()).info("Lat,Lon found using " + sp.getClass().getSimpleName());
                return loc;
            }
        }
        return null;
    }

    public Double getDrivingDistanceKms(GeoLocation l1, GeoLocation l2, Map<String,String> params){
        return getDrivingDistanceKms(l1.getLat(),l1.getLng(),l2.getLat(),l2.getLng(),params);
    }
    public Double getDrivingDistanceKms(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2, Map<String,String> params){

        for (GeoSP sp :getSps()){
            Double distance = sp.getDrivingDistance(lat1,lng1,lat2,lng2,params);
            if (distance != null){
                Logger.getLogger(GeoCoder.class.getName()).info("Distance found using " + sp.getClass().getSimpleName());
                return distance;
            }
        }
        return null;
    }
    public Double distanceKms(GeoLocation l1, GeoLocation l2){
        return new GeoCoordinate(l1).distanceTo(new GeoCoordinate(l2));
    }

    private static interface GeoSP {

        public GeoLocation getLocation(String address, Map<String, String> params);

        public GeoAddress getAddress(GeoLocation geoLocation, Map<String, String> params);

        public boolean isEnabled(Map<String, String> params);

        default Double getDrivingDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2, Map<String, String> params){
            return distance(lat1,lng1,lat2,lng2);
        }
        public static Double distance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2){
            return new GeoCoordinate(lat1, lng1).distanceTo(new GeoCoordinate(lat2, lng2));
        }


    }

    public static class GeoAddress {

        String city;
        String state;
        String country;

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getCountry() {
            return country;
        }
    }

    private static class Google implements GeoSP {

        private static final String WSURL = "https://maps.googleapis.com/maps/api/geocode/xml?sensor=false&key=%s&address=%s";
        private static final String REVERSE_GEOCODE_URL = 
                "https://maps.googleapis.com/maps/api/geocode/xml?latlng=%f,%f&key=%s";

        @Override
        public boolean isEnabled(Map<String, String> params) {
            return !ObjectUtil.isVoid(params.get("google.api_key"));
        }
        public GeoLocation getLocation(String address, Map<String, String> params) {
            try {
                String apiKey = params.get("google.api_key");
                if (!ObjectUtil.isVoid(apiKey)) {
                    String url = String.format(WSURL, apiKey, URLEncoder.encode(address, "UTF-8"));
                    URL u = new URL(url);
                    URLConnection connection = u.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    XMLDocument doc = XMLDocument.getDocumentFor(connection.getInputStream());
                    XMLElement status = doc.getDocumentRoot().getChildElement("status");
                    if ("OK".equals(status.getNodeValue())) {
                        Logger.getLogger(getClass().getName()).info("URL:" + url);
                        XMLElement location = doc.getDocumentRoot().getChildElement("result").getChildElement("geometry").getChildElement("location");
                        float lat = -1;
                        float lng = -1;
                        for (Iterator<XMLElement> nodeIterator = location.getChildElements(); nodeIterator.hasNext();) {
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

        @Override
        public GeoAddress getAddress(GeoLocation geoLocation, Map<String, String> params) {
            try {
                String apiKey = params.get("google.api_key");
                if (!ObjectUtil.isVoid(apiKey)) {
                    String url = String.format(REVERSE_GEOCODE_URL, geoLocation.getLat().floatValue(), geoLocation.getLng().floatValue(),apiKey);
                    URL u = new URL(url);
                    URLConnection connection = u.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    XMLDocument doc = XMLDocument.getDocumentFor(connection.getInputStream());
                    XMLElement status = doc.getDocumentRoot().getChildElement("status");
                    if ("OK".equals(status.getNodeValue())) {
                        Logger.getLogger(getClass().getName()).info("URL:" + url);
                        Iterator<XMLElement> li = doc.getDocumentRoot().getChildElement("result").getChildElements("address_component");
                        GeoAddress address = new GeoAddress();
                        while (li.hasNext()){
                            XMLElement addressComponent = li.next();
                            String longName = addressComponent.getChildElement("long_name").getNodeValue();
                            Iterator<XMLElement> typeIterator = addressComponent.getChildElements("type");
                            Set<String> types = new HashSet<String>();
                            while (typeIterator.hasNext()){
                                XMLElement typeElement  = typeIterator.next();
                                String type = typeElement.getNodeValue();
                                types.add(type);
                            }
                            if (types.contains("country")){
                                address.country = longName;
                            }else if (types.contains("administrative_area_level_1")){
                                address.state = longName;
                            }else if (types.contains("locality")){
                                address.city = longName;
                            }
                        }
                        return address;
                    }
                }
            } catch (IOException e) {
                Logger.getLogger(getClass().getName()).warning(e.getMessage());
            }
            return null;
        }


    }

    private static class Nominatim implements GeoSP {

        private static final String WSURL = "https://nominatim.openstreetmap.org/search?format=xml&polygon=0&q=%s";
        private static final String REVERSE_GEO_CODE_URL
                = "https://nominatim.openstreetmap.org/reverse?format=xml&lat=%f&lon=%f&zoom=10&addressdetails=1";

        public GeoLocation getLocation(String address, Map<String, String> params) {
            try {
                String url = String.format(WSURL, URLEncoder.encode(address, "UTF-8"));
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");

                boolean redirect = false;

                // normally, 3xx is redirect
                int status = conn.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER) {
                        redirect = true;
                    }
                }

                if (redirect) {
                    String newUrl = conn.getHeaderField("Location");

                    // get the cookie if need, for login
                    String cookies = conn.getHeaderField("Set-Cookie");

                    // open the new connnection again
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    conn.setRequestProperty("Cookie", cookies);
                    conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                    conn.addRequestProperty("User-Agent", "Mozilla");
                }
                XMLDocument doc = XMLDocument.getDocumentFor(conn.getInputStream());
                XMLElement place = doc.getDocumentRoot().getChildElement("place");

                if (place != null) {
                    Logger.getLogger(getClass().getName()).info("URL:" + url);
                    return new GeoCoordinate(new BigDecimal(place.getAttribute("lat")), new BigDecimal(place.getAttribute("lon")));
                }
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).warning(e.getMessage());
            } finally {
                try {
                    Thread.sleep(1000); //To ensure nominatim response code 429 doesnot happen.
                } catch (InterruptedException ex) {
                    //
                }
            }
            return null;
        }

        @Override
        public GeoAddress getAddress(GeoLocation location, Map<String, String> params) {
            GeoAddress address = null;
            String url = String.format(REVERSE_GEO_CODE_URL, location.getLat().floatValue(), location.getLng().floatValue());
            try {
                URL u = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                XMLDocument doc = XMLDocument.getDocumentFor(conn.getInputStream());
                XMLElement addressparts = doc.getDocumentRoot().getChildElement("addressparts");

                if (addressparts != null) {
                    address = new GeoAddress();
                    address.city = addressparts.getChildElement("city").getNodeValue();
                    address.state = addressparts.getChildElement("state").getNodeValue();
                    address.country = addressparts.getChildElement("country").getNodeValue();
                }
                return address;
            } catch (IOException | DOMException ex) {
                return null;
            }

        }

        @Override
        public boolean isEnabled(Map<String, String> params) {
            return true;
        }
    }

    private static class Here implements GeoSP {
        @Override
        public boolean isEnabled(Map<String, String> params) {
            return getGeoCodingUrl("",params) != null;
        }

        private String getGeoCodingUrl(String address, Map<String,String> params){
            try {
                String appKey = params.get("here.app_key");
                String appId = params.get("here.app_id");
                String appCode = params.get("here.app_code");
                if (!ObjectUtil.isVoid(appKey)){
                    return String.format("https://geocoder.ls.hereapi.com/6.2/geocode.json?apiKey=%s&searchtext=%s",
                            URLEncoder.encode(appKey,"UTF-8"),
                            URLEncoder.encode(address,"UTF-8"));
                }else if (!ObjectUtil.isVoid(appCode)) {
                    return String.format("https://geocoder.api.here.com/6.2/geocode.json?app_id=%s&app_code=%s&searchtext=%s",
                            URLEncoder.encode(appId,"UTF-8"),
                            URLEncoder.encode(appCode,"UTF-8"),
                            URLEncoder.encode(address,"UTF-8"));
                }else {
                    return null;
                }
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }

        public GeoLocation getLocation(String address, Map<String, String> params) {
            try {
                String url = getGeoCodingUrl(address, params);
                
                if (!ObjectUtil.isVoid(url)) {
                    URL u = new URL(url);
                    URLConnection connection = u.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    JSONObject doc = (JSONObject) JSONValue.parseWithException(new InputStreamReader(connection.getInputStream()));
                    JSONObject place = (JSONObject) doc.get("Response");
                    JSONArray views = (JSONArray) place.get("View");
                    JSONObject position = null;

                    if (views != null && !views.isEmpty()) {
                        JSONObject view = (JSONObject) views.get(0);
                        JSONObject location = (JSONObject) ((JSONObject) ((JSONArray) view.get("Result")).get(0)).get("Location");
                        position = (JSONObject) location.get("DisplayPosition");
                    }

                    if (position != null) {
                        Logger.getLogger(getClass().getName()).info("URL:" + url);
                        return new GeoCoordinate(new BigDecimal((Double) position.get("Latitude")), new BigDecimal((Double) position.get("Longitude")));
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(getClass().getName()).warning(e.getMessage());
            }
            return null;
        }

        private String getReverseGeoCodeUrl(GeoLocation geoLocation, Map<String,String> params) throws UnsupportedEncodingException{
            String appKey = params.get("here.app_key");                
            String appId = params.get("here.app_id");
            String appCode = params.get("here.app_code");
            if (!ObjectUtil.isVoid(appKey)){
                return String.format("https://reverse.geocoder.ls.hereapi.com/6.2/reversegeocode.json?prox=%f,%f"
                                                + "&mode=retrieveLandmarks&maxresults=1&gen=9&apiKey=%s",
                        geoLocation.getLat().floatValue(), geoLocation.getLng().floatValue(),URLEncoder.encode(appKey,"UTF-8"));
            }else if (!ObjectUtil.isVoid(appCode)) {
                return String.format("https://reverse.geocoder.api.here.com/6.2/reversegeocode.json?prox=%f,%f"
                        + "&mode=retrieveLandmarks&maxresults=1&gen=9&app_id=%s&app_code=%s",
                        geoLocation.getLat().floatValue(), geoLocation.getLng().floatValue(),
                        URLEncoder.encode(appId,"UTF-8"),
                        URLEncoder.encode(appCode,"UTF-8"));
            }else {
                    return null;
            }
            
        }
        
        
        @Override
        public GeoAddress getAddress(GeoLocation geoLocation, Map<String, String> params) {
            GeoAddress address = null;
            try {
                String url = getReverseGeoCodeUrl(geoLocation, params);
                if (!ObjectUtil.isVoid(url)) {
                    URL u = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                    conn.addRequestProperty("User-Agent", "Mozilla");
                    JSONObject doc = (JSONObject) JSONValue.parseWithException(new InputStreamReader(conn.getInputStream()));
                    JSONObject place = (JSONObject) doc.get("Response");
                    JSONArray views = (JSONArray) place.get("View");
                    JSONObject addressparts = null;
                    if (views != null && !views.isEmpty()) {
                        JSONObject view = (JSONObject) views.get(0);
                        JSONObject location = (JSONObject) ((JSONObject) ((JSONArray) view.get("Result")).get(0)).get("Location");
                        addressparts = (JSONObject) location.get("Address");
                    }

                    
                    if (addressparts != null) {
                        address = new GeoAddress();
                        address.city = StringUtil.valueOf(addressparts.get("City"));
                        address.state = StringUtil.valueOf(addressparts.get("State"));
                        address.country = StringUtil.valueOf(addressparts.get("Country"));
                    }
                    return address;
                }
            } catch (IOException | DOMException | ParseException ex) {
                
            }
            return null;
        }

        @Override
        public Double getDrivingDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2, Map<String, String> params)  {
            String appKey = params.get("here.app_key");
            /*
            curl 'https://router.hereapi.com/v8/routes?transportMode=car&origin=12.902987,77.599674&destination=13.198533,77.707956&return=summary&apiKey=4Su9WUwZJ7_dvMn1uDJDs0IoSgDltWlI6Efm4CPnfBM'

                {"routes":[{"id":"8ac052c1-b446-403e-b867-65744a5dc8e5","sections":[{"id":"6692dcb0-2b75-4ad7-b9d0-983cf0e1176d","type":"vehicle","departure":{"place":{"type":"place","location":{"lat":12.9029757,"lng":77.6000587},"originalLocation":{"lat":12.902987,"lng":77.599674}}},"arrival":{"place":{"type":"place","location":{"lat":13.19847,"lng":77.7079559},"originalLocation":{"lat":13.198533,"lng":77.7079559}}},"summary":{"duration":5353,"length":43380,"baseDuration":4055},"transport":{"mode":"car"}}]}]}
             */

            if (!ObjectUtil.isVoid(appKey)) {
                try {
                    String url = String.format("https://router.hereapi.com/v8/routes?transportMode=%s&apiKey=%s&origin=%f,%f&destination=%f,%f&return=summary",
                            URLEncoder.encode(params.getOrDefault("transportMode", "car"), StandardCharsets.UTF_8),
                            URLEncoder.encode(appKey, StandardCharsets.UTF_8),
                            lat1.floatValue(), lng1.floatValue(),
                            lat2.floatValue(), lng2.floatValue());

                    URL u = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                    conn.addRequestProperty("User-Agent", "Mozilla");
                    JSONObject out = (JSONObject) JSONValue.parseWithException(new InputStreamReader(conn.getInputStream()));
                    JSONArray routes = (JSONArray)out.get("routes");
                    JSONObject route = (JSONObject)routes.get(0);
                    JSONArray sections = (JSONArray)route.get("sections");
                    JSONObject section = (JSONObject)sections.get(0);
                    JSONObject summary = (JSONObject)section.get("summary");
                    Object length = summary.get("length");
                    if (length instanceof  Number){
                        return ((Number)length).doubleValue()/1000.0;
                    }
                } catch (Exception ex) {
                    //
                }
            }
            return GeoSP.distance(lat1, lng1, lat2, lng2);
        }
    }
}

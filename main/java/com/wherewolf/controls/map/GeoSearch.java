package com.wherewolf.controls.map;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Greg on 9/28/15.
 */
public class GeoSearch
{
    public static String MapboxBaseURL = "https://api.mapbox.com/v4/geocode/mapbox.places/";
    public static String MapboxaccessToken = "&access_token=pk.eyJ1IjoiZ2Vpc21hbiIsImEiOiJjaWVpb3NveWgwMG9kc3ZrZ3FxZ2pyNzJpIn0.Zhw4A_KoSlxWaC3-qM8gMA";

    public static String FoursquareBaseURL = "https://api.foursquare.com/v2/venues/search";
    public static String FoursquareClientID = "AVXLCKWFYY3X0HCTU2TXILVNSDUUYWRUKFEVBKT3HURASBVI";
    public static String FoursquareClientSecret = "AZ4A2KHDGLUIZVOH4TQ2ZSZY13OWX243MUVAXZKT4KOWX0FV";

    private static String geocodeURL;
    private static JSONObject geoJSONObject;
    private static JSONArray venueArray;
    private static JSONArray addressArray;

    public static HashMap<String, GeoPoint> addressHash;
    public static HashMap<String, GeoPoint> venueHash;

    public static HashMap<GeoPoint, String> addressLocHash;
    public static HashMap<GeoPoint, String> venueNameHash;
    public static HashMap<GeoPoint, String> venueAddressHash;

    public static GeoPoint primaryVenuePoint;
    public static GeoPoint primaryAddressPoint;
    public static String primaryAddress = "";
    public static String primaryVenueAddress = "";
    public static String primaryVenueName = "";

    public static double defaultLat = MapView.mapCenter.Latitude + 0.0001;
    public static double defaultLng = MapView.mapCenter.Longitude + 0.0001;



    public static String BuildMapboxURL(String query, double x, double y)
    {
        StringBuilder geoCodeURL = new StringBuilder(256);
        geoCodeURL.append(MapboxBaseURL);
        geoCodeURL.append(query.replace(" ", "+"));
        geoCodeURL.append(".json?proximity=");
        geoCodeURL.append(x);
        geoCodeURL.append(",");
        geoCodeURL.append(y);
        geoCodeURL.append(MapboxaccessToken);

        return geoCodeURL.toString();
    }

    public static JSONArray parseMapboxJSON(JSONObject json){
        try {
            JSONArray addressArray = json.getJSONArray("features");
//                    .getJSONObject(i)
//                    .optString("place_name");
//            JSONArray venueArray = json.getJSONObject("response")
//                    .getJSONArray("venues");
////                    .getJSONObject(0);
            return addressArray;

        } catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> addressListBuilder(Context context, String searchFor) {
        double primaryAddressLat = defaultLat;
        double primaryAddressLng = defaultLng;
        double markerLat = defaultLat;
        double markerLng = defaultLng;
        double d3 = markerLat;
        float markerLatErrorFloat = (float) d3;
        double d4 = markerLng;
        float markerLngErrorFloat = (float) d4;


        List<String> addressList = new ArrayList<String>();
        try {
            geocodeURL = BuildMapboxURL(searchFor, MapView.mapCenter.Longitude, MapView.mapCenter.Latitude);
            geoJSONObject = readJsonFromUrl(geocodeURL);
            addressArray = parseMapboxJSON(geoJSONObject);

            for (int i = 0; i<addressArray.length();  i++) {
                String address = "";
                JSONObject currentObject = addressArray.getJSONObject(i);
                if (currentObject.has("place_name")) {
                    address = currentObject
                            .optString("place_name");
                }

                if (currentObject.has("geometry")) {
                    markerLng = currentObject
                            .getJSONObject("geometry")
                            .getJSONArray("coordinates")
                            .optDouble(0, defaultLng);
                    markerLat = currentObject
                            .getJSONObject("geometry")
                            .getJSONArray("coordinates")
                            .optDouble(1, defaultLat);
                }

                addressList.add(address);

                double d1 = markerLat;
                float markerLatFloat = (float) d1;
                double d2 = markerLng;
                float markerLngFloat = (float) d2;
                GeoPoint searchMarkerGeoPoint = new GeoPoint(markerLatFloat, markerLngFloat);
                if (address != null && searchMarkerGeoPoint != null) {
                    addressHash.put(address, searchMarkerGeoPoint);
                }


            }
            // Getting 1st most relevant search response item
            if (addressArray.getJSONObject(0)
                    .getJSONObject("geometry")
                    .has("coordinates"))

            // Mapbox does lng before lat >:(
            {
                primaryAddressLat = addressArray.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")
                        .optDouble(1, defaultLat);
                primaryAddressLng = addressArray.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates")
                        .optDouble(0, defaultLng);
            }

            if (addressArray.getJSONObject(0)
                    .has("place_name")) {
                primaryAddress = addressArray
                        .getJSONObject(0)
                        .optString("place_name", "");
            }

            // Converting doubles to floats again
            double d7 = primaryAddressLat;
            float primaryAddressLatFloat = (float) d7;
            double d8 = primaryAddressLng;
            float primaryAddressLngFloat = (float) d8;
            primaryAddressPoint = new GeoPoint(primaryAddressLatFloat, primaryAddressLngFloat);
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return addressList;
    }

    public static String BuildFoursquareURL(String query, double x, double y)
    {
        StringBuilder geoCodeURL = new StringBuilder(256);
        geoCodeURL.append(FoursquareBaseURL);
        geoCodeURL.append("?client_id=");
        geoCodeURL.append(FoursquareClientID);
        geoCodeURL.append("&client_secret=");
        geoCodeURL.append(FoursquareClientSecret);
        geoCodeURL.append("&ll=");
        geoCodeURL.append(x);
        geoCodeURL.append(",");
        geoCodeURL.append(y);
        geoCodeURL.append("&query=");
        geoCodeURL.append(query.replace(" ", "+"));
        geoCodeURL.append("&v=20140806");
        geoCodeURL.append("&m=foursquare");
        return geoCodeURL.toString();
    }

    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        }
        finally {
            is.close();
        }
    }

    public static JSONArray parseFoursquareJSON(JSONObject json){
        try {
            JSONArray venueArray = json.getJSONObject("response")
                    .getJSONArray("venues");
            return venueArray;

        } catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static GeoPoint getFoursquareLatLong(Context context, String query){
        double markerLat = defaultLat;
        double markerLng = defaultLng;
        double d3 = markerLat;
        float markerLatErrorFloat = (float) d3;
        double d4 = markerLng;
        float markerLngErrorFloat = (float) d4;
        GeoPoint searchMarkerGeoErrorPoint = new GeoPoint(markerLatErrorFloat, markerLngErrorFloat);

        try {
            geocodeURL = BuildFoursquareURL(query, MapView.mapCenter.Latitude, MapView.mapCenter.Longitude);
            try {
                geoJSONObject = readJsonFromUrl(geocodeURL);
            }catch (IOException ex){
                ex.printStackTrace();
            }

            if (geoJSONObject.has("response") &&
                    geoJSONObject.getJSONObject("response")
                    .getJSONArray("venues")
                    .getJSONObject(0)
                    .getJSONObject("location")
                    .has("lat")) {
                markerLat = geoJSONObject.getJSONObject("response")
                        .getJSONArray("venues")
                        .getJSONObject(0)
                        .getJSONObject("location")
                        .optDouble("lat");
            }

            if (geoJSONObject.has("response") &&
                    geoJSONObject.getJSONObject("response").getJSONArray("venues")
                    .getJSONObject(0)
                    .getJSONObject("location")
                    .has("lng")) {
                markerLng = geoJSONObject.getJSONObject("response")
                        .getJSONArray("venues")
                        .getJSONObject(0)
                        .getJSONObject("location")
                        .optDouble("lng");
            }

            double d1 = markerLat;
            float markerLatFloat = (float) d1;
            double d2 = markerLng;
            float markerLngFloat = (float) d2;
            GeoPoint searchMarkerGeoPoint = new GeoPoint(markerLatFloat, markerLngFloat);

            return searchMarkerGeoPoint;

        } catch (JSONException e)
        {
            e.printStackTrace();
            return searchMarkerGeoErrorPoint;
        }
    }

    public static List<String> venueListBuilder(Context context, String searchFor) {

        double markerLat = defaultLat;
        double markerLng = defaultLng;
        double primaryVenueLat = defaultLat;
        double primaryVenueLng = defaultLng;
        double d3 = markerLat;
        float markerLatErrorFloat = (float) d3;
        double d4 = markerLng;
        float markerLngErrorFloat = (float) d4;
        GeoPoint searchMarkerGeoErrorPoint = new GeoPoint(markerLatErrorFloat, markerLngErrorFloat);
        venueHash = new HashMap<String, GeoPoint>();
        List<String> venueList = new ArrayList<String>();
        primaryVenuePoint = new GeoPoint(markerLatErrorFloat, markerLngErrorFloat);
        venueNameHash = new HashMap<GeoPoint, String>();
        venueAddressHash = new HashMap<GeoPoint, String>();


        try {
            geocodeURL = BuildFoursquareURL(searchFor, MapView.mapCenter.Latitude, MapView.mapCenter.Longitude);
            geoJSONObject = readJsonFromUrl(geocodeURL);
            venueArray = parseFoursquareJSON(geoJSONObject);

            for (int i = 0; i<venueArray.length();  i++) {
                String venueName = "";
                String venueAddress = "";
                String venueCity = "";
                String venueState = "";
                JSONObject currentObject = venueArray.getJSONObject(i);
                if (currentObject.has("name")) {
                    venueName = currentObject
                            .optString("name", "");
                }
                if (currentObject
                        .getJSONObject("location")
                        .has("address")) {
                    venueAddress = currentObject
                            .getJSONObject("location")
                            .optString("address");
                }
                if (currentObject
                        .getJSONObject("location")
                        .has("city")) {
                    venueCity = currentObject
                            .getJSONObject("location")
                            .optString("city");
                }
                if (currentObject
                        .getJSONObject("location")
                        .has("state")) {
                    venueState = currentObject
                            .getJSONObject("location")
                            .optString("state");
                }

                if (currentObject
                        .getJSONObject("location")
                        .has("lat")) {
                    markerLat = currentObject
                            .getJSONObject("location")
                            .optDouble("lat", defaultLat);
                }

                if (currentObject.getJSONObject("location").has("lng")) {
                    markerLng = currentObject
                            .getJSONObject("location")
                            .optDouble("lng", defaultLng);
                }

                StringBuilder buildListLine = new StringBuilder(256);
                buildListLine.append(venueName);
                buildListLine.append("\n");
                buildListLine.append(venueAddress);
                if (venueAddress != ""){
                    buildListLine.append(", ");
                }
                buildListLine.append(venueCity);
                if (venueCity != ""){
                    buildListLine.append(", ");
                }
                buildListLine.append(venueState);
                String listLine = buildListLine.toString();

                venueList.add(listLine);


                double d1 = markerLat;
                float markerLatFloat = (float) d1;
                double d2 = markerLng;
                float markerLngFloat = (float) d2;
                GeoPoint searchMarkerGeoPoint = new GeoPoint(markerLatFloat, markerLngFloat);
                if (listLine != null && searchMarkerGeoPoint != null) {
                    venueHash.put(listLine, searchMarkerGeoPoint);
                    if (venueName != null)
                    {
                        venueNameHash.put(searchMarkerGeoPoint, venueName);
                    }
                    if (venueAddress != null)
                    {
                        if (venueAddress != "")
                        {
                            venueAddressHash.put(searchMarkerGeoPoint, venueAddress + ", " + venueCity);
                        }
                        else
                        {
                            venueAddressHash.put(searchMarkerGeoPoint, venueCity);
                        }
                    }
                }
            }

            if (venueArray.getJSONObject(0)
                    .getJSONObject("location")
                    .has("lat")) {
                primaryVenueLat = venueArray.getJSONObject(0)
                        .getJSONObject("location")
                        .optDouble("lat", defaultLat);
            }

            if (venueArray.getJSONObject(0)
                    .getJSONObject("location")
                    .has("lng")) {
                primaryVenueLng = venueArray.getJSONObject(0)
                        .getJSONObject("location")
                        .optDouble("lng", defaultLng);
            }

            if (venueArray.getJSONObject(0)
                    .has("name")) {
                primaryVenueName = venueArray.getJSONObject(0)
                        .optString("name","");
            }
            if (venueArray.getJSONObject(0)
                    .getJSONObject("location")
                    .has("address")) {
                primaryVenueAddress = venueArray.getJSONObject(0)
                        .getJSONObject("location")
                        .optString("address", "");
            }

            double d5 = primaryVenueLat;
            float primaryVenueLatFloat = (float) d5;
            double d6 = primaryVenueLng;
            float primaryVenueLngFloat = (float) d6;
            primaryVenuePoint = new GeoPoint(primaryVenueLatFloat, primaryVenueLngFloat);

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return venueList;

    }

}

package looprac.app;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by XiaoYong on 2014/6/19.
 */
public class MapClient {

    private static MapClient googleMapClient;

    public static MapClient create() {
        if (googleMapClient == null) {
            googleMapClient = new MapClient();
        }
        return googleMapClient;
    }

    public MyLocation getLocDes(double latitude, double longitude) {
        MyLocation location = null;
        try {
            String apiUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&sensor=true_or_false";
            HttpGet locDesRequest = new HttpGet(apiUrl);
            HttpResponse locDesResponse = new DefaultHttpClient().execute(locDesRequest);
            JSONObject firstLocResult = new JSONObject(EntityUtils.toString(locDesResponse.getEntity())).getJSONArray("results").getJSONObject(0);
            JSONArray locDesArray = firstLocResult.getJSONArray("address_components");
            /* Get city name */
            for (int i = 0; i < locDesArray.length(); i++) {
                JSONObject locDesJSON = locDesArray.getJSONObject(i);
                if (locDesJSON.getJSONArray("types").get(0).toString().compareTo("locality") == 0) {
                    location = new MyLocation(firstLocResult.getString("formatted_address"), locDesJSON.getString("short_name"), latitude, longitude);
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return location; // A valid location or null
    }

    public ArrayList<String> getSearchResultsList(String queryInput) {
        ArrayList<String> searchResultsList = null;
        try {
            String apiUrl = "https://maps.googleapis.com/maps/api/place/queryautocomplete/json?key=" + ConstantVal.APIKEY + "&sensor=false&input=" + queryInput.replace(' ', '+');
            HttpGet searchRequest = new HttpGet(apiUrl);
            HttpResponse searchResponse = new DefaultHttpClient().execute(searchRequest);
            JSONArray searchResult = new JSONObject(EntityUtils.toString(searchResponse.getEntity())).getJSONArray("predictions");
            searchResultsList = new ArrayList<String>();
            for (int i = 0; i < searchResult.length(); i++) {
                searchResultsList.add(searchResult.getJSONObject(i).getString("description"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return searchResultsList; // A valid list with multiple search suggestions or null
    }

    public MyLocation getLocData(String addressStr) {
        MyLocation location = null;
        try {
            String apiUrl = "http://maps.googleapis.com/maps/api/geocode/json?address=" + addressStr.replace(' ', '+') + "&sensor=true";
            HttpGet locLatRequest = new HttpGet(apiUrl);
            HttpResponse locLatResponse = new DefaultHttpClient().execute(locLatRequest);
            JSONObject firstLocResult = new JSONObject(EntityUtils.toString(locLatResponse.getEntity())).getJSONArray("results").getJSONObject(0);
            JSONObject latJSON = firstLocResult.getJSONObject("geometry").getJSONObject("location");
            JSONArray locDesArray = firstLocResult.getJSONArray("address_components");
            /* Get city name */
            for (int i = 0; i < locDesArray.length(); i++) {
                JSONObject locDesJSON = locDesArray.getJSONObject(i);
                if (locDesJSON.getJSONArray("types").get(0).toString().compareTo("locality") == 0) {
                    location = new MyLocation(addressStr, locDesJSON.getString("short_name"), latJSON.getDouble("lat"), latJSON.getDouble("lng"));
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return location; // A valid location or null
    }
}

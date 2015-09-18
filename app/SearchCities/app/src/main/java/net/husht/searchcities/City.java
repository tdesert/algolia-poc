package net.husht.searchcities;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tom on 9/16/15.
 */
public class City {
    private static final String TAG = "City";
    
    private String mObjectID;
    private String mName;
    private String mCountry;
    private int mPopulation;
    private String mTimezone;
    private Location mLocation;
    private JSONObject mJsonObject;

    public City(JSONObject jsonObject) {
        try {
            mObjectID = jsonObject.getString("objectID");
            mName = jsonObject.getString("name");
            mCountry = jsonObject.getString("country");
            mPopulation = jsonObject.getInt("population");
            mTimezone = jsonObject.getString("timezone");

            //Convert _geoloc field to Location object
            JSONObject geoloc = jsonObject.getJSONObject("_geoloc");
            mLocation = new Location("");
            mLocation.setLatitude(geoloc.getDouble("lat"));
            mLocation.setLongitude(geoloc.getDouble("lng"));
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }

        mJsonObject = jsonObject;
    }
    
    public String getObjectID() {
        return mObjectID;
    }

    public String getName() {
        return mName;
    }

    public String getCountry() {
        return mCountry;
    }

    public int getPopulation() {
        return mPopulation;
    }

    public String getTimezone() {
        return mTimezone;
    }

    public Location getLocation() {
        return mLocation;
    }

    //Return highlighted property as a HTML string
    public String getHighlightedProperty(String property) {
        String highlightString = null;
        try {
            JSONObject highlight = mJsonObject.getJSONObject("_highlightResult");
            if (!highlight.has(property)) {
                highlightString = mJsonObject.getString(property);
            } else {
                highlightString = highlight.getJSONObject(property).getString("value");
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return highlightString;
    }

    //Return estimated distance as a formated string (XX km)
    public String getFormattedDistance(GoogleApiClient googleApiClient) {
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(getLocation()) / 1000;
            return String.format("%.2f", distance) + " km";
        }
        return "N/A";
    }

    public String getFormatedLocation() {
        return String.format("%f, %f", mLocation.getLatitude(), mLocation.getLongitude());
    }
}

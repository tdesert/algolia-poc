package net.husht.searchcities;

import android.content.Context;
import android.location.Location;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.Filter;
import android.widget.TextView;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by tom on 9/16/15.
 */
public class SearchCitiesAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String TAG = "SearchCitiesAdapter";

    private APIClient client;
    private Index index;
    private int resourceId;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<City> mHits;

    public SearchCitiesAdapter(Context context, int resource, APIClient apiClient, GoogleApiClient googleApiClient) {
        super(context, resource);
        client = apiClient;
        index = client.initIndex("dev_cities");
        resourceId = resource;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(resourceId, null);
        }

        City city = mHits.get(position);
        TextView mainTextView = (TextView)view.findViewById(R.id.hit_main);
        mainTextView.setText(Html.fromHtml(city.getHighlightedName()));
        TextView countryTextView = (TextView)view.findViewById(R.id.hit_country);
        countryTextView.setText("Country: " + city.getCountry());
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            TextView distanceTextView = (TextView) view.findViewById(R.id.hit_distance);
            float distance = lastLocation.distanceTo(city.getLocation()) / 1000;
            distanceTextView.setText("Distance: " + String.format("%.2f", distance) + "km");
        }
        return view;
    }

    @Override
    public int getCount() {
        if (mHits != null) {
            return mHits.size();
        }
        return 0;
    }

    @Override
    public String getItem(int position) {
        if (mHits.size() > position) {
            return mHits.get(position).getName();
        }
        return null;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (lastLocation != null) {
                        Log.d(TAG, "Last location: " + lastLocation.toString());
                    }
                    else {
                        Log.d(TAG, "Geolocation search is unavailable on this device");
                    }

                    Query qry = new Query(constraint.toString());
                    if (lastLocation != null) {
                        qry = qry.aroundLatitudeLongitude((float)lastLocation.getLatitude(), (float)lastLocation.getLongitude(), 10000000);
                    }
                    JSONObject searchResult = null;
                    try {
                        //We use synchronous search here as we already are in a background thread
                        searchResult = index.search(qry);
                    } catch (AlgoliaException e) {
                        Log.e(TAG, "Error searching for [" + constraint + "]: " + e.getMessage());
                        e.printStackTrace();
                    }
                    mHits = searchResultsToArrayList(searchResult);
                    filterResults.values = mHits;
                    filterResults.count = mHits.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private ArrayList<City> searchResultsToArrayList(JSONObject results) {
        ArrayList<City> list = new ArrayList<City>();
        try {
            JSONArray hits = results.getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                JSONObject obj = hits.getJSONObject(i);
                City city = new City(obj);
                list.add(city);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

}

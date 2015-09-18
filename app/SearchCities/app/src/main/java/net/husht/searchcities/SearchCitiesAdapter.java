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

    private Index mAlgoliaIndex;
    private int mResourceId;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<City> mHits;

    public SearchCitiesAdapter(Context context, int resource, Index index, GoogleApiClient googleApiClient) {
        super(context, resource);
        mAlgoliaIndex = index;
        mResourceId = resource;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(mResourceId, null);
        }

        City city = mHits.get(position);
        TextView mainTextView = (TextView)view.findViewById(R.id.hit_main);
        mainTextView.setText(Html.fromHtml(city.getHighlightedProperty("name")));
        TextView countryTextView = (TextView)view.findViewById(R.id.hit_country);
        countryTextView.setText(Html.fromHtml("Country: " + city.getHighlightedProperty("country")));
        TextView distanceTextView = (TextView) view.findViewById(R.id.hit_distance);
        distanceTextView.setText("Distance: " + city.getFormattedDistance(mGoogleApiClient));

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

                    //Setup a query
                    Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Query qry = new Query(constraint.toString());
                    if (lastLocation != null) {
                        //If GPS coordinates of the device are reachable, we use geosearch
                        qry = qry.aroundLatitudeLongitude((float)lastLocation.getLatitude(), (float)lastLocation.getLongitude(), 10000000);
                    }

                    //Perform search
                    JSONObject searchResult = null;
                    try {
                        //We use synchronous search here as we already are in a background thread
                        searchResult = mAlgoliaIndex.search(qry);
                    } catch (AlgoliaException e) {
                        Log.e(TAG, "Error searching for [" + constraint + "]: " + e.getMessage());
                        e.printStackTrace();
                    }
                    ArrayList<City> hits = searchResultsToArrayList(searchResult);
                    filterResults.values = hits;
                    filterResults.count = hits.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mHits = (ArrayList<City>)results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    //Convert search results retrieved as JSONObject from Algolia API to an ArrayList of City objects
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

    //Return a City object from current results set at given index
    public City getCity(int position) {
        if (mHits != null && mHits.size() > position) {
            return mHits.get(position);
        }
        return null;
    }

}

package net.husht.searchcities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.Filter;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by tom on 9/16/15.
 */
public class SearchCitiesAdapter extends ArrayAdapter<String> implements Filterable {

    private static final String TAG = "SearchCitiesAdapter";

    private JSONArray hits;
    private APIClient client;
    private Index index;

    public SearchCitiesAdapter(Context context, int resource, APIClient apiClient) {
        super(context, resource);
        client = apiClient;
        index = client.initIndex("dev_cities");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView: " + convertView);
        return super.getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        if (hits != null) {
            return hits.length();
        }
        return 0;
    }

    @Override
    public String getItem(int position) {
        String item = null;
        try {
            JSONObject hit = hits.getJSONObject(position);
            item = hit.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
        return item;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    JSONObject searchResult = null;
                    try {
                        //We use synchronous search here as we already are in a background thread
                        searchResult = index.search(new Query(constraint.toString()));
                        hits = searchResult.getJSONArray("hits");
                        filterResults.values = hits;
                        filterResults.count = hits.length();
                        Log.d(TAG, "Matched " + hits.length() + " results while searching for [" + constraint + "]");
                    } catch (AlgoliaException e) {
                        Log.e(TAG, "Error searching for [" + constraint + "]: " + e.getMessage());
                        e.printStackTrace();
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
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
        return filter;
    }

}

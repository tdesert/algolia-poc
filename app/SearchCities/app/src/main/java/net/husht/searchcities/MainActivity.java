package net.husht.searchcities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.Index;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = "MainActivity";
    private final static int PERMISSIONS_REQUEST_LOCATION_ID = 42;

    private GoogleApiClient mGoogleApiClient;
    private APIClient mAlgoliaClient;

    private DelayedAutoCompleteTextView mAutoCompleteTextView;
    private SearchCitiesAdapter mSearchCitiesAdapter;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CityDetailAdapter mCityDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to Google Play Services API. Further initialization is performed in the connection callbacks.
        buildGoogleApiClient();

        //Setup the recycler view that will be used to display a selected city details from search results
        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mCityDetailAdapter = new CityDetailAdapter(mGoogleApiClient);
        mRecyclerView.setAdapter(mCityDetailAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    // Google Play Services API Initialization
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initSearchEngine() {
        if (mAlgoliaClient != null) return; //Initialization is already done

        //Setup Algolia Client
        String apiKey = getString(R.string.algolia_api_key);
        String appId = getString(R.string.algolia_app_id);
        mAlgoliaClient = new APIClient(appId, apiKey);
        Index index = mAlgoliaClient.initIndex(getString(R.string.algolia_cities_index));

        //Setup autocomplete text view to perform searches based on user input
        mAutoCompleteTextView = (DelayedAutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
        mSearchCitiesAdapter = new SearchCitiesAdapter(this, R.layout.hit, index, mGoogleApiClient);
        mAutoCompleteTextView.setAdapter(mSearchCitiesAdapter);
        mAutoCompleteTextView.setLoadingIndicator((ProgressBar) findViewById(R.id.autoCompleteLoadingIndicator));
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked on city " + mSearchCitiesAdapter.getItem(position));
                mCityDetailAdapter.setCity(mSearchCitiesAdapter.getCity(position));
            }
        });
    }

    /**
     * Google Play Services API connection callbacks.
     * When running on Android API >= 23, we need to explicitly request permissions to access GPS coordinates
     * of the device.
     *
     */
    @Override
    public void onConnected(Bundle bundle) {
        //Setup access to GPS information once connected to Google Play Services API
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION_ID);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, new LocationRequest(), this);
        }

        //Initialize Algolia API client and autocomplete text view
        initSearchEngine();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Failed to connect to Google Play Services API. Search engine will not use geosearch.
        initSearchEngine();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    /*
     * Permissions callbacks
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION_ID:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, new LocationRequest(), this);
                }
                    break;
        }
    }

}

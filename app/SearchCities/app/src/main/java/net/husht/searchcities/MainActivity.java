package net.husht.searchcities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.algolia.search.saas.APIClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = "MainActivity";
    private final static int PERMISSIONS_REQUEST_LOCATION_ID = 42;

    private final static String ALGOLIA_API_KEY = "08819e0217baeb114f3026c444009ae9";
    private final static String ALGOLIA_APP_ID = "CC37YOB5YL";

    private GoogleApiClient mGoogleApiClient;
    private APIClient mAlgoliaClient;

    private AutoCompleteTextView mAutoCompleteTextView;
    private SearchCitiesAdapter mSearchCitiesAdapter;

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CityDetailAdapter mCityDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to Google Play Services API, further initialization is performed in the connection callbacks
        buildGoogleApiClient();

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

    @Override
    public void onConnected(Bundle bundle) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION_ID);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, new LocationRequest(), this);
        }
        initSearchEngine();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        initSearchEngine();
    }

    private void initSearchEngine() {
        if (mAlgoliaClient != null) return; //Initialization is already done

        mAlgoliaClient = new APIClient(ALGOLIA_APP_ID, ALGOLIA_API_KEY);
        mAutoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);

        mSearchCitiesAdapter = new SearchCitiesAdapter(this, R.layout.hit, mAlgoliaClient, mGoogleApiClient);
        mAutoCompleteTextView.setAdapter(mSearchCitiesAdapter);
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Clicked on city " + mSearchCitiesAdapter.getItem(position));
                mCityDetailAdapter.setCity(mSearchCitiesAdapter.getCity(position));
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
    }


    // Permissions callbacks

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

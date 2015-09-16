package net.husht.searchcities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.IndexListener;
import com.algolia.search.saas.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private APIClient client;
    private Index index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new APIClient("CC37YOB5YL", "08819e0217baeb114f3026c444009ae9");
        index = client.initIndex("dev_cities");
        index.searchASync(new Query("par"), new IndexListener() {
            @Override
            public void addObjectResult(Index index, JSONObject jsonObject, JSONObject jsonObject1) {

            }

            @Override
            public void addObjectError(Index index, JSONObject jsonObject, AlgoliaException e) {

            }

            @Override
            public void addObjectsResult(Index index, List<JSONObject> list, JSONObject jsonObject) {

            }

            @Override
            public void addObjectsError(Index index, List<JSONObject> list, AlgoliaException e) {

            }

            @Override
            public void addObjectsResult(Index index, JSONArray jsonArray, JSONObject jsonObject) {

            }

            @Override
            public void addObjectsError(Index index, JSONArray jsonArray, AlgoliaException e) {

            }

            @Override
            public void searchResult(Index index, Query query, JSONObject jsonObject) {
                Log.d(TAG, jsonObject.toString());
            }

            @Override
            public void searchError(Index index, Query query, AlgoliaException e) {

            }

            @Override
            public void deleteObjectResult(Index index, String s, JSONObject jsonObject) {

            }

            @Override
            public void deleteObjectError(Index index, String s, AlgoliaException e) {

            }

            @Override
            public void deleteObjectsResult(Index index, JSONArray jsonArray, JSONObject jsonObject) {

            }

            @Override
            public void deleteByQueryError(Index index, Query query, AlgoliaException e) {

            }

            @Override
            public void deleteByQueryResult(Index index) {

            }

            @Override
            public void deleteObjectsError(Index index, List<JSONObject> list, AlgoliaException e) {

            }

            @Override
            public void saveObjectResult(Index index, JSONObject jsonObject, String s, JSONObject jsonObject1) {

            }

            @Override
            public void saveObjectError(Index index, JSONObject jsonObject, String s, AlgoliaException e) {

            }

            @Override
            public void saveObjectsResult(Index index, List<JSONObject> list, JSONObject jsonObject) {

            }

            @Override
            public void saveObjectsError(Index index, List<JSONObject> list, AlgoliaException e) {

            }

            @Override
            public void saveObjectsResult(Index index, JSONArray jsonArray, JSONObject jsonObject) {

            }

            @Override
            public void saveObjectsError(Index index, JSONArray jsonArray, AlgoliaException e) {

            }

            @Override
            public void partialUpdateResult(Index index, JSONObject jsonObject, String s, JSONObject jsonObject1) {

            }

            @Override
            public void partialUpdateError(Index index, JSONObject jsonObject, String s, AlgoliaException e) {

            }

            @Override
            public void partialUpdateObjectsResult(Index index, List<JSONObject> list, JSONObject jsonObject) {

            }

            @Override
            public void partialUpdateObjectsError(Index index, List<JSONObject> list, AlgoliaException e) {

            }

            @Override
            public void partialUpdateObjectsResult(Index index, JSONArray jsonArray, JSONObject jsonObject) {

            }

            @Override
            public void partialUpdateObjectsError(Index index, JSONArray jsonArray, AlgoliaException e) {

            }

            @Override
            public void getObjectResult(Index index, String s, JSONObject jsonObject) {

            }

            @Override
            public void getObjectError(Index index, String s, AlgoliaException e) {

            }

            @Override
            public void getObjectsResult(Index index, List<String> list, JSONObject jsonObject) {

            }

            @Override
            public void getObjectsError(Index index, List<String> list, AlgoliaException e) {

            }

            @Override
            public void waitTaskResult(Index index, String s) {

            }

            @Override
            public void waitTaskError(Index index, String s, AlgoliaException e) {

            }

            @Override
            public void getSettingsResult(Index index, JSONObject jsonObject) {

            }

            @Override
            public void getSettingsError(Index index, AlgoliaException e) {

            }

            @Override
            public void setSettingsResult(Index index, JSONObject jsonObject, JSONObject jsonObject1) {

            }

            @Override
            public void setSettingsError(Index index, JSONObject jsonObject, AlgoliaException e) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

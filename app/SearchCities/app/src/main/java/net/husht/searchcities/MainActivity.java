package net.husht.searchcities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;

import com.algolia.search.saas.APIClient;
import com.algolia.search.saas.Index;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private APIClient client;
    private Index index;
    private AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = new APIClient("CC37YOB5YL", "08819e0217baeb114f3026c444009ae9");
        autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);

        SearchCitiesAdapter adapter = new SearchCitiesAdapter(this, R.layout.hit, client);
        autoCompleteTextView.setAdapter(adapter);
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

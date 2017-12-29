package com.app.waleent.geodo;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class AddTaskActivity extends AppCompatActivity {
    private static String TAG = AddTaskActivity.class.getSimpleName();

    private PlaceAutoCompleteAdapter mAdapter;
    private AddTaskViewModel addTaskViewModel;

    private String latitude ;
    private String longitude;
    private Date date = new Date();

    EditText titleText;
    PlaceAutoCompleteTextView autoCompleteTextView;

    GoogleAPIClient customGoogleApiClient;
    private LatLngBounds CURRENT_USER_BOUND;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        customGoogleApiClient = GoogleAPIClient.getInstance(this);
        GoogleApiClient mGoogleApiClient = customGoogleApiClient.buildGoogleAPIClient();

        Intent parentIntent = getIntent();
        Bundle mainBundle = parentIntent.getParcelableExtra("main_bundle");
        CURRENT_USER_BOUND = mainBundle.getParcelable("current_user_bound");
        addTaskViewModel = ViewModelProviders.of(this).get(AddTaskViewModel.class);

        mAdapter = new PlaceAutoCompleteAdapter(this, mGoogleApiClient, CURRENT_USER_BOUND);
        autoCompleteTextView = findViewById(R.id.task_location);
        autoCompleteTextView.setThreshold(3);
        autoCompleteTextView.setAdapter(mAdapter);

        latitude = "";
        longitude = "";

        autoCompleteTextView.setOnItemClickListener((adapterView, view, i, l) -> {
            final AutoCompletePlace item = mAdapter.getItem(i);
            latitude = String.valueOf(item.latitude);
            longitude = String.valueOf(item.longitude);
            Log.d(TAG,"Autocomplete Lat and Lon"+ latitude +" "+longitude);
        });

        titleText = findViewById(R.id.task_title);
        Button saveTaskBtn = findViewById(R.id.save_task);
        saveTaskBtn.setOnClickListener(view -> {
            if (autoCompleteTextView.getText().toString() == null || titleText.getText() == null || TextUtils.isEmpty(longitude) || TextUtils.isEmpty(latitude)) {
                String query = autoCompleteTextView.getText().toString();
                new textPlaceApiSearch().execute(query);
            }
            else {
                TaskModel taskModel= new TaskModel(
                        titleText.getText().toString(),
                        autoCompleteTextView.getText().toString(),
                        date,
                        latitude,
                        longitude
                );
                insertTask(taskModel);
                Log.d(TAG, "User Bound is :" + CURRENT_USER_BOUND);
                Log.d(TAG, "User lat :" + latitude + longitude);
                finish();
            }
        });

    }

    private void insertTask(TaskModel taskModel){
        new Thread(() -> addTaskViewModel.addTask(taskModel)).start();
    }
    private class textPlaceApiSearch extends AsyncTask<String,Void,Void>{
        PlaceAPI placeAPI = new PlaceAPI();

        @Override
        protected Void doInBackground(String... strings) {
            Log.d(TAG,"Query: "+strings[0]);
            JSONObject jsonObject = placeAPI.textSearch(strings[0]);
            Log.d(TAG,"Result: "+jsonObject);
            try {
                JSONObject geometry = jsonObject.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                latitude = location.getString("lat");
                longitude= location.getString("lng");
                addTaskViewModel.addTask(new TaskModel(
                        titleText.getText().toString(),
                        jsonObject.getString("formatted_address"),
                        date,
                        latitude,
                        longitude
                ));
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

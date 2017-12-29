package com.app.waleent.geodo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleAPIClient customGoogleAPIClient;

    private final int PERMISSION_ACCESS_LOCATION = 100;
    private final double degreeRadius = 1.0;
    private static LatLngBounds CURRENT_USER_BOUND = null;
    private static Location mLastLocation;

    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private static final float GEOFENCE_RADIUS = 5000000.0f;
    private final int UPDATE_INTERVAL = 1 * 60 * 1000;
//    private final int FASTEST_INTERVAL = 3 * 60 * 1000;

    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private String ListFragTag = "ListFragment";
    private String MapFragTag = "MapFragment";

    private Fragment visibleFragment;
    private HashSet<TaskModel> withinRangeTask;
    TaskListViewModel taskListViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customGoogleAPIClient = GoogleAPIClient.getInstance(this);

        mGoogleApiClient = customGoogleAPIClient.buildGoogleAPIClient();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

       // Handler
        taskListViewModel = ViewModelProviders.of(this).get(TaskListViewModel.class);
        if (!checkPermissions()) {
            requestPermissions();
        }else{
            startLocationUpdates();
        }

        //Log.d(TAG, "last known location is: " + mLastLocation);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), AddTaskActivity.class);
            Bundle data = new Bundle();
            data.putParcelable("current_user_bound", CURRENT_USER_BOUND);
            i.putExtra("main_bundle", data);
            startActivity(i);
        });

        getSupportFragmentManager().addOnBackStackChangedListener(backStackedChangedListener());
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TaskListFragment(), ListFragTag)
                    .commit();
        }

        onNewIntent(getIntent());
    }

    private void getPointsWithinRadius() {
        //Log.d(TAG,"getting points for geofence");
        withinRangeTask = new HashSet<>();
        taskListViewModel.getTaskList().observe(this, taskModelList -> {
            if (taskModelList != null)
                for (TaskModel taskModel : taskModelList) {
                    Location locationA = new Location("TaskLocation");
                    locationA.setLatitude(Double.parseDouble(taskModel.getLatitude()));
                    locationA.setLongitude(Double.parseDouble(taskModel.getLongitude()));
                    if (mLastLocation.distanceTo(locationA) <= GEOFENCE_RADIUS) {
                        withinRangeTask.add(taskModel);
                    }
                }
        });
        // Log.d(TAG,"got:"+ withinRangeTask.size() + " points");
    }

    private GeofencingRequest createGeofenceRequest(List<Geofence> geofences) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }

    public ArrayList<Geofence> createMyGeofences(HashSet<TaskModel> withinRangeTask) {
        getPointsWithinRadius();
        ArrayList<Geofence> listOfFence = new ArrayList<>();
        if (withinRangeTask != null) {
            for (TaskModel taskModel : withinRangeTask) {
                Geofence geofence = new Geofence.Builder().setRequestId(String.valueOf(taskModel.id))
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .setCircularRegion(Double.parseDouble(taskModel.getLatitude()), Double.parseDouble(taskModel.getLongitude()), GEOFENCE_RADIUS)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .build();
                listOfFence.add(geofence);
            }
        }
        //Log.d(TAG, "Count of Geofence  is:" + listOfFence.size());
        return listOfFence;
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null)
            return mGeofencePendingIntent;

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.CURRENT_LOCATION,mLastLocation);
        Intent intent = GeoFenceTransitionIntentService.newIntent(MainActivity.this);
        intent.putExtra("MAIN_BUNDLE",bundle);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @SuppressLint("MissingPermission")
    private void addGeofence() {
        if (checkPermissions()) {
            mGeofencingClient.addGeofences(createGeofenceRequest(createMyGeofences(withinRangeTask)), getGeofencePendingIntent())
                    .addOnSuccessListener(this, aVoid -> {
//                        Log.d(TAG, "Geofence Created");

                    }).addOnFailureListener(this, e -> {
//                Log.d(TAG, "Failed to add geofence " + e.getMessage());
            });
        }

    }

    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (createMyGeofences(withinRangeTask) != null && !withinRangeTask.isEmpty()) {
            addGeofence();
        } else {
            Log.e(TAG, "No Geofence");
        }
    }

    private FragmentManager.OnBackStackChangedListener backStackedChangedListener() {
        FragmentManager.OnBackStackChangedListener result = () -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
                visibleFragment = fragment;
            }
        };
        return result;
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ACCESS_LOCATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map_view:
                Bundle mapBundle = new Bundle();
                mapBundle.putParcelable("current_user_bound", CURRENT_USER_BOUND);
                mapBundle.putParcelable("current_location", mLastLocation);
                TaskMapFragment taskMapFragment = TaskMapFragment.newInstance();
                taskMapFragment.setArguments(mapBundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, taskMapFragment, MapFragTag).addToBackStack(null).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setMaxWaitTime(2 * UPDATE_INTERVAL);
//        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());


    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    view -> {
                        // Request permission
                        startLocationPermissionRequest();
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLastLocation();
                } else {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                            view -> {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(this,
                task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        mLastLocation = task.getResult();

                    } else {
                        Log.w(TAG, "getLastLocation:exception", task.getException());
                        showSnackbar(getString(R.string.no_location_detected));
                        startLocationUpdates();
                    }
                });

    }

    private void showSnackbar(final String text) {
        View container = findViewById(R.id.add_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        mLastLocation = location;
        LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        LatLng northEast = new LatLng(center.latitude + degreeRadius, center.longitude + degreeRadius);
        LatLng southWest = new LatLng(center.latitude - degreeRadius, center.longitude - degreeRadius);
        CURRENT_USER_BOUND = LatLngBounds.builder().include(northEast).include(southWest).build();
        Log.d(TAG, "Last Known location :" + CURRENT_USER_BOUND);
        if (mLastLocation != null) {
            Log.d(TAG, "startGeofence");
            startGeofence();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG,"New Intent");

        Bundle extras = intent.getExtras();
        if(extras != null){
            if (extras.containsKey(Constants.HIGHLIGHT_TASK_EXTRA)){
                ArrayList<String> highlightedTask = extras.getStringArrayList(Constants.HIGHLIGHT_TASK_EXTRA);
                Log.d(TAG,"Highlighted task= "+ highlightedTask);
                mGeofencingClient.removeGeofences(highlightedTask);
            }
        }

    }

    public static Intent newIntent(Context context) {
        return new Intent(context,MainActivity.class);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
}

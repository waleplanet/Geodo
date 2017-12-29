package com.app.waleent.geodo;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class TaskMapFragment extends Fragment  implements OnMapReadyCallback{

    public String TAG= TaskMapFragment.class.getSimpleName();
    private Location mLastLocation;
    private List<TaskModel> taskModels;
    TaskListViewModel viewModel;
    private GoogleMap mMap;

    public TaskMapFragment() {

    }


    public static TaskMapFragment newInstance() {
        TaskMapFragment fragment = new TaskMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLastLocation = getArguments().getParcelable("current_location");
        }
        viewModel = ViewModelProviders.of(this).get(TaskListViewModel.class);
        taskModels =  new ArrayList<>();
//        taskModels=viewModel.getTasks();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_task_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.task_map_fragment);
        mapFragment.getMapAsync(this);
        viewModel.getTaskList().observe(this,taskModelList -> {
            Log.d(TAG,"change observerd "+ taskModelList);
            taskModels=taskModelList;
            addMarkersToMap(mMap);
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_fragment_menu,menu);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"My location is "+mLastLocation);
        Log.d(TAG,"Map is ready");
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        if(mLastLocation != null){
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),13));
        }
        addMarkersToMap(mMap);
    }

    private void addMarkersToMap(GoogleMap googleMap) {
        Log.d(TAG,"Task models "+taskModels);
        Marker[] markers = new  Marker[taskModels.size()];

        for (int i=0; i< taskModels.size(); i++){
            LatLng latLng = new LatLng(Double.parseDouble(taskModels.get(i).getLatitude()) ,Double.parseDouble(taskModels.get(i).getLongitude()));
            markers[i] = googleMap.addMarker(new MarkerOptions().position(latLng).title(taskModels.get(i).getTitle()));
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(Marker marker: markers){
            builder.include((marker.getPosition()));
        }

        if (taskModels.size()>=1){
            LatLngBounds bounds = builder.build();
            int padding = 0; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cu);
        }
    }
}

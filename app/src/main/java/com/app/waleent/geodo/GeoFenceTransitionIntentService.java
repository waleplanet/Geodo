package com.app.waleent.geodo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wale on 11/15/17.
 */

public class GeoFenceTransitionIntentService extends IntentService {

    private String TAG = GeoFenceTransitionIntentService.class.getSimpleName();
    public Location triggeredLocation;
//    public Intent startIntent;
//    public Bundle mainBundle;

    public static Intent newIntent(Context context){
        return new Intent(context,GeoFenceTransitionIntentService.class);
    }

    public GeoFenceTransitionIntentService() {
        super("GeoFenceTransitionIntentService");
    }

    private static String getErrorString(int errorCode){
        switch(errorCode){
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//        startIntent = intent;
//        mainBundle = intent.getExtras().getBundle("MAIN_BUNDLE");
//        Log.d(TAG,"Contains :"+mainBundle.containsKey(Constants.CURRENT_LOCATION));

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
//            Log.e(TAG, errorMsg);
            return;
        }
        triggeredLocation = geofencingEvent.getTriggeringLocation();
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
//            String geofenceTransitionDetails = getGeofenceTransitiondetails(geofenceTransition,triggeredGeofences);
//            ArrayList<String > taskIds= new ArrayList<>();
//
//            for (Geofence geofence: triggeredGeofences){
//                taskIds.add(geofence.getRequestId());
//            }
            sendEnteringNotification(this,triggeredGeofences);
        }
    }

//    private String getGeofenceTransitiondetails(int geofenceTransition, List<Geofence> triggeredGeofences) {
//        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
//        for ( Geofence geofence : triggeredGeofences ) {
//            triggeringGeofencesList.add( geofence.getRequestId());
//            getGeofenceTask(geofence.getRequestId());
//        }
//        String status = null;
//        if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
//            status = "Entering ";
//        else if ( geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
//            status = "Exiting ";
//        return status + TextUtils.join( ", ", triggeringGeofencesList);
//    }

    private TaskModel getGeofenceTask (String id){
//        viewModel = ViewModelProviders.of(this).get(TaskListViewModel.class);
        TaskModel taskModel=TaskListViewModel.getTask(id);
//        Log.d(TAG,"Task title is :"+taskModel.getTitle());
        return taskModel;
    }

    private void sendEnteringNotification(Context context,List<Geofence> geofences){
//        Log.i(TAG, "sendNotification: " + msg );

        Intent notificationIntent = MainActivity.newIntent(context);
        Bundle bdl = new Bundle();
        ArrayList<String > taskIds= new ArrayList<>();

        for (Geofence geofence: geofences){
            taskIds.add(geofence.getRequestId());
        }
        bdl.putStringArrayList(Constants.HIGHLIGHT_TASK_EXTRA,taskIds);
        notificationIntent.putExtras(bdl);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,0,notificationIntent,0);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (taskIds.size() == 1){
            TaskModel task = getGeofenceTask(taskIds.get(0));

//            Intent  muIntent = Intent.getIntent()
//            Location currentUserLocation = mainBundle.getParcelable(Constants.CURRENT_LOCATION);
//            Log.d(TAG,"curent location is: "+ currentUserLocation);
//            Log.d(TAG,"event location is: "+ triggeredLocation);
            Location taskLocation = new Location("");
            taskLocation.setLatitude(Double.parseDouble(task.getLatitude()));
            taskLocation.setLongitude(Double.parseDouble(task.getLongitude()));
            float distanceLeft = triggeredLocation.distanceTo(taskLocation);
//            Log.d(TAG,"difference in distance is: "+ distanceLeft);
            String title = task.getTitle();
            String msg = String.format("You are %.0f meters from %s to complete task",distanceLeft,task.getLocation());
            notificationManager.notify(0,createSingleEnteringTaskNotification(msg,title ,notificationPendingIntent));
        }
        if (taskIds.size() > 1){
            String title = String.format("Quickly complete %d tasks",taskIds.size());
            String msg =String.format("You are in range to complete %d tasks",taskIds.size());
            notificationManager.notify(0,createMultiEnteringTaskNotification(msg,title,notificationPendingIntent));
        }


    }

    private Notification createSingleEnteringTaskNotification(String msg,String title, PendingIntent notificationPendingIntent) {
        Notification notification = new NotificationCompat.Builder(this,"CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        return notification;
    };

    private Notification createMultiEnteringTaskNotification(String msg,String title, PendingIntent notificationPendingIntent) {
        Notification notification = new NotificationCompat.Builder(this,"CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        return notification;
    }

}

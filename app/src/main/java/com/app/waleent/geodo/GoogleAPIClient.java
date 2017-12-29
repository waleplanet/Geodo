package com.app.waleent.geodo;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

/**
 * Created by wale on 11/2/17.
 */

class GoogleAPIClient {
    private static GoogleAPIClient ourInstance = null;
    private static GoogleApiClient mGoogleApiClient = null;

    Context dContext;

    static GoogleAPIClient getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new GoogleAPIClient(context);
        }
        return ourInstance;
    }

    private GoogleAPIClient(Context context) {
        dContext= context;
    }

    public synchronized GoogleApiClient buildGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(dContext)
                // .addConnectionCallbacks(this)
                .addApi(Places.GEO_DATA_API)
                //.addOnConnectionFailedListener(this)
                //.addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        return mGoogleApiClient;
    }
}

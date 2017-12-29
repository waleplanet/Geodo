package com.app.waleent.geodo;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by wale on 10/30/17.
 */

class PlaceAutoCompleteAdapter extends ArrayAdapter<AutoCompletePlace> implements Filterable {
    // ArrayList<String> resultList;

    Context mContext;
    // int mResource;
    GoogleApiClient mGoogleApiClient;

    LatLng itemLatLng;
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<AutocompletePrediction> mResultList;
    private LatLngBounds mBounds;

    public PlaceAutoCompleteAdapter(Context context, GoogleApiClient googleApiClient,LatLngBounds bounds) {
        super(context,R.layout.autocomplete_list_item);
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mBounds = bounds;
        itemLatLng = new LatLng(0.00,0.00);
    }

    @NonNull
    @Override
    public int getCount() {
        // Last item will be the footer
        return mResultList.size();
    }

    @Nullable
    @Override
    public AutoCompletePlace getItem(int position) {
        AutocompletePrediction item =mResultList.get(position);
        setPlaceLatLng(item);
        AutoCompletePlace place = new AutoCompletePlace(item.getPlaceId(),item.getPrimaryText(null).toString(),itemLatLng.latitude,itemLatLng.longitude);
        return place;
    }

    private void setPlaceLatLng(AutocompletePrediction item) {
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, item.getPlaceId())
                .setResultCallback(places -> {
                    if (places.getStatus().isSuccess()) {
                        final Place myPlace = places.get(0);
                        itemLatLng = myPlace.getLatLng();
                    }
                    places.release();
                });

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (position != (mResultList.size() - 1))
            convertView = inflater.inflate(R.layout.autocomplete_list_item, null);
        else
            convertView = inflater.inflate(R.layout.autocomplete_google_logo, null);


        if (position != (mResultList.size() - 1)) {
            AutoCompletePlace item = getItem(position);
            TextView autocompleteTextView = convertView.findViewById(R.id.autocomplete_text);
            autocompleteTextView.setText(item.description);
        }
        return convertView;
    }


    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                if (charSequence != null){
                    filterData = getAutocomplete(charSequence);
                }
                filterResults.values = filterData;
                if (filterData != null) {
                    filterResults.count = filterData.size();
                } else {
                    filterResults.count = 0;
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0) {
                    mResultList = (ArrayList<AutocompletePrediction>)filterResults.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }

            }


        };
        return filter;
    }

    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence query) {
        if (mGoogleApiClient.isConnected()) {
            Log.i("ArrayAdapter", "Starting autocomplete query for: " + query);

            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(mGoogleApiClient, query.toString(),
                                    mBounds, new AutocompleteFilter.Builder().setTypeFilter(Place.TYPE_STREET_ADDRESS).build());

            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Toast.makeText(getContext(), "Error contacting API: " + status.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e("ArrayAdapter", "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }
            Log.i("ArrayAdapter", "Query completed. Received " + autocompletePredictions.getCount()
                    + " predictions.");

            // Freeze the results immutable representation that can be stored safely.
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        }
        Log.e("ArrayAdapter", "Google API client is not connected for autocomplete query.");
        return null;
    }



}

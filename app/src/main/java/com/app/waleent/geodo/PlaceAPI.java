package com.app.waleent.geodo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by wale on 10/30/17.
 */

public class PlaceAPI {
    private static final String TAG = PlaceAPI.class.getSimpleName();
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_TEXTSEARCH = "/textsearch";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBdxgu5s934362lbB6tTv_eTRFEGIvgmKc";

    public ArrayList<String> autocomplete (String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try{
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&types=(cities)");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG,e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG,e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            Log.d(TAG,jsonResults.toString());
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        }catch (JSONException e){
            Log.e(TAG,e.getMessage());
        }
        return resultList;
    }

    public JSONObject textSearch (String input) {
       // ArrayList<String> resultList = null;

        JSONObject result = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try{
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_TEXTSEARCH + OUT_JSON);
           // sb.append("&location="+42.3675294+","+-71.186966);
            sb.append("?key=" + API_KEY);
            sb.append("&query=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG,e.getMessage());
        } catch (MalformedURLException e) {
            Log.e(TAG,e.getMessage());
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            Log.d(TAG,jsonResults.toString());
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray resultsJsonArray = jsonObj.getJSONArray("results");
            result = resultsJsonArray.getJSONObject(0);
            return result;
//            resultList = new ArrayList<String>(predsJsonArray.length());
//            return resultList.get(0);
//            for (int i = 0; i < predsJsonArray.length(); i++) {
//                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
//            }
        }catch (JSONException e){
            Log.e(TAG,e.getMessage());
        }
        return result;
    }

}

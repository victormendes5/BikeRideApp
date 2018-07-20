package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BikeRideGoogleDistanceMatrixAPI extends AsyncTask<String, Void, String>{

    // https://developers.google.com/maps/documentation/distance-matrix/intro

    private static final String TAG = "GoogleDistanceMatrix";

    Context context;
    private BikeRideGoogleDistanceMatrixAPIAsyncInterface
            bikeRideGoogleDistanceMatrixAPIAsyncInterface;

    public BikeRideGoogleDistanceMatrixAPI(Context context) {
        this.context = context;
        this.bikeRideGoogleDistanceMatrixAPIAsyncInterface =
                (BikeRideGoogleDistanceMatrixAPIAsyncInterface) context;
    }

    @Override
    protected String doInBackground(String... urls) {

        Log.i(TAG, "doInBackground: fetching distance and duration...");

        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {

            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();

            while (data != -1) {
                char current = (char) data;
                result += current;
                data = reader.read();
            }

            return result;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.i(TAG, "onPostExecute: distance and duration fetched.");

        try {

            JSONObject distanceMatrixObj = new JSONObject(s);
            String distanceMatrixRows = distanceMatrixObj.getString("rows");
            JSONArray distanceMatrixRowsArr = new JSONArray(distanceMatrixRows);
            JSONObject distanceMatrixElements = distanceMatrixRowsArr.getJSONObject(0);
            String distanceMatrixDetails = distanceMatrixElements.getString("elements");
            JSONArray distanceMatrixDetailsArr = new JSONArray(distanceMatrixDetails);
            JSONObject distanceObject =  distanceMatrixDetailsArr.getJSONObject(0);
            String distanceMatrixDistance = distanceObject.getString("distance");
            JSONObject distanceMatrixDistanceText = new JSONObject(distanceMatrixDistance);
            String distance = distanceMatrixDistanceText.getString("text");
            String distanceMatrixDuration = distanceObject.getString("duration");
            JSONObject distanceMatrixDurationText = new JSONObject(distanceMatrixDuration);
            String duration = distanceMatrixDurationText.getString("text");

            bikeRideGoogleDistanceMatrixAPIAsyncInterface
                    .onCalculateDistanceAndDurationCompleted(distance, duration);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

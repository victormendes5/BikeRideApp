package com.infnet.bikeride.bikeride.services;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.HttpRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class GoogleDirectionsAPI {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "GoogleDirectionsAPI";
    private static final String API_KEY = Constants.Keys.GOOGLE_API;
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String UNIT_TYPE = "metric";  // "metric" or "imperial"
    private static final String TRAVEL_MODE = "bicycling"; // "walking", "bicycling", "driving"

    //endregion

    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private static String mOverallStatus;
    private static ArrayList<String> mGeocodedWaypointsStatus = new ArrayList<>();

    private static LatLngBounds mBoundaries;
    private static ArrayList<LatLng> mWayPoints = new ArrayList<>();

    //endregion


    /*=======================================================================================
                                            INTERFACES
     =======================================================================================*/

    //region INTERFACES

    public interface OnDirectionsComplete {
        void onSuccess(LatLngBounds boundaries, ArrayList<LatLng> waypoints);
        void onFailure();
    }

    private interface OnDirectionsDecodeComplete {
        void onSuccess() ;
        void onFailure(String errorMessage);
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                      LOGIC                                       |
       |                                                                                  |
       \=================================================================================*/

    public static void getData(Double[] data,
                               final OnDirectionsComplete callbacks) {

        // https://developers.google.com/maps/documentation/directions/intro

        if (data.length%2 != 0 || data.length<4) {

            Log.d(TAG, "getData: ERROR! Number of arguments passed on " +
                    "varargs parameter is incorrect. (Total of " + data.length +
                    " arguments passed. Total must be multiple of 2, at least 4 doubles)");

            callbacks.onFailure();

            return;
        }

        Log.d(TAG, "getData: getting Google Directions API data ...");
        Log.d(TAG, "getData: building directions API request string ...");

        Double startLatitude = data[0];
        Double startLongitude = data[1];

        Double finalLatitude = data[data.length-2];
        Double finalLongitude = data[data.length-1];

        String executeString = BASE_URL
                + "units=" + UNIT_TYPE + "&"
                + "origin=" + startLatitude + "," + startLongitude + "&"
                + "destination=" + finalLatitude + "," + finalLongitude + "&";

        if (data.length>4) {

            executeString += "waypoints=";

            for (int i = 0; i<data.length; i += 2) {

                if (i == 0 || i == data.length-2) continue;

                executeString = executeString + "via:" + data[i] + "," + data[i+1] + "|";
            }

            executeString = executeString.substring(0, executeString.length() - 1);

            executeString += "&";
        }

        executeString = executeString
                + "mode=" + TRAVEL_MODE + "&"
                + "key=" + API_KEY;

        Log.d(TAG, "getData: built directions API request string is - " +
                executeString);

        Log.d(TAG, "getData: executing Http request ...");

        final long startTime = System.currentTimeMillis();

        new HttpRequest(new HttpRequest.Callbacks() {

            @Override
            public String OnComplete(String data) {

                long stopTime = System.currentTimeMillis();

                double elapsedTime = (stopTime - startTime) / 1000;

                Log.d(TAG, "getData: directions API data successfully " +
                        "retrieved (" + elapsedTime + " s).");

                decodeGoogleDirectionsData(data,

                    new OnDirectionsDecodeComplete() {

                        @Override
                        public void onSuccess() {

                            Log.d(TAG, "getData: successfully decoded " +
                                    "directions API data.");

                            callbacks.onSuccess(mBoundaries, mWayPoints);

                        }

                        @Override
                        public void onFailure(String errorMessage) {

                            Log.d(TAG, "getData: could not decode " +
                                    "directions API data - " + errorMessage);

                            callbacks.onFailure();
                        }
                    });


                return null;
            }

            @Override
            public String OnFailure(Exception e) {

                Log.d(TAG, "getData: failed to execute Http request to " +
                        "retrieve directions API data.");

                callbacks.onFailure();
                return null;
            }
        }).execute(executeString);
    }

    private static void decodeGoogleDirectionsData (String data,
                                                    OnDirectionsDecodeComplete callbacks) {

        Log.d(TAG, "decodeGoogleDirectionsData: decoding directions API data ...");

        mWayPoints.clear();
        mGeocodedWaypointsStatus.clear();


        try {

            JSONObject directionsObj = new JSONObject(data);

            // ---> Decoding overall status
            mOverallStatus = directionsObj.getString("status");

            Log.d(TAG, "decodeGoogleDirectionsData: overall status is - " + mOverallStatus);

            // ---> Decoding geocoded waypoint statuses
            String geocodedWaypointsString = directionsObj.getString("geocoded_waypoints");
            JSONArray geocodedWaypointsArr = new JSONArray(geocodedWaypointsString);

            for (int i = 0; i<geocodedWaypointsArr.length(); i++) {

                JSONObject geocodedWaypointsArrElement = geocodedWaypointsArr.getJSONObject(i);
                String geocodedWaypointsArrElementStatus = geocodedWaypointsArrElement
                        .getString("geocoder_status");

                Log.d(TAG, "decodeGoogleDirectionsData: geocoded_waypoints element no. "
                        + i + " status is " + geocodedWaypointsArrElementStatus + ".");

                mGeocodedWaypointsStatus.add(geocodedWaypointsArrElementStatus);
            }

            // ---> Test for data integrity
            if (isStatusNotOK()) {

                String message = "Overall Satus: " + mOverallStatus;

                if (mGeocodedWaypointsStatus.size()>0) {

                    for (int i = 0; i<mGeocodedWaypointsStatus.size(); i++) {

                        message += " / Geocoded Waypoint no. " + i + " status: " +
                                mGeocodedWaypointsStatus.get(i);
                    }
                }

                message += ".";

                callbacks.onFailure(message);

                return;
            }

            // ---> Decoding ROUTES data
            String routesString = directionsObj.getString("routes");
            JSONArray routesArr = new JSONArray(routesString);

            String route0String = routesArr.getString(0);

            JSONObject route0Obj = new JSONObject(route0String);

                // ---> Decoding ROUTES > BOUNDS data
                String boundsString = route0Obj.getString("bounds");
                JSONObject boundsObj = new JSONObject(boundsString);

                    // ---> Decoding ROUTES > BOUNDS > NORTHEAST data
                    String northeastString = boundsObj.getString("northeast");
                    JSONObject northeastObj = new JSONObject(northeastString);

                    String northeastLatString = northeastObj.getString("lat");
                    String northeastLngString = northeastObj.getString("lng");

                    // ---> Decoding ROUTES > BOUNDS > SOUTHWEST data
                    String southwestString = boundsObj.getString("southwest");
                    JSONObject southwestObj = new JSONObject(southwestString);

                    String southwestLatString = southwestObj.getString("lat");
                    String southwestLngString = southwestObj.getString("lng");

                    Log.d(TAG, "decodeGoogleDirectionsData: directions boundaries decoded " +
                            "as - SOUTHWEST (" + southwestLatString + " / " + southwestLngString +
                    "), NORTHEAST (" + northeastLatString + " / " + northeastLngString + ")");

                    LatLng southWest = new LatLng(
                            Double.parseDouble(southwestLatString),
                            Double.parseDouble(southwestLngString)
                    );

                    LatLng northEast = new LatLng(
                            Double.parseDouble(northeastLatString),
                            Double.parseDouble(northeastLngString)
                    );

                    mBoundaries = new LatLngBounds(southWest, northEast);

                // ---> Decoding ROUTES > LEGS > STEPS data
                String legsString = route0Obj.getString("legs");
                JSONArray legsArr = new JSONArray(legsString);
                String legs0String = legsArr.getString(0);
                JSONObject legs0Obj = new JSONObject(legs0String);

                String stepsString = legs0Obj.getString("steps");

                JSONArray stepsArr = new JSONArray(stepsString);

                for (int i = 0; i<stepsArr.length(); i++) {

                    JSONObject stepsArrElement = stepsArr.getJSONObject(i);

                    String startLocationString = stepsArrElement.getString("start_location");

                    JSONObject startLocationObj = new JSONObject(startLocationString);

                    String startLocationLat = startLocationObj.getString("lat");
                    String startLocationLng = startLocationObj.getString("lng");

                    Log.d(TAG, "decodeGoogleDirectionsData: waypoint extracted from step " +
                            "number " + i + " (" + startLocationLat + " / " + startLocationLng +
                            ")");

                    LatLng newWaypoint = new LatLng(
                            Double.parseDouble(startLocationLat),
                            Double.parseDouble(startLocationLng)
                    );

                    mWayPoints.add(newWaypoint);

                    if (i != stepsArr.length()-1) continue;

                    String endLocationString = stepsArrElement.getString("end_location");

                    JSONObject endLocationObj = new JSONObject(endLocationString);

                    String endLocationLat = endLocationObj.getString("lat");
                    String endLocationLng = endLocationObj.getString("lng");

                    Log.d(TAG, "decodeGoogleDirectionsData: final waypoint extracted " +
                            "from step number " + i + " (" + endLocationLat + " / " +
                            endLocationLng +
                            ")");

                    LatLng finalWaypoint = new LatLng(
                            Double.parseDouble(startLocationLat),
                            Double.parseDouble(startLocationLng)
                    );

                    mWayPoints.add(finalWaypoint);
                }

                callbacks.onSuccess();
        }

        catch (Exception e) {

            Log.d(TAG, "decodeGoogleDirectionsData: could not decode directions API " +
                    "JSON Object. It's structure might have changed.");

            e.printStackTrace();

            callbacks.onFailure("");
        }
    }

    private static boolean isStatusNotOK() {

        if (mOverallStatus != "OK") return false;

        for (String waypoint : mGeocodedWaypointsStatus) {
            if (!waypoint.equals("OK")) return false;
        }

        return true;
    }
}

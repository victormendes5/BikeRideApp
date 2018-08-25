package com.infnet.bikeride.bikeride.services;

import android.util.Log;

import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class GoogleDistanceMatrixAPI {

    /*=======================================================================================
                                            INSTRUCTIONS
     =======================================================================================*/

    //region INSTRUCTIONS

    // https://developers.google.com/maps/documentation/distance-matrix/intro

    //endregion


    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    private static final String TAG = "GoogleDistanceMatrixAPI";

    //region CONSTANTS


    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private static String mEstimatesPickupDistance = "";
    private static String mEstimatesPickupDuration = "";
    private static String mEstimatesDeliveryDistance = "";
    private static String mEstimatesDeliveryDuration = "";

    //endregion


    /*=======================================================================================
                                              GETTERS
     =======================================================================================*/

    //region GETTERS

    public static String getPickupDistanceEstimate() {
        return mEstimatesPickupDistance;
    }

    public static String getPickupDurationEstimate() {
        return mEstimatesPickupDuration;
    }

    public static String getDeliveryDistanceEstimate() {
        return mEstimatesDeliveryDistance;
    }

    public static String getDeliveryDurationEstimate() {
        return mEstimatesDeliveryDuration;
    }

    //endregion


    /*=======================================================================================
                                             INTERFACES
     =======================================================================================*/

    //region INTERFACES

    public interface OnDistanceMatrixComplete {
        void OnSuccess();
        void OnFailure();
    }

    private interface OnDistanceMatrixDecodeComplete {
        void OnSuccess();
        void OnFailure();
        void OnDecodeFailure();
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                      LOGIC                                       |
       |                                                                                  |
       \=================================================================================*/

    public static void getGoogleDistanceMatrixData(String bikerAddress,
                                            String pickupAddress,
                                            String deliveryAddress,
                                            final OnDistanceMatrixComplete callbacks) {

        Log.d(TAG, "getGoogleDistanceMatrixData: getting Google Distance Matrix API " +
                "data ...");
        Log.d(TAG, "getGoogleDistanceMatrixData: building Google Distance API request " +
                "string ...");

        String distanceMatrixBaseUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?";
        String unitType = "metric";  // "metric" or "imperial"
        String travelMode = "bicycling"; // "walking", "bicycling", "driving"

        while (bikerAddress.contains(" "))
            bikerAddress = bikerAddress.replace(" ", "+");

        while (pickupAddress.contains(" "))
            pickupAddress = pickupAddress.replace(" ", "+");

        while (deliveryAddress.contains(" "))
            deliveryAddress = deliveryAddress.replace(" ", "+");

        String executeString = distanceMatrixBaseUrl
                + "units=" + unitType + "&"
                + "origins=" + bikerAddress + "|" + pickupAddress + "&"
                + "destinations=" + pickupAddress + "|" + deliveryAddress + "&"
                + "mode=" + travelMode + "&"
                + "key=" + Constants.Keys.GOOGLE_API;

        Log.d(TAG, "getGoogleDistanceMatrixData: directions API request string is - " +
                executeString);

        Log.d(TAG, "getGoogleDistanceMatrixData: performing Http request ...");

        new HttpRequest(

            new HttpRequest.Callbacks() {

                @Override
                public String OnComplete(String data) {

                    Log.d(TAG, "getGoogleDistanceMatrixData: successfully retrieved " +
                            "JSON object from Google Distance Matrix API. Decoding content ...");

                    decodeGoogleDistanceMatrixData(data,

                        new OnDistanceMatrixDecodeComplete() {

                            @Override
                            public void OnSuccess() {

                                Log.d(TAG, "getGoogleDistanceMatrixData: successfully " +
                                            "aquired data from Google DistanceMatrix API.");

                                callbacks.OnSuccess();
                            }

                            @Override
                            public void OnFailure() {

                                Log.d(TAG, "getGoogleDistanceMatrixData: FAILED! Data " +
                                        "sent to or received from Distance Matrix API might have " +
                                        "presented some anomaly.");

                                callbacks.OnFailure();
                            }

                            @Override
                            public void OnDecodeFailure() {

                                Log.d(TAG, "getGoogleDistanceMatrixData: could not decode " +
                                        "Google Distance Matrix object. Data structure might " +
                                        "have changed.");

                                callbacks.OnFailure();

                            }
                    });

                    return null;
                }

                @Override
                public String OnFailure(Exception e) {

                    callbacks.OnFailure();
                    return null;
                }

        }).execute(executeString);
    }

    private static void decodeGoogleDistanceMatrixData (String s,
                                                 OnDistanceMatrixDecodeComplete callbacks) {

        Log.d(TAG, "decodeGoogleDistanceMatrixData: decoding serialized JSON object ...");

        String statusOverall = "";
        String statusPickup = "";
        String statusDelivery = "";

        String pickupDistance = "";
        String pickupDuration = "";
        String deliveryDistance = "";
        String deliveryDuration = "";

        try {

            // ---> Decode "rows" array
            JSONObject distanceMatrixObj = new JSONObject(s);
            String distanceMatrixRows = distanceMatrixObj.getString("rows");
            JSONArray distanceMatrixRowsArr = new JSONArray(distanceMatrixRows);

            statusOverall = distanceMatrixObj.getString("status");

            JSONObject distanceMatrixElements = distanceMatrixRowsArr.getJSONObject(0);
            String distanceMatrixDetails = distanceMatrixElements.getString("elements");
            JSONArray distanceMatrixDetailsArr = new JSONArray(distanceMatrixDetails);
            JSONObject distanceObject =  distanceMatrixDetailsArr.getJSONObject(0);


            statusPickup = distanceObject.getString("status");

            String distanceMatrixDistance = distanceObject.getString("distance");
            JSONObject distanceMatrixDistanceText = new JSONObject(distanceMatrixDistance);
            pickupDistance = distanceMatrixDistanceText.getString("text");

            String distanceMatrixDuration = distanceObject.getString("duration");
            JSONObject distanceMatrixDurationText = new JSONObject(distanceMatrixDuration);
            pickupDuration = distanceMatrixDurationText.getString("text");


            JSONObject distanceMatrixElements2 = distanceMatrixRowsArr.getJSONObject(1);
            String distanceMatrixDetails2 = distanceMatrixElements2.getString("elements");
            JSONArray distanceMatrixDetailsArr2 = new JSONArray(distanceMatrixDetails2);
            JSONObject distanceObject2 =  distanceMatrixDetailsArr2.getJSONObject(1);

            statusDelivery = distanceObject2.getString("status");

            String distanceMatrixDistance2 = distanceObject2.getString("distance");
            JSONObject distanceMatrixDistanceText2 = new JSONObject(distanceMatrixDistance2);
            deliveryDistance = distanceMatrixDistanceText2.getString("text");

            String distanceMatrixDuration2 = distanceObject2.getString("duration");
            JSONObject distanceMatrixDurationText2 = new JSONObject(distanceMatrixDuration2);
            deliveryDuration = distanceMatrixDurationText2.getString("text");


        } catch (JSONException e) {

            Log.d(TAG, "decodeGoogleDistanceMatrixData: Could not find one or more " +
                    "properties on retrieved JSON Object, or type might have changed. Check " +
                    "details on stack trace below:");

            e.printStackTrace();

            callbacks.OnDecodeFailure();

            return;
        }

        Log.d(TAG, "decodeGoogleDistanceMatrixData: successfully decoded JSON object.");
        Log.d(TAG, "decodeGoogleDistanceMatrixData: analyzing JSON object content ...");

        if (statusOverall.equals("OK") &&
                statusPickup.equals("OK") &&
                statusDelivery.equals("OK") &&
                !pickupDistance.equals("") &&
                !pickupDuration.equals("") &&
                !deliveryDistance.equals("") &&
                !deliveryDuration.equals("")) {

            Log.d(TAG, "decodeGoogleDistanceMatrixData: information is complete and " +
                    "can be trusted (pickupDistance: " +
                    pickupDistance + " / pickupDuration: " + pickupDuration + " / " +
                    "deliveryDistance: " + deliveryDistance + " / deliveryDuration: " +
                    deliveryDuration + ").");

            mEstimatesPickupDistance = pickupDistance;
            mEstimatesPickupDuration = pickupDuration;
            mEstimatesDeliveryDistance = deliveryDistance;
            mEstimatesDeliveryDuration = deliveryDuration;

            callbacks.OnSuccess();
        }

        else {

            Log.d(TAG, "decodeGoogleDistanceMatrixData: Information is incomplete " +
                    "and cannot be trusted. Check status below: ");

            Log.d(TAG, "decodeGoogleDistanceMatrixData: Overall Status: " + statusOverall +
                    " / Pickup Status: " + statusPickup + " / Delivery Status: " + statusDelivery);

            callbacks.OnFailure();
        }

    }
}

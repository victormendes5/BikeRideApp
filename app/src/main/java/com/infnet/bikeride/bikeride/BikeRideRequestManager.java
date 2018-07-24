package com.infnet.bikeride.bikeride;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BikeRideRequestManager {

    private static final String TAG = "BikeRideRequestManager";

    private String mBikerLocation;
    private String mPickupLocation;
    private String mDeliveryLocation;

    private String mEstimatesPickupDistance = "";
    private String mEstimatesPickupDuration = "";
    private String mEstimatesDeliveryDistance = "";
    private String mEstimatesDeliveryDuration = "";

    public String getPickupDistanceEstimate() {
        return mEstimatesPickupDistance;
    }

    public String getPickupDurationEstimate() {
        return mEstimatesPickupDuration;
    }

    public String getDeliveryDistanceEstimate() {
        return mEstimatesDeliveryDistance;
    }

    public String getDeliveryDurationEstimate() {
        return mEstimatesDeliveryDuration;
    }

    public String getFeeEstimate () {

        String feeEstimate = "null";

        double pickupFeePerKM = 1;
        double pickupFeePerMinute = 0.2;
        double deliveryFeePerKM = 1.5;
        double deliveryFeePerMinute = 0.3;

        double pickupDistance =
                Double.valueOf(mEstimatesPickupDistance.replace("km", "").trim());

        double deliveryDistance =
                Double.valueOf(mEstimatesDeliveryDistance.replace("km", "").trim());

        double totalFee = 0;

        totalFee += pickupDistance * pickupFeePerKM;
        totalFee += deliveryDistance * deliveryFeePerKM;

        feeEstimate = "R$" + String.format ("%.2f", totalFee).replace(".", ",");

        Log.i(TAG, "getFeeEstimate: estimated fee is - " + feeEstimate);

        return feeEstimate;
    }

    public String getBikerLocation() {
        Log.i(TAG, "getBikerLocation: Biker Location is - " + mBikerLocation);
        return mBikerLocation;
    }

    public String getPickupLocation() {
        Log.i(TAG, "getPickupLocation: Pickup Location is - " + mPickupLocation);
        return mPickupLocation;
    }

    public String getPickupLocationShort() {
        Log.i(TAG, "getPickupLocationShort: Pickup Location short is - " +
                mPickupLocation.substring(0, 40) + "...");
        return mPickupLocation.substring(0, 40) + "...";
    }

    public String getDeliveryLocation() {
        Log.i(TAG, "getDeliveryLocation: Delivery Location is - " + mDeliveryLocation);
        return mDeliveryLocation;
    }

    public String getDeliveryLocationShort() {
        Log.i(TAG, "getDeliveryLocationShort: Delivery Location short is - "
                + mDeliveryLocation.substring(0, 40) + "...");
        return mDeliveryLocation.substring(0, 40) + "...";
    }

    public void setBikerLocation(String mBikerLocation) {
        Log.i(TAG, "setBikerLocation: setting Biker location to - " + mBikerLocation);
        this.mBikerLocation = mBikerLocation;
    }

    public void setPickupLocation(String mPickupLocation) {
        Log.i(TAG, "setPickupLocation: setting Pickup location to - " + mPickupLocation);
        this.mPickupLocation = mPickupLocation;
    }

    public void setDeliveryLocation(String mDeliveryLocation) {
        Log.i(TAG, "setDeliveryLocation: setting Delivery location to - " + mDeliveryLocation);
        this.mDeliveryLocation = mDeliveryLocation;
    }

    public boolean setEstimatesFromWebData (String s) {

        Log.i(TAG, "setDistanceAndDurationFromWebData: decoding serialized JSON object.");

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
            mEstimatesPickupDistance = distanceMatrixDistanceText.getString("text");
            String distanceMatrixDuration = distanceObject.getString("duration");
            JSONObject distanceMatrixDurationText = new JSONObject(distanceMatrixDuration);
            mEstimatesPickupDuration = distanceMatrixDurationText.getString("text");

            JSONObject distanceMatrixElements2 = distanceMatrixRowsArr.getJSONObject(1);
            String distanceMatrixDetails2 = distanceMatrixElements2.getString("elements");
            JSONArray distanceMatrixDetailsArr2 = new JSONArray(distanceMatrixDetails2);
            JSONObject distanceObject2 =  distanceMatrixDetailsArr2.getJSONObject(1);
            String distanceMatrixDistance2 = distanceObject2.getString("distance");
            JSONObject distanceMatrixDistanceText2 = new JSONObject(distanceMatrixDistance2);
            mEstimatesDeliveryDistance = distanceMatrixDistanceText2.getString("text");
            String distanceMatrixDuration2 = distanceObject2.getString("duration");
            JSONObject distanceMatrixDurationText2 = new JSONObject(distanceMatrixDuration2);
            mEstimatesDeliveryDuration = distanceMatrixDurationText2.getString("text");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "setDistanceAndDurationFromWebData: " + mEstimatesPickupDistance + " " +
                mEstimatesPickupDuration + " " + mEstimatesDeliveryDistance + " " + mEstimatesDeliveryDuration);

        return true;
    }
}

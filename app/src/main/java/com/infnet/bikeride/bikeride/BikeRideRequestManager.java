package com.infnet.bikeride.bikeride;

import android.util.Log;

import java.util.ArrayList;

public class BikeRideRequestManager {

    private static final String TAG = "BikeRideRequestManager";

    private ArrayList<String> mEstimatesList = new ArrayList<String>();

    private String mBikerLocation;
    private String mPickupLocation;
    private String mDeliveryLocation;

    public int addResultsToEstimatesList(String source, String destination) {

        mEstimatesList.add(source);
        mEstimatesList.add(destination);

        return mEstimatesList.size();
    }

    public void clearEstimatesList () {
        mEstimatesList.clear();
    }

    public String getPickupDistanceEstimate() {
        if (mEstimatesList.size() > 0) {
            return mEstimatesList.get(0);

        }
        return null;
    }

    public String getPickupDurationEstimate() {
        if (mEstimatesList.size() > 0) {
            return mEstimatesList.get(1);

        }
        return null;
    }

    public String getDeliveryDistanceEstimate() {
        if (mEstimatesList.size() > 0) {
            return mEstimatesList.get(2);

        }
        return null;
    }

    public String getDeliveryDurationEstimate() {
        if (mEstimatesList.size() > 0) {
            return mEstimatesList.get(3);

        }
        return null;
    }

    public String getFeeEstimate () {

        String feeEstimate = "null";

        double pickupFeePerKM = 1;
        double pickupFeePerMinute = 0.2;
        double deliveryFeePerKM = 1.5;
        double deliveryFeePerMinute = 0.3;

        double pickupDistance =
                Double.valueOf(mEstimatesList.get(0).replace("km", "").trim());

        double deliveryDistance =
                Double.valueOf(mEstimatesList.get(2).replace("km", "").trim());

        double totalFee = 0;

        if (mEstimatesList.size() > 0) {

            totalFee += pickupDistance * pickupFeePerKM;
            totalFee += deliveryDistance * deliveryFeePerKM;

            feeEstimate = "R$" + String.format ("%.2f", totalFee).replace(".", ",");

            Log.i(TAG, "getFeeEstimate: estimated fee is - " + feeEstimate);

            return feeEstimate;
        }

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
}

package com.infnet.bikeride.bikeride.activitydelivery;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.RequestModel;
import com.infnet.bikeride.bikeride.services.BRLocations;
import com.infnet.bikeride.bikeride.services.GoogleDirectionsAPI;
import com.infnet.bikeride.bikeride.services.GoogleMapsAPI;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;


public class DeliveryManager extends FirebaseAccess {


    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    private static final String TAG = "DeliveryManager";


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private AppCompatActivity mReferredActivity;
    private RequestModel mRequestContract = new RequestModel();

    // ---> Locations API
    private BRLocations mLocation;

    // ---> Google APIs
    private GoogleMapsAPI mGoogleMaps;

    // ---> Control variables
    private boolean mIsBiker = true;
    private boolean mIsDirectionsCalculating = false;


    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public DeliveryManager(Context context) {

        Log.d(TAG, "DeliveryManager: initializing delivery process ...");

        mReferredActivity = (AppCompatActivity) context;

        getBundledData();

        initializeGoogleMap();
    }

    //endregion


      /*====================================================================================\
     /                                                                                       \
    (                                      COMMON LOGIC                                       )
     \                                                                                       /
      \====================================================================================*/

    //region COMMON LOGIC

    private void getBundledData () {

        Bundle extras = mReferredActivity.getIntent().getExtras();

        if (extras != null) {

            String isBiker = extras.getString("isBiker");

            if (isBiker.equals("true")) {

                Log.d(TAG, "getBundledData: user recognized as BIKER.");

                mIsBiker = true;
            }

            else if (isBiker.equals("false")) {

                Log.d(TAG, "getBundledData: user recognized as REQUESTER.");

                mIsBiker = false;
            }

            mRequestContract = (RequestModel) mReferredActivity.getIntent()
                    .getSerializableExtra("requestData");
        }
    }

    private void initializeGoogleMap () {

        Log.d(TAG, "initializeGoogleMap: initializing map ...");

        mGoogleMaps = new GoogleMapsAPI(mReferredActivity, Constants.ViewId.MAP,

            new GoogleMapsAPI.Maps() {

                @Override
                public void OnMapReady() {

                    Log.d(TAG, "initializeGoogleMap: map is ready.");

                    mGoogleMaps.setPadding(0, 0, 0, 96);

                    digestRequestContractData();
                }
            });
    }

    private void digestRequestContractData () {

        Double[] applicableCoordinates = {
                mRequestContract.bikerPositionLatitude,
                mRequestContract.bikerPositionLongitude,
                mRequestContract.pickupAddressLatitude,
                mRequestContract.pickupAddressLongitude,
                mRequestContract.deliveryAddressLatitude,
                mRequestContract.deliveryAddressLongitude
        };

        placeMarkersOnMap();
        mGoogleMaps.centerMapWithinArea(applicableCoordinates);
        broadcastLocationIfBiker();
        setListenerToRequestContract();
        drawPathAndCenterWithinArea(applicableCoordinates);
    }

    private void placeMarkersOnMap() {

        Log.d(TAG, "placeMarkersOnMap: placing markers on map ...");

        mGoogleMaps.addMarkerOnMap(

            new LatLng(
                    mRequestContract.pickupAddressLatitude,
                    mRequestContract.pickupAddressLongitude), "");

        mGoogleMaps.addMarkerOnMap(

            new LatLng(
                    mRequestContract.deliveryAddressLatitude,
                    mRequestContract.deliveryAddressLongitude), "");

        mGoogleMaps.addBikerMarkerOnMap(

            new LatLng(
                    mRequestContract.bikerPositionLatitude,
                    mRequestContract.bikerPositionLongitude));

    }

    private void setListenerToRequestContract () {

        Log.d(TAG, "setListenerToRequestContract: starting to listen to request contract " +
                "changes ...");

        setListenerToObjectOrProperty(RequestModel.class,

            new ListenToChanges<RequestModel>() {

                @Override
                public void onChange(RequestModel data) {

                    Log.d(TAG, "setListenerToRequestContract: request contract update " +
                            "detected.");

                    mRequestContract = data;

                    onRequestContractUpdate();
                }

                @Override
                public void onError(RequestModel data) {

                    Log.d(TAG, "setListenerToRequestContract: some error occurred while " +
                            "listening to request contract changes.");
                }

        }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    private void onRequestContractUpdate () {

        moveBikerOnMap();

        // ---> Pickup has NOT yet been confirmed.
        if (!isPickupConfirmed()) {

            Log.d(TAG, "onRequestContractUpdate: Executing pickup flow ...");

            drawPathAndCenterWithinArea (
                    mRequestContract.bikerPositionLatitude,
                    mRequestContract.bikerPositionLongitude,
                    mRequestContract.pickupAddressLatitude,
                    mRequestContract.pickupAddressLongitude,
                    mRequestContract.deliveryAddressLatitude,
                    mRequestContract.deliveryAddressLongitude
            );
        }

        // ---> Pickup has been been confirmed.
        else {

            Log.d(TAG, "onRequestContractUpdate: Executing delivery flow ...");

            drawPathAndCenterWithinArea (
                    mRequestContract.bikerPositionLatitude,
                    mRequestContract.bikerPositionLongitude,
                    mRequestContract.deliveryAddressLatitude,
                    mRequestContract.deliveryAddressLongitude
            );
        }
    }

    //endregion


               /*---------------------------------------------------------------\
                                          HELPER METHODS
               \---------------------------------------------------------------*/

    //region HELPER METHODS

    private void moveBikerOnMap () {

        mGoogleMaps.moveBikerMarkerOnMap(

            new LatLng(

                    mRequestContract.bikerPositionLatitude,
                    mRequestContract.bikerPositionLongitude));
    }

    private void drawPathAndCenterWithinArea(Double... latLongs) {

        mGoogleMaps.centerMapWithinArea(latLongs);

        if (mIsDirectionsCalculating) return;

        mIsDirectionsCalculating = true;

        GoogleDirectionsAPI.getGoogleDirectionsData (

            new GoogleDirectionsAPI.OnDirectionsComplete() {

                @Override
                public void onSuccess(LatLngBounds boundaries, ArrayList<LatLng> waypoints) {

                    mIsDirectionsCalculating = false;

                    mGoogleMaps.drawPolylines(boundaries, waypoints);
                }

                @Override
                public void onFailure() {

                    mIsDirectionsCalculating = false;
                }

            }, latLongs);
    }

    private boolean isPickupConfirmed () {

        if (!mRequestContract.confirmedPickupByBiker || !mRequestContract.confirmedPickupByUser)
            return false;

        return true;
    }

    //endregion


      /*====================================================================================\
     /                                                                                       \
    (                                       BIKER LOGIC                                       )
     \                                                                                       /
      \====================================================================================*/

               /*---------------------------------------------------------------\
                                     BROADCAST BIKER LOCATION
               \---------------------------------------------------------------*/

    //region BROADCAST BIKER LOCATION

    private void broadcastLocationIfBiker() {

        if (!mIsBiker) return;

        Log.d(TAG, "broadcastLocationIfBiker: starting to broadcast this " +
                "biker's position on request contract ...");

        mLocation = new BRLocations(mReferredActivity,

            new BRLocations.OnLocationChanged() {

                @Override
                public void OnChange() {

                    if (!validateLocationChange()) return;

                    Log.d(TAG, "broadcastLocationIfBiker: location change " +
                            "event triggered on this device with new biker position.");

                    Log.d(TAG, "broadcastLocationIfBiker: updating this " +
                            "biker's position on request contract (Lat: " +
                            mLocation.getLatitude() + " / Lng: " +  mLocation.getLongitude()
                            + ") ...");

                    updateMutableDataOnCondition(RequestModel.class, mRequestContract,

                        new Condition<RequestModel>() {

                            @Override
                            public boolean ExecuteIf(RequestModel data) {

                                mRequestContract = data;

                                mRequestContract.bikerPositionLatitude = mLocation.getLatitude();
                                mRequestContract.bikerPositionLongitude = mLocation.getLongitude();
                                mRequestContract.updateTime = getCurrentISODateTime();

                                return true;
                            }
                        },

                        new OnComplete<RequestModel>() {

                            @Override
                            public void onSuccess(RequestModel data) {

                                Log.d(TAG, "broadcastLocationIfBiker: successfully updated "
                                        + "this biker's location on request contract.");
                            }

                            @Override
                            public void onFailure(RequestModel data) {

                                Log.d(TAG, "broadcastLocationIfBiker: failed to update this "
                                        + "biker's location on request contract.");
                            }

                        }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
                }
            });
    }

    private boolean validateLocationChange () {

        if (    mLocation.getLatitude()  == null ||
                mLocation.getLatitude()  == 0 ||
                mLocation.getLongitude() == null ||
                mLocation.getLongitude() == 0) {

            // Log.d(TAG, "broadcastLocationIfBiker: location " +
            //        "change event triggered on this device, but API returned invalid " +
            //        "values.");

            return false;
        }

        if (mLocation.getLatitude() == mRequestContract.bikerPositionLatitude &&
                mLocation.getLongitude() == mRequestContract.bikerPositionLongitude) {

            Log.d(TAG, "broadcastLocationIfBiker: location " +
                    "change event triggered on this device, but position stayed " +
                    "the same.");

            return false;
        }

        return true;
    }

    //endregion


    /*=======================================================================================
                                              OTHER
     =======================================================================================*/

    //region OTHER METHODS

    public void verifyPermissionsRequestResult (int requestCode, int[] grantResults) {

        mGoogleMaps.verifyPermissionRequestResult(requestCode, grantResults);
    }

    private String getCurrentISODateTime () {
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName());

        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    //endregion

}

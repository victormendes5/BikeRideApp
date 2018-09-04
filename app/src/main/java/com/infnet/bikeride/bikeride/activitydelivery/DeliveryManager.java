package com.infnet.bikeride.bikeride.activitydelivery;

import android.content.Context;
import android.location.Location;
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

    private static final float PICKUP_AND_DELIVERY_RANGE_IN_METERS = 100f;


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
    private OnUiUpdate mCallbacks;


    /*=======================================================================================
                                          GETTERS & SETTERS
     =======================================================================================*/

    public boolean isBiker () { return mIsBiker; }
    public RequestModel getRequestContract () { return mRequestContract; }


    /*=======================================================================================
                                             INTERFACES
     =======================================================================================*/

    public interface OnUiUpdate {
        void isUnderWay();
        void onBikerIsAtPickupAddress();
        void onBikerIsAtDeliveryAddress();
        void onCancel();
        void onComplete();
        void onFinish();
    }


    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public DeliveryManager(Context context, OnUiUpdate callbacks) {

        Log.d(TAG, "DeliveryManager: initializing delivery process ...");

        mReferredActivity = (AppCompatActivity) context;

        mCallbacks = callbacks;

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

            Log.d(TAG, "getBundledData: " + mRequestContract.toString());

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

    private void placeMarkersOnMap () {

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

                    onRequestContractUpdate(data);
                }

                @Override
                public void onError(RequestModel data) {

                    if (data == null) {

                        Log.d(TAG, "setListenerToRequestContract: request contract has " +
                                "been cancelled (deleted).");

                        mCallbacks.onCancel();

                        return;
                    }

                    Log.d(TAG, "setListenerToRequestContract: some error occurred while " +
                            "listening to request contract changes.");
                }

        }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    private void onRequestContractUpdate (RequestModel data) {

        mRequestContract = data;

        moveBikerOnMap();

        // ---> Pickup has NOT yet been confirmed.
        if (!isPickupConfirmed() && !isDeliveryConfirmed()) {

            Log.d(TAG, "onRequestContractUpdate: Executing pickup flow ...");

            drawPathAndCenterWithinArea (
                    mRequestContract.bikerPositionLatitude,
                    mRequestContract.bikerPositionLongitude,
                    mRequestContract.pickupAddressLatitude,
                    mRequestContract.pickupAddressLongitude,
                    mRequestContract.deliveryAddressLatitude,
                    mRequestContract.deliveryAddressLongitude
            );

            if (isBikerWithinPickupRange())  mCallbacks.onBikerIsAtPickupAddress();
            else                             mCallbacks.isUnderWay();
        }

        // ---> Pickup has been confirmed.
        else if (isPickupConfirmed() && !isDeliveryConfirmed()) {

            Log.d(TAG, "onRequestContractUpdate: Executing delivery flow ...");

            drawPathAndCenterWithinArea (
                    mRequestContract.bikerPositionLatitude,
                    mRequestContract.bikerPositionLongitude,
                    mRequestContract.deliveryAddressLatitude,
                    mRequestContract.deliveryAddressLongitude
            );

            if (isBikerWithinPickupRange())   mCallbacks.onBikerIsAtPickupAddress();
            if (isBikerWithinDeliveryRange()) mCallbacks.onBikerIsAtDeliveryAddress();
            else                              mCallbacks.isUnderWay();
        }

        // ---> Delivery has been confirmed.
        else if (isPickupConfirmed() && isDeliveryConfirmed()) {

            Log.d(TAG, "onRequestContractUpdate: Executing delivery completed flow ...");

            mCallbacks.onComplete();
        }
    }

    public void cancelRequest () {

        Log.d(TAG, "onRequestCancel: cancelling request contract by deleting it from " +
                "Deliveries node ...");

        delete(

            new OnCompleteVoid() {

                @Override
                public void onSuccess() {

                    Log.d(TAG, "onRequestCancel: successfully cancelled request contract " +
                            "by deleting it from deliveries node.");
                }

                @Override
                public void onFailure() {

                    Log.d(TAG, "onRequestCancel: could not delete request contract " +
                            "from Deliveries node.");
                }

        }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
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

        Log.d(TAG, "drawPathAndCenterWithinArea: fetching directions API data ...");

        GoogleDirectionsAPI.getData(latLongs,

                new GoogleDirectionsAPI.OnDirectionsComplete() {

                    @Override
                    public void onSuccess(LatLngBounds boundaries, ArrayList<LatLng> waypoints) {

                        mIsDirectionsCalculating = false;

                        Log.d(TAG, "drawPathAndCenterWithinArea: directions API data updated. " +
                                "Drawing path ...");

                        mGoogleMaps.drawPolylines(waypoints);
                    }

                    @Override
                    public void onFailure() {

                        mIsDirectionsCalculating = false;
                    }

                });
    }

    public boolean isPickupConfirmed () {

        if (!mRequestContract.confirmedPickupByBiker || !mRequestContract.confirmedPickupByUser)
            return false;

        return true;
    }

    public boolean isDeliveryConfirmed () {

        if (!mRequestContract.confirmedDeliveryByBiker || !mRequestContract.confirmedDeliveryByUser)
            return false;

        return true;
    }

    //endregion


      /*====================================================================================\
     /                                                                                       \
    (                                       BIKER LOGIC                                       )
     \                                                                                       /
      \====================================================================================*/

    //region BIKER LOGIC

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

                    updateBikerPositionOnRequestContract();
                }
            });
    }

    private void updateBikerPositionOnRequestContract () {

        Log.d(TAG, "broadcastLocationIfBiker: updating this " +
                "biker's position on request contract (Lat: " +
                mLocation.getLatitude() + " / Lng: " +  mLocation.getLongitude()
                + ") ...");

        mRequestContract.bikerPositionLatitude = mLocation.getLatitude();
        mRequestContract.bikerPositionLongitude = mLocation.getLongitude();
        mRequestContract.updateTime = getCurrentISODateTime();

        updateMutableDataOnCondition(RequestModel.class, mRequestContract,

            new Condition<RequestModel>() {

                @Override
                public boolean ExecuteIf(RequestModel data) {
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

    public void bikerConfirmsPickup () {

        mRequestContract.confirmedPickupByBiker = true;

        Log.d(TAG, "bikerConfirmsPickup: biker has confirmed pickup. Updating request " +
                "contract accordingly ...");

        updateMutableDataOnCondition(RequestModel.class, mRequestContract,

                new Condition<RequestModel>() {

                    @Override
                    public boolean ExecuteIf(RequestModel data) {
                        return true;
                    }
                },

                new OnComplete<RequestModel>() {

                    @Override
                    public void onSuccess(RequestModel data) {

                        Log.d(TAG, "bikerConfirmsPickup: successfully updated "
                                + "this biker's pickup confirmation on request contract.");
                    }

                    @Override
                    public void onFailure(RequestModel data) {

                        Log.d(TAG, "bikerConfirmsPickup: failed to update this "
                                + "biker's pickup confirmation on request contract.");
                    }

                }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    public void bikerConfirmsDelivery () {

        mRequestContract.confirmedDeliveryByBiker = true;

        Log.d(TAG, "bikerConfirmsDelivery: biker has confirmed delivery. Updating request " +
                "contract accordingly ...");

        updateMutableDataOnCondition(RequestModel.class, mRequestContract,

                new Condition<RequestModel>() {

                    @Override
                    public boolean ExecuteIf(RequestModel data) {
                        return true;
                    }
                },

                new OnComplete<RequestModel>() {

                    @Override
                    public void onSuccess(RequestModel data) {

                        Log.d(TAG, "bikerConfirmsDelivery: successfully updated "
                                + "this biker's delivery confirmation on request contract.");
                    }

                    @Override
                    public void onFailure(RequestModel data) {

                        Log.d(TAG, "bikerConfirmsDelivery: failed to update this "
                                + "biker's delivery confirmation on request contract.");
                    }

                }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    public void finishDeliveryBiker () {

        Log.d(TAG, "finishDelivery: wrapping up delivery from biker's perspective ...");

        Log.d(TAG, "finishDeliveryBiker: awaiting deletion of request contract on " +
                "Deliveries node ...");

        removeAllValueEventListeners();

        setListenerToObjectOrProperty(RequestModel.class,

                new ListenToChanges<RequestModel>() {

                    @Override
                    public void onChange(RequestModel data) {


                    }

                    @Override
                    public void onError(RequestModel data) {

                        if (data == null) {

                            removeLastValueEventListener();

                            Log.d(TAG, "finishDeliveryBiker: request contract successfully " +
                                    "deleted on Deliveries node.");

                            Log.d(TAG, "finishDelivery: delivery process completed! " +
                                    "Returning to request activitiy ...");

                            mCallbacks.onFinish();
                        }
                    }

                }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    //endregion


               /*---------------------------------------------------------------\
                                          HELPER METHODS
               \---------------------------------------------------------------*/

    private boolean validateLocationChange () {

        if (    mLocation.getLatitude()  == null ||
                mLocation.getLatitude()  == 0 ||
                mLocation.getLongitude() == null ||
                mLocation.getLongitude() == 0) {

             Log.d(TAG, "broadcastLocationIfBiker: location " +
                    "change event triggered on this device, but API returned invalid " +
                    "values.");

            return false;
        }

        if (mLocation.getLatitude() == mRequestContract.bikerPositionLatitude &&
                mLocation.getLongitude() == mRequestContract.bikerPositionLongitude) {

//            Log.d(TAG, "broadcastLocationIfBiker: location " +
//                    "change event triggered on this device, but position stayed " +
//                    "the same.");

            return false;
        }

        return true;
    }

    public boolean isBikerWithinPickupRange () {

        float distance = getDistanceBetweenCordinates(
                mRequestContract.pickupAddressLatitude,
                mRequestContract.pickupAddressLongitude,
                mRequestContract.bikerPositionLatitude,
                mRequestContract.bikerPositionLongitude);

        if (distance < PICKUP_AND_DELIVERY_RANGE_IN_METERS) {

            if (!isPickupConfirmed()) {

                Log.d(TAG, "isBikerWithinPickupRange: biker ranges " + distance + "m " +
                        "to pickup and IS WITHIN pickup range.");
            }

            return true;
        }

        if (!isPickupConfirmed()) {

            Log.d(TAG, "isBikerWithinPickupRange: biker ranges " + distance + "m to " +
                    "pickup and IS NOT WITHIN pickup range.");
        }

        return false;
    }

    public boolean isBikerWithinDeliveryRange () {

        float distance = getDistanceBetweenCordinates(
                mRequestContract.bikerPositionLatitude,
                mRequestContract.bikerPositionLongitude,
                mRequestContract.deliveryAddressLatitude,
                mRequestContract.deliveryAddressLongitude);

        if (distance < PICKUP_AND_DELIVERY_RANGE_IN_METERS) {

            if (isPickupConfirmed()) {

                Log.d(TAG, "isBikerWithinDeliveryRange: biker ranges " + distance + "m to " +
                        "delivery and IS WITHIN delivery range.");
            }

            return true;
        }

        if (isPickupConfirmed()) {

            Log.d(TAG, "isBikerWithinDeliveryRange: biker ranges " + distance + "m to " +
                    "delivery and IS NOT WITHIN delivery range.");
        }

        return false;
    }

    public boolean bikerConfirmedPickupAndAwaitsRequesterConfirmation() {

        return mRequestContract.confirmedPickupByBiker && !mRequestContract.confirmedPickupByUser;
    }

    public boolean bikerConfirmedDeliveryAndAwaitsRequesterConfirmation() {

        return mRequestContract.confirmedDeliveryByBiker && !mRequestContract.confirmedDeliveryByUser;
    }

    public float getDistanceBetweenCordinates(Double lat1, Double lng1,
                                              Double lat2, Double lng2) {

        Location origin = new Location ("");
        Location destination = new Location ("");

        origin.setLatitude(lat1);
        origin.setLongitude(lng1);

        destination.setLatitude(lat2);
        destination.setLongitude(lng2);

        float distance = origin.distanceTo(destination);

        return distance;
    }


      /*====================================================================================\
     /                                                                                       \
    (                                    REQUESTER LOGIC                                      )
     \                                                                                       /
      \====================================================================================*/

    public void requesterConfirmsPickup () {

        mRequestContract.confirmedPickupByUser = true;

        Log.d(TAG, "requesterConfirmsPickup: requester has confirmed pickup. Updating " +
                "request contract accordingly ...");

        updateMutableDataOnCondition(RequestModel.class, mRequestContract,

                new Condition<RequestModel>() {

                    @Override
                    public boolean ExecuteIf(RequestModel data) {
                        return true;
                    }
                },

                new OnComplete<RequestModel>() {

                    @Override
                    public void onSuccess(RequestModel data) {

                        Log.d(TAG, "requesterConfirmsPickup: successfully updated "
                                + "this requester's pickup confirmation on request contract.");
                    }

                    @Override
                    public void onFailure(RequestModel data) {

                        Log.d(TAG, "requesterConfirmsPickup: failed to update this "
                                + "requester's pickup confirmation on request contract.");
                    }

                }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    public void requesterConfirmsDelivery () {

        mRequestContract.confirmedDeliveryByUser = true;

        Log.d(TAG, "bikerConfirmsDelivery: requester has confirmed delivery. Updating " +
                "request contract accordingly ...");

        updateMutableDataOnCondition(RequestModel.class, mRequestContract,

                new Condition<RequestModel>() {

                    @Override
                    public boolean ExecuteIf(RequestModel data) {
                        return true;
                    }
                },

                new OnComplete<RequestModel>() {

                    @Override
                    public void onSuccess(RequestModel data) {

                        Log.d(TAG, "bikerConfirmsDelivery: successfully updated "
                                + "this requester's delivery confirmation on request contract.");
                    }

                    @Override
                    public void onFailure(RequestModel data) {

                        Log.d(TAG, "bikerConfirmsDelivery: failed to update this "
                                + "requester's delivery confirmation on request contract.");
                    }

                }, Constants.ChildName.DELIVERIES, mRequestContract.userId);
    }

    public void finishDeliveryRequester () {

        Log.d(TAG, "finishDelivery: wrapping up delivery from requester's perspective ...");

        removeAllValueEventListeners();

        addUsingKey(mRequestContract, new OnCompleteKey() {

            @Override
            public void onSuccess(String key) {

                Log.d(TAG, "finishDelivery: successfully copied request contract to " +
                        "this requester's history.");

                addUsingKey(mRequestContract, new OnCompleteKey() {

                    @Override
                    public void onSuccess(String key) {

                        Log.d(TAG, "finishDelivery: successfully copied request contract " +
                                "to this biker's history.");

                        delete(new OnCompleteVoid() {

                            @Override
                            public void onSuccess() {

                                Log.d(TAG, "finishDelivery: successfully removed request " +
                                        "contract from Deliveries node.");

                                Log.d(TAG, "finishDelivery: delivery process completed! " +
                                        "Returning to request activitiy ...");

                                mCallbacks.onFinish();

                            }

                            @Override
                            public void onFailure() {

                                Log.d(TAG, "finishDelivery: could not remove request " +
                                        "contract from Deliveries node.");

                                mCallbacks.onFinish();
                            }

                        }, Constants.ChildName.DELIVERIES, mRequestContract.userId);

                    }

                    @Override
                    public void onFailure() {

                        Log.d(TAG, "finishDelivery: failed to copy this request contract " +
                                "to requester's and biker's history.");

                        mCallbacks.onFinish();
                    }

                }, Constants.ChildName.HISTORY, mRequestContract.bikerId);

            }

            @Override
            public void onFailure() {

                Log.d(TAG, "finishDelivery: failed to copy this request contract to " +
                        "requester's and biker's history.");

                mCallbacks.onFinish();
            }

        }, Constants.ChildName.HISTORY, mRequestContract.userId);

    }


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

    public void removeLocationListener() {

        Log.d(TAG, "removeLocationListener: removing location listener ...");

        mLocation.removeLocationListener();
    }

    //endregion

}

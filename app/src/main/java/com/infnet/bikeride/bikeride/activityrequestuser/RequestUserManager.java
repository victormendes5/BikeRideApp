package com.infnet.bikeride.bikeride.activityrequestuser;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.AvailableBikerModel;
import com.infnet.bikeride.bikeride.models.RequestModel;
import com.infnet.bikeride.bikeride.services.CurrentUserData;
import com.infnet.bikeride.bikeride.services.GoogleDistanceMatrixAPI;
import com.infnet.bikeride.bikeride.services.GoogleMapsAPI;
import com.infnet.bikeride.bikeride.services.Mockers;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RequestUserManager extends FirebaseAccess {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "RequestUserManager";

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private AppCompatActivity mReferredActivity;
    private RequestModel mRequest = new RequestModel();

    // ---> Google APIs
    private GoogleMapsAPI mGoogleMaps;

    // ---> Map for active Time outs
    HashMap<Handler, Runnable> mTimeouts = new HashMap<>();

    //endregion


    /*=======================================================================================
                                            INTERFACES
     =======================================================================================*/

    //region INTERFACES

    public interface GetEstimatesResponses {
        void onSuccess();
        void onInvalidAddresses();
        void noBikersAvailable();
        void onError();
    }

    public interface OnDistanceMatrixComplete {
        void OnSuccess(String s);
        void OnFailure(Exception e);
    }

    private interface OnDistanceMatrixDecode {
        void OnSuccess();
        void OnFailure(String status);
        void OnDecodeFailure(JSONException e);
    }

    public interface RequestStatus {
        void onRequestAccepted(RequestModel request);
        void onSearchTimedOut();
        void onRequestCancel();
        void onError();
    }

    public interface OnComplete<T> {
        void onSuccess(T data);
        void onFailure(T data);
    }

    public interface OnCompleteVoid {
        void onSuccess();
        void onFailure();
    }

    //endregion


    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public RequestUserManager(Context context) {

        mReferredActivity = (AppCompatActivity) context;

        mGoogleMaps = new GoogleMapsAPI(mReferredActivity, Constants.ViewId.MAP, new GoogleMapsAPI.Maps() {

            @Override
            public void OnMapReady() {

                mGoogleMaps.centerMapOnDeviceLocation();
            }
        });

        Mockers.mockBikerData();
    }

    //endregion


    /*=======================================================================================
                                         GETTERS & SETTERS
     =======================================================================================*/

    //region GETTERS & SETTERS

    public String getPickupDistanceEstimate() { return mRequest.estimatesPickupDistance; }

    public String getPickupDurationEstimate() { return mRequest.estimatesPickupDuration; }

    public String getDeliveryDistanceEstimate() { return mRequest.estimatesDeliveryDistance; }

    public String getDeliveryDurationEstimate() { return mRequest.estimatesDeliveryDuration; }

    public String getFeeEstimate () { return mRequest.estimatesFee; }

    public String getSendersName () { return mRequest.sendersName; }

    public String getReceiversName () { return mRequest.receiversName; }

    public String getPickupAddress() { return  mRequest.pickupAddress; }

    public String getDeliveryAddress() { return mRequest.deliveryAddress; }

    public String getPickupAddressShort() {

        String location = mRequest.pickupAddress;

        if (location.length() >= 41) {
            location = location.substring(0, 40) + "...";
        }

        return  location;
    }

    public String getDeliveryAddressShort() {

        String location = mRequest.deliveryAddress;

        if (location.length() >= 41) {
            location = location.substring(0, 40) + "...";
        }

        return  location;
    }

    public void setBikerLocation(double latitude, double longitude) {

        Log.d(TAG, "setBikerLocation: setting Biker location to - Latitude: " + latitude +
                " / Longitude: " + longitude);

        mRequest.bikerPositionLatitude = latitude;
        mRequest.bikerPositionLongitude = longitude;
    }

    public void setPickupAddress(String location) {
        Log.i(TAG, "setPickupAddress: setting pickuo address to - " + location);
        mRequest.pickupAddress = location;
    }

    public void setDeliveryAddress(String location) {
        Log.i(TAG, "setDeliveryAddress: setting delivery address to - " + location);
        mRequest.deliveryAddress = location;
    }

    public void setPackageType (String type) {
        Log.i(TAG, "setPackageType: setting package type to - " + type);
        mRequest.packageType = type;
    }

    public void setPackageSize (String size) {
        Log.i(TAG, "setPackageType: setting package size to - " + size);
        mRequest.packageSize = size;
    }

    public void setSendersName (String name) {
        Log.i(TAG, "setSendersName: setting sender's name to - " + name);
        mRequest.sendersName = name;
    }

    public void setReceiversName (String name) {
        Log.i(TAG, "setSendersName: setting receiver's name to - " + name);
        mRequest.receiversName = name;
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                    USER LOGIC                                    |
       |                                                                                  |
       \=================================================================================*/


               /*---------------------------------------------------------------\
                                           GET ESTIMATES
               \---------------------------------------------------------------*/

    //region GET ESTIMATES METHODS

    public void getEstimates (final GetEstimatesResponses callback) {

        Log.d(TAG, "getEstimates: starting getEstimates ...");

        Log.d(TAG, "getEstimates: updating coordinates for given addresses ...");

        setCoordinatesFromAddresses(new OnCompleteVoid() {

            @Override
            public void onSuccess() {

                Log.d(TAG, "getEstimates: Successfully updated coordinates for addresses." +
                        " Getting available bikers ....");

                getClosestAvailableBikers(callback);
            }

            @Override
            public void onFailure() {

                Log.d(TAG, "getEstimates: cannot get coordinates for given addresses." +
                        " Aborting estimates.");

                callback.onInvalidAddresses();

                return;
            }
        });
    }

    private void getClosestAvailableBikers(final GetEstimatesResponses callback) {

        getAll(AvailableBikerModel.class,

            new FirebaseAccess.OnComplete<ArrayList<AvailableBikerModel>>() {

                @Override
                public void onSuccess(ArrayList<AvailableBikerModel> data) {

                    if (data.size() == 0) {
                        Log.d(TAG, "getEstimates: data successfully retrieved from " +
                                "Available Bikers node, but none is available at this " +
                                "moment.");
                        callback.noBikersAvailable();
                        return;
                    }

                    Log.d(TAG, "getEstimates: data successfully retrieved from " +
                            "Available Bikers node, arranging bikers by proximity. (" +
                            data.size() + " bikers found)");

                    data = sortByClosestBiker(data, mRequest.pickupAddressLatitude,
                            mRequest.pickupAddressLongitude);

                    Log.d(TAG, "getEstimates: closest biker is '" +
                            data.get(0).bikerName + "' (ID: " + data.get(0).bikerId + ").");

                    Log.d(TAG, "getEstimates: getting closest biker's street address "
                            + "from coordinates ...");

                    String currentBikerAddress = getAddressFromCoordinates(
                            data.get(0).bikerPositionLatitude,
                            data.get(0).bikerPositionLongitude
                    );

                    if (currentBikerAddress.equals("") || currentBikerAddress == null) {
                        Log.d(TAG, "getEstimates: could not determine closest biker's"
                                + " street address. Aborting getting estimates.");
                        callback.onError();
                        return;
                    }

                    Log.d(TAG, "getEstimates: closest biker street address successfully " +
                            "determined - " + currentBikerAddress);

                    Log.d(TAG, "getEstimates: requesting estimate data from Google " +
                            "Distance Matrix ...");

                    accessGoogleDistanceMatrixAndFinish(currentBikerAddress,
                            callback);
                }

                @Override
                public void onFailure(ArrayList<AvailableBikerModel> data) {

                    Log.d(TAG, "getEstimates: an error has occurred while accessing the " +
                            "database. Aborting getEstimates.");
                    callback.onError();
                }
            }, Constants.ChildName.AVAILABLE_BIKERS);
    }

    private void accessGoogleDistanceMatrixAndFinish (String currentBikerAddress,
                                                      final GetEstimatesResponses callback) {

        GoogleDistanceMatrixAPI.getGoogleDistanceMatrixData(

                currentBikerAddress,
                mRequest.pickupAddress,
                mRequest.deliveryAddress,

                new GoogleDistanceMatrixAPI.OnDistanceMatrixComplete() {

                    @Override
                    public void OnSuccess() {

                        Log.d(TAG, "getEstimates: getEstimates successfully " +
                                            "completed!");

                        mRequest.estimatesPickupDistance =
                                GoogleDistanceMatrixAPI.getPickupDistanceEstimate();
                        mRequest.estimatesPickupDuration =
                                GoogleDistanceMatrixAPI.getPickupDurationEstimate();
                        mRequest.estimatesDeliveryDistance =
                                GoogleDistanceMatrixAPI.getDeliveryDistanceEstimate();
                        mRequest.estimatesDeliveryDuration =
                                GoogleDistanceMatrixAPI.getDeliveryDurationEstimate();

                        setFeeEstimate();

                        callback.onSuccess();
                    }

                    @Override
                    public void OnFailure() {

                        Log.d(TAG, "getEstimates: an error has occurred " +
                            "while accessing the Google Distance Matrix API. " +
                            "Aborting getting estimates.");

                        callback.onError();

                    }
                });
    }

    private void setCoordinatesFromAddresses (final OnCompleteVoid callbacks) {

        Log.d(TAG, "setCoordinatesFromAddresses: getting LatLng coordinates for " +
                "pickup address - " + mRequest.pickupAddress);


        new GetCoordinatesFromAddress(mReferredActivity, new OnComplete<LatLng>() {

            @Override
            public void onSuccess(LatLng data) {

                Log.d(TAG, "setCoordinatesFromAddresses: successfully gotten LatLng " +
                        "coordinates for pickup address (Latitude: " + data.latitude + " / " +
                        "Longitude: " + data.longitude + ")");

                mRequest.pickupAddressLatitude = data.latitude;
                mRequest.pickupAddressLongitude = data.longitude;

                Log.d(TAG, "setCoordinatesFromAddresses: getting LatLng coordinates for " +
                        "delivery address - " + mRequest.deliveryAddress);

                new GetCoordinatesFromAddress(mReferredActivity, new OnComplete<LatLng>() {

                    @Override
                    public void onSuccess(LatLng data) {

                        Log.d(TAG, "setCoordinatesFromAddresses: successfully gotten " +
                                "LatLng coordinates for delivery address (Latitude: " +
                                data.latitude + " / Longitude: " + data.longitude + ")");

                        mRequest.deliveryAddressLatitude = data.latitude;
                        mRequest.deliveryAddressLongitude = data.longitude;

                        callbacks.onSuccess();

                    }

                    @Override
                    public void onFailure(LatLng data) {

                        Log.d(TAG, "setCoordinatesFromAddresses: could not get LatLng " +
                                "coordinates for delivery address.");

                        callbacks.onFailure();

                    }
                }).execute(mRequest.deliveryAddress);

            }

            @Override
            public void onFailure(LatLng data) {

                Log.d(TAG, "setCoordinatesFromAddresses: could not get LatLng coordinates " +
                        "for pickup address.");

                callbacks.onFailure();

            }
        }).execute(mRequest.pickupAddress);
    }

    private ArrayList<AvailableBikerModel> sortByClosestBiker (
            ArrayList<AvailableBikerModel> array,
            final double lat,
            final double lon) {

        Collections.sort(array,

            new Comparator<AvailableBikerModel>() {

                @Override
                public int compare(AvailableBikerModel b1,
                                   AvailableBikerModel b2) {

                    Double distanceBetweenReferenceAndFirst = getDistanceBetweenCoordinates(
                            lat,
                            lon,
                            b1.bikerPositionLatitude,
                            b1.bikerPositionLongitude
                    );

                    Double distanceBetweenReferenceAndSecond = getDistanceBetweenCoordinates(
                            lat,
                            lon,
                            b2.bikerPositionLatitude,
                            b2.bikerPositionLongitude
                    );

                    if (distanceBetweenReferenceAndFirst >
                            distanceBetweenReferenceAndSecond) {
                        return 1;
                    }

                    else if (distanceBetweenReferenceAndFirst ==
                            distanceBetweenReferenceAndSecond) {
                        return 0;
                    }

                    else {
                        return -1;
                    }
                }
            });

        return array;
    }

    private double getDistanceBetweenCoordinates(double lat1, double lon1, double lat2,
                                                 double lon2) {

        double dim1 = lat2 - lat1;
        double dim2 = lon2 - lon1;

        return Math.abs(dim1) + Math.abs(dim2);
    }

    //endregion METHODS


               /*---------------------------------------------------------------\
                                     POST NEW DELIVERY REQUEST
               \---------------------------------------------------------------*/

    //region POST NEW DELIVERY REQUESTS METHODS

    public void postNewDeliveryRequest(final RequestStatus callbacks) {

        RequestModel request = mRequest;

        request.userId = getUid();
        request.userName = CurrentUserData.getFirstName() + " " + CurrentUserData.getLastName();
        request.createTime = getCurrentISODateTime();

        Log.d(TAG, "postNewDeliveryRequest: posting new delivery request " +
                "from user " + getUid() + " with data below.");

        Log.d(TAG, "postNewDeliveryRequest: " + request.toString());

        addNewDeliveryRequestToRequestsNode (request, callbacks);
    }

    private void addNewDeliveryRequestToRequestsNode (RequestModel request,
                                                      final RequestStatus callbacks) {

        addOrUpdate(request,

            new FirebaseAccess.OnCompleteVoid() {

                @Override
                public void onSuccess() {

                    Log.d(TAG, "postNewDeliveryRequest: new delivery request successfully " +
                            "posted.");

                    // ---> Await Biker response
                    awaitBikerResponse(callbacks);

                    // ---> Set maximum response waiting time limit
                    setMaximumResponseWaitingTimeLimit(callbacks);
                }

                @Override
                public void onFailure() {

                    Log.d(TAG, "postNewDeliveryRequest: new delivery request post failed.");

                    callbacks.onError();
                }
            }, Constants.ChildName.REQUESTS, getUid()
        );
    }

    private void awaitBikerResponse (final RequestStatus callbacks) {

        setListenerToObjectOrProperty (RequestModel.class,

            new FirebaseAccess.ListenToChanges<RequestModel>() {

                @Override
                public void onChange(RequestModel data) {

                    if (data.bikerId.equals("")) return;

                    Log.d(TAG, "postNewDeliveryRequest: Biker named " + data.bikerName +
                            " (ID: " + data.bikerId + ") has accepted the request.");

                    cancelTimeouts();
                    removeLastValueEventListener();

                    initiateRequestObjectTransfer(data, callbacks);
                }

                @Override
                public void onError(RequestModel data) {

                    cancelTimeouts();
                    removeLastValueEventListener();

                    if (data == null) {

                        Log.d(TAG, "postNewDeliveryRequest: request object is missing " +
                                "or has been deleted.");

                        callbacks.onRequestCancel();
                    }

                    else {

                        Log.d(TAG, "postNewDeliveryRequest: some error occurred while " +
                                "listening to request object.");

                        callbacks.onError();
                    }
                }

            }, Constants.ChildName.REQUESTS, getUid());
    }

    private void setMaximumResponseWaitingTimeLimit (final RequestStatus callbacks) {

        Handler timeout = new Handler();

        Runnable runnable = new Runnable() {

            public void run() {

                Log.d(TAG,
                        "postNewDeliveryRequest: delivery request timed out.");

                removeLastValueEventListener();

                // ---> Cancel request by deleting object
                delete(new FirebaseAccess.OnCompleteVoid() {

                    @Override
                    public void onSuccess() {
                        Log.d(TAG,
                                "postNewDeliveryRequest: cancelled request by deleting " +
                                        "request object associated with this user.");

                        callbacks.onSearchTimedOut();
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG,
                                "postNewDeliveryRequest: "
                                        + " thought request time limit has been reached, " +
                                        "request hasn't been cancelled due to some error.");

                        callbacks.onError();
                    }

                }, Constants.ChildName.REQUESTS, getUid());
            }
        };

        timeout.postDelayed(runnable, Constants.Timeouts.REQUEST_SHORT);

        mTimeouts.put(timeout, runnable);
    }


    private void initiateRequestObjectTransfer(RequestModel data,
                                               final RequestStatus callbacks) {

        Log.d(TAG, "transferRequestObject: transferring request object from Requests to" +
                "Deliveries child ...");

        copyRequestObjectToDeliveriesNode(data, callbacks);
    }

    private void copyRequestObjectToDeliveriesNode (RequestModel data,
                                                    final RequestStatus callbacks) {
        addOrUpdate(data,

            new FirebaseAccess.OnCompleteVoid() {

                @Override
                public void onSuccess() {

                    Log.d(TAG, "transferRequestObject: successfully copied" +
                            " request object from Requests to Deliveries " +
                            "child.");

                    deleteRequestObjectFromRequestsNode(callbacks);
                }

                @Override
                public void onFailure() {

                    Log.d(TAG, "transferRequestObject: FAILED! Could " +
                            "not copy request object from Requests to " +
                            "Deliveries child.");

                    callbacks.onError();
                }
            }, Constants.ChildName.DELIVERIES, getUid());
    }

    private void deleteRequestObjectFromRequestsNode(final RequestStatus callbacks) {

        delete(new FirebaseAccess.OnCompleteVoid() {

            @Override
            public void onSuccess() {

                Log.d(TAG, "transferRequestObject: successfully deleted request object " +
                        "from Requests child.");

                awaitBikerDataUpdateOnDeliveriesNode(callbacks);
            }

            @Override
            public void onFailure() {

                Log.d(TAG, "transferRequestObject: FAILED! Could not delete request object " +
                        "from Requests child.");

                Log.i(TAG, "transferRequestObject: FAILED! Could not complete Request" +
                        " object transfer.");

                callbacks.onError();
            }
        }, Constants.ChildName.REQUESTS, getUid());
    }

    private void awaitBikerDataUpdateOnDeliveriesNode (final RequestStatus callbacks) {

        setListenerToObjectOrProperty (RequestModel.class,

            new FirebaseAccess.ListenToChanges<RequestModel>() {
                @Override
                public void onChange(RequestModel data) {

                    if (data.bikerName.equals("") ||
                            data.bikerPositionLongitude == 0d ||
                            data.bikerPositionLatitude == 0d) return;

                    Log.d(TAG, "transferRequestObject: "
                            + "Biker named " + data.bikerName + " (ID: " +
                            data.bikerId + ") has updated his information on deliveries node.");

                    removeLastValueEventListener();

                    Log.i(TAG, "transferRequestObject: Request object transfer " +
                            "completed, starting delivery ...");

                    cancelTimeouts();

                    callbacks.onRequestAccepted(data);
                }

                @Override
                public void onError(RequestModel data) {
                    Log.d(TAG, "transferRequestObject: "
                            + "request object is missing or has been deleted.");
                    callbacks.onError();
                }

            }, Constants.ChildName.DELIVERIES, getUid());
    }

    public void cancelNonAcceptedRequest() {

        Log.d(TAG, "cancelNonAcceptedRequest: request cancelled by user before any biker " +
                "accepted it.");

        cancelTimeouts();

        delete(

            new FirebaseAccess.OnCompleteVoid() {

                @Override
                public void onSuccess() {

                    Log.d(TAG, "cancelNonAcceptedRequest: successfully deleted request " +
                            "object from Requests child.");
                }

                @Override
                public void onFailure() {

                    Log.d(TAG, "cancelNonAcceptedRequest: FAILED! Could not delete " +
                            "request object from Requests child.");

                    Log.i(TAG, "cancelNonAcceptedRequest: FAILED! Could not complete " +
                            "Request object transfer.");
                }
            }, Constants.ChildName.REQUESTS, getUid());
    }

    //endregion


    /*=======================================================================================
                                              OTHER
     =======================================================================================*/

    //region OTHER METHODS

    private static class GetCoordinatesFromAddress extends AsyncTask<String, Void, LatLng> {

        AppCompatActivity context;
        OnComplete<LatLng> callbacks;

        public GetCoordinatesFromAddress (AppCompatActivity context,
                                          OnComplete<LatLng> callbacks) {
            this.context = context;
            this.callbacks = callbacks;
        }

        @Override
        protected LatLng doInBackground (String... params) {

            Geocoder coder = new Geocoder(context);
            List<Address> address;
            LatLng p1 = null;

            try {
                // May throw an IOException
                address = coder.getFromLocationName(params[0], 5);
                if (address == null) {
                    return null;
                }

                Address location = address.get(0);

                p1 = new LatLng(location.getLatitude(), location.getLongitude() );

                Log.d(TAG, "GetCoordinatesFromAddress: coordinates found! (Latitude: " +
                        p1.latitude + " / Longitude: " + p1.longitude);

            } catch (IOException ex) {

                Log.d(TAG, "GetCoordinatesFromAddress: could not locate coordinates for "
                        + "given address. Check stack trace below. Returning LatLng with " +
                        "null value.");

                ex.printStackTrace();

                return null;
            }

            return p1;
        }

        @Override
        protected void onPostExecute (LatLng result) {
            super.onPostExecute(result);

            if (result == null) {
                callbacks.onFailure(result);
                return;
            }

            callbacks.onSuccess(result);
        }
    }

    private String getAddressFromCoordinates (double lat, double lon) {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(mReferredActivity, Locale.getDefault());

        // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        // If any additional address line present than only, check with max available address
        // lines by getMaxAddressLineIndex()
        String address = addresses.get(0).getAddressLine(0);

        String add = addresses.get(0).getThoroughfare() + ", " +
                addresses.get(0).getSubThoroughfare() + " - " +
                addresses.get(0).getSubLocality() + " - " +
                addresses.get(0).getLocality() + " - State of " +
                addresses.get(0).getAdminArea() + ", " +
                addresses.get(0).getCountryName();

        return add;
    }

    private String getCurrentISODateTime () {
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName());

        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public String getUid() {
        return CurrentUserData.getId();
    }

    public void setFeeEstimate () {

        String feeEstimate = "null";

        double pickupFeePerKM = 1.5;
        double pickupFeePerMinute = 0.5;
        double deliveryFeePerKM = 2;
        double deliveryFeePerMinute = 0.7;


        double pickupDistance = decodeDistance(mRequest.estimatesPickupDistance);

        double deliveryDistance = decodeDistance(mRequest.estimatesDeliveryDistance);

        double totalFee = 5;

        totalFee += pickupDistance * pickupFeePerKM;
        totalFee += deliveryDistance * deliveryFeePerKM;

        feeEstimate = "R$" + String.format ("%.2f", totalFee).replace(".", ",");

        Log.i(TAG, "getFeeEstimate: estimated fee is - " + feeEstimate);

        mRequest.estimatesFee = feeEstimate;

    }

    public void resetRequestProperties() {
        RequestModel newModel = new RequestModel();
        mRequest = newModel;
    }

    private void cancelTimeouts() {

        Log.d(TAG, "cancelTimeouts: cancelling Timeouts ...");

        for (Map.Entry<Handler, Runnable> entry : mTimeouts.entrySet()) {
            Handler handler = entry.getKey();
            Runnable runnable = entry.getValue();

            handler.removeCallbacks(runnable);
        }
    }

    public void verifyPermissionsRequestResult (int requestCode, int[] grantResults) {

        mGoogleMaps.verifyPermissionRequestResult(requestCode, grantResults);
    }

    public double decodeDistance (String distance) {

        if (distance.contains("km")) {

            return Double.valueOf(distance
                    .replace("km", "")
                    .trim());

        }

        return Double.valueOf(distance
                .replace("m", "")
                .trim())/1000;
    }

    //endregion
}

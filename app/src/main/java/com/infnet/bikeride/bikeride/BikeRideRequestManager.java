package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class BikeRideRequestManager {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    private static final String TAG = "BikeRideRequestManager";
    private static final String REQUESTS_CHILD = "Requests";
    private static final String DELIVERIES_CHILD = "Deliveries";
    private static final String AVAILABLE_BIKERS_CHILD = "AvailableBikers";

    private static final String GOOGLE_API_KEY = "AIzaSyBNHqa3hUDjRRmSz7vW4t_3q4eE34JMTH8";

    private static final int REQUEST_TIMEOUT = 10000;


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    private AppCompatActivity referredActivity;
    private BikeRideRequestModel mRequest = new BikeRideRequestModel();
    private FirebaseAccess mFirebase = new FirebaseAccess();
    boolean mIsBikerFound = false;


    /*=======================================================================================
                                            INTERFACES
     =======================================================================================*/

    public interface GetEstimatesResponses {
        void onSuccess();
        void onInvalidAddresses();
        void noBikersAvailable();
        void onError();
    }

    private interface OnDistanceMatrixComplete {
        void OnSuccess(String s);
        void OnFailure(Exception e);
    }

    private interface OnDistanceMatrixDecode {
        void OnSuccess();
        void OnFailure(String status);
        void OnDecodeFailure(JSONException e);
    }

    public interface RequestStatus {
        void onRequestAccepted();
        void onSearchTimedOut();
        void onError();
    }

    public interface StartDelivery {
        void onStartDeliveryComplete();
        void onError();
    }

    public interface MonitorRequests {
        void onAvailabilityRegister();
        void onRequestsUpdate();
        void onError();
    }

    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    public BikeRideRequestManager (Context context) {
        referredActivity = (AppCompatActivity) context;
        mockBikerData();
    }


    /*=======================================================================================
                                         GETTERS & SETTERS
     =======================================================================================*/

    public String getPickupDistanceEstimate() {

        Log.d(TAG, "getPickupDistanceEstimate: pickup distance estimate is - " +
                mRequest.estimatesPickupDistance);
        return mRequest.estimatesPickupDistance;
    }

    public String getPickupDurationEstimate() {

        Log.d(TAG, "getPickupDurationEstimate: pickup duration estimate is - " +
                mRequest.estimatesPickupDuration);
        return mRequest.estimatesPickupDuration;
    }

    public String getDeliveryDistanceEstimate() {

        Log.d(TAG, "getDeliveryDistanceEstimate: delivery distance estimate is - " +
                mRequest.estimatesDeliveryDistance);
        return mRequest.estimatesDeliveryDistance;
    }

    public String getDeliveryDurationEstimate() {

        Log.d(TAG, "getDeliveryDurationEstimate: delivery duration estimate is - " +
                mRequest.estimatesDeliveryDuration);
        return mRequest.estimatesDeliveryDuration;
    }

    public String getFeeEstimate () {

        String feeEstimate = "null";

        double pickupFeePerKM = 1;
        double pickupFeePerMinute = 0.2;
        double deliveryFeePerKM = 1.5;
        double deliveryFeePerMinute = 0.3;

        double pickupDistance =
                Double.valueOf(mRequest.estimatesPickupDistance.replace("km",
                        "").trim());

        double deliveryDistance =
                Double.valueOf(mRequest.estimatesDeliveryDistance.replace("km",
                        "").trim());

        double totalFee = 0;

        totalFee += pickupDistance * pickupFeePerKM;
        totalFee += deliveryDistance * deliveryFeePerKM;

        feeEstimate = "R$" + String.format ("%.2f", totalFee).replace(".", ",");

        Log.i(TAG, "getFeeEstimate: estimated fee is - " + feeEstimate);

        return feeEstimate;
    }

    public String getPickupAddress() {
        Log.d(TAG, "getPickupAddress: Pickup location is - " + mRequest.pickupAddress);
        return  mRequest.pickupAddress;
    }

    public String getPickupAddressShort() {

        String location = mRequest.pickupAddress;

        if (location.length() >= 41) {
            location = location.substring(0, 40) + "...";
        }

        Log.d(TAG, "getPickupAddressShort: Pickup Location short is - " + location);
        return  location;
    }

    public String getDeliveryAddress() {

        Log.d(TAG, "getDeliveryAddress: Delivery location is - " + mRequest.deliveryAddress);
        return mRequest.deliveryAddress;
    }

    public String getDeliveryAddressShort() {

        String location = mRequest.deliveryAddress;

        if (location.length() >= 41) {
            location = location.substring(0, 40) + "...";
        }

        Log.d(TAG, "getPickupAddressShort: Delivery Location short is - " + location);
        return  location;
    }

    public void setBikerLocation(double latitude, double longitude) {

        Log.d(TAG, "setBikerLocation: setting Biker location to - Latitude: " + latitude +
                " / Longitude: " + longitude);

        mRequest.bikerPositionLatitude = latitude;
        mRequest.bikerPositionLongitude = longitude;
    }

    public void setPickupLocation(String location) {
        Log.i(TAG, "setPickupLocation: setting Pickup location to - " + location);
        mRequest.pickupAddress = location;
    }

    public void setDeliveryLocation(String location) {
        Log.i(TAG, "setDeliveryLocation: setting Delivery location to - " + location);
        mRequest.deliveryAddress = location;
    }


    /*=======================================================================================*
     |                                                                                       |
     |                                         LOGIC                                         |
     |                                                                                       |
     *=======================================================================================*/


    /*=======================================================================================
                                           GET ESTIMATES
     =======================================================================================*/

    public void getEstimates (final GetEstimatesResponses callback) {

        Log.d(TAG, "getEstimates: updating coordinates from given addresses ...");

        if (!setCoordinatesFromAddresses()) {
            Log.d(TAG, "getEstimates: given addresses are invalid. Aborting " +
                    "getting estimates.");
            callback.onInvalidAddresses();
            return;
        };

        Log.d(TAG, "getEstimates: getting available bikers ....");

        mFirebase.getAll(BikeRideAvailableBikerModel.class,
                new FirebaseAccess.OnComplete<ArrayList<BikeRideAvailableBikerModel>>() {
                    @Override
                    public void onSuccess(ArrayList<BikeRideAvailableBikerModel> data) {

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

                        data = sortByClosestBiker(data,
                                mRequest.pickupAddressLatitude,
                                mRequest.pickupAddressLongitude);

                        Log.d(TAG, "getEstimates: closest biker is '" + data.get(0).bikerName
                                + "' (ID: " + data.get(0).bikerId + ").");

                        Log.d(TAG, "getEstimates: getting closest biker's street address " +
                                "from coordinates ...");

                        String currentBikerAddress = getAddressFromCoordinates(
                                data.get(0).bikerPositionLatitude,
                                data.get(0).bikerPositionLongitude
                        );

                        if (currentBikerAddress.equals("") || currentBikerAddress == null) {
                            Log.d(TAG, "getEstimates: could not determine closest biker's " +
                                    "street address. Aborting getting estimates.");
                            callback.onError();
                            return;
                        }

                        Log.d(TAG, "getEstimates: closest biker street address found - "
                                + currentBikerAddress);

                        Log.d(TAG, "getEstimates: requesting estimate data from Google " +
                                "Distance Matrix ...");

                        getGoogleDistanceMatrixData(currentBikerAddress,
                                new OnDistanceMatrixComplete() {
                            @Override
                            public void OnSuccess(String s) {

                                Log.d(TAG, "getEstimates: successfully retrieved JSON object"
                                        + "from Google Distance Matrix API. Decoding content ...");

                                decodeGoogleDistanceMatrixData(s,
                                        new OnDistanceMatrixDecode() {
                                    @Override
                                    public void OnSuccess() {

                                        Log.d(TAG, "getEstimates: successfully decoded Google"
                                                + "DistanceMatrix JSON object and updated request" +
                                                "properties.");
                                        callback.onSuccess();
                                        return;
                                    }

                                    @Override
                                    public void OnFailure(String status) {

                                        Log.d(TAG, "getEstimates: data sent presented some " +
                                                "anomaly. Aborting getting estimates. (" + status
                                                + ")");
                                        callback.onError();
                                        return;
                                    }

                                    @Override
                                    public void OnDecodeFailure(JSONException e) {

                                        Log.d(TAG, "getEstimates:  could not decode Google " +
                                                "Distance Matrix object. Data structure might have " +
                                                "changed. Aborting getting estimates.");
                                        callback.onError();
                                        return;
                                    }
                                });
                            }

                            @Override
                            public void OnFailure(Exception e) {

                                Log.d(TAG, "getEstimates: an error has occurred while " +
                                        "accessing the Google Distance Matrix API. Check stack " +
                                        "trace below. Aborting getting estimates.");

                                callback.onError();
                                return;
                            }
                        });



                    }

                    @Override
                    public void onFailure(ArrayList<BikeRideAvailableBikerModel> data) {

                    }
                }, AVAILABLE_BIKERS_CHILD);
    }

    private boolean setCoordinatesFromAddresses() {

        Log.d(TAG, "setAddressCoordinates: updating delivery and pickup address coordinates" +
                " ...");

        LatLng pickupCoordinates = getCoordinatesFromAddress(mRequest.pickupAddress);

        if (pickupCoordinates == null) {
            Log.d(TAG, "setAddressCoordinates: cannot extract coordinates from pickup " +
                    "address.");
            return false;
        }

        LatLng deliveryCoordinates = getCoordinatesFromAddress(mRequest.deliveryAddress);

        if (deliveryCoordinates == null) {
            Log.d(TAG, "setAddressCoordinates: cannot extract coordinates from delivery " +
                    "address.");
            return false;
        }

        mRequest.pickupAddressLatitude = pickupCoordinates.latitude;
        mRequest.pickupAddressLongitude = pickupCoordinates.longitude;

        mRequest.deliveryddressLatitude = deliveryCoordinates.latitude;
        mRequest.deliveryAddressLongitude = deliveryCoordinates.longitude;

        return true;
    }

    private ArrayList<BikeRideAvailableBikerModel> sortByClosestBiker (
            ArrayList<BikeRideAvailableBikerModel> array,
            final double lat,
            final double lon) {

        final Double referenceLatitude = -22.940291;
        final Double referenceLongitude = -43.177166;

        Collections.sort(array, new Comparator<BikeRideAvailableBikerModel>() {
            @Override
            public int compare(BikeRideAvailableBikerModel bikeRideRequestModel,
                               BikeRideAvailableBikerModel t1) {

                Double distanceBetweenReferenceAndFirst =
                        getDistanceBetweenCoordinates(
                                lat,
                                lon,
                                bikeRideRequestModel.bikerPositionLatitude,
                                bikeRideRequestModel.bikerPositionLongitude
                        );

                Double distanceBetweenReferenceAndSecond =
                        getDistanceBetweenCoordinates(
                                lat,
                                lon,
                                t1.bikerPositionLatitude,
                                t1.bikerPositionLongitude
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

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

//        double height = el1 - el2;
        double height = 0;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }


    /*=======================================================================================
                                    POST NEW DELIVERY REQUEST
     =======================================================================================*/

    public void postNewDeliveryRequestAndAwaitBikerResponse(
            final RequestStatus requestStatusCallback) {

        BikeRideRequestModel request = mRequest;

        request.userName = getUid();
        request.createTime = getCurrentISODateTime();


        Log.d(TAG, "postNewDeliveryRequestAndListenToChanges: posting new delivery request " +
                "from user " + getUid() + " with data below.");

        Log.d(TAG, "postNewDeliveryRequestAndListenToChanges: " + request.toString());

        mIsBikerFound = false;

        mFirebase.addOrUpdate(
                request,
                new FirebaseAccess.OnCompleteVoid() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "postNewDeliveryRequestAndListenToChanges: new " +
                                "delivery request successfully posted.");

                        // ---> Await Biker response
                        mFirebase.setListenerToObjectOrProperty(
                                BikeRideRequestModel.class,
                                new FirebaseAccess.ListenToChanges<BikeRideRequestModel>() {
                                    @Override
                                    public void onChange(BikeRideRequestModel data) {

                                        if (data.bikerId.equals("")) return;

                                        Log.d(TAG, "postNewDeliveryRequestAndListenToChanges: "
                                                + "Biker named " + data.bikerName + " (ID: " +
                                                data.bikerId + ") has accepted the request.");

                                        mIsBikerFound = true;

                                        transferRequestObject(
                                                new BikeRideRequestManager.StartDelivery() {
                                                    @Override
                                                    public void onStartDeliveryComplete() {
                                                        Log.i(TAG, "postNewDeliveryRequestAndListenToChanges: " +
                                                                "Request object transfer completed, " +
                                                                "starting delivery ...");
                                                        requestStatusCallback.onRequestAccepted();
                                                    }

                                                    @Override
                                                    public void onError() {
                                                        Log.i(TAG, "onRequestAccepted: " +
                                                                "FAILED! Could not complete Request" +
                                                                " object transfer.");
                                                        requestStatusCallback.onError();
                                                    }
                                                });

                                    }

                                    @Override
                                    public boolean removeListenerCondition (DataSnapshot data) {

                                        if (data.getValue() == null) return true;
                                        return false;
                                    }

                                    @Override
                                    public void onError(BikeRideRequestModel data) {
                                        Log.d(TAG, "postNewDeliveryRequestAndListenToChanges: "
                                                + "request object is missing or has been deleted.");
                                    }
                                },
                                REQUESTS_CHILD, getUid());

                        // ---> Set maximum response waiting time limit
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {

                                        if (mIsBikerFound) return;

                                        Log.d(TAG,
                                                "postNewDeliveryRequestAndListenToChanges: "
                                                        + "delivery request timed out.");

                                        // ---> Cancel request by deleting object
                                        mFirebase.delete(new FirebaseAccess.OnCompleteVoid() {
                                                            @Override
                                                            public void onSuccess() {
                                                                Log.d(TAG,
                                                                        "postNewDeliveryRequestAndListenToChanges: "
                                                                                + "cancelled request by deleting request "
                                                                                + "object associated with this user.");

                                                                requestStatusCallback
                                                                        .onSearchTimedOut();
                                                            }

                                                            @Override
                                                            public void onFailure() {
                                                                Log.d(TAG,
                                                                        "postNewDeliveryRequestAndListenToChanges: "
                                                                                + " thought request time limit has been " +
                                                                                "reached, request hasn't been cancelled " +
                                                                                "due to some error.");

                                                                requestStatusCallback.onError();
                                                            }
                                                        },
                                                REQUESTS_CHILD, getUid());
                                    }
                                },
                                REQUEST_TIMEOUT);
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG, "postNewDeliveryRequestAndListenToChanges: new " +
                                "delivery request post failed.");
                    }
                },
                REQUESTS_CHILD, getUid()
        );
    }

    private void transferRequestObject(final StartDelivery startDeliveryCallback) {

        Log.d(TAG, "transferRequestObject: transferring request object from Requests to" +
                "Deliveries child ...");

        mFirebase.getObjectOrProperty(

                BikeRideRequestModel.class,
                new FirebaseAccess.OnComplete<BikeRideRequestModel>() {
                    @Override
                    public void onSuccess(BikeRideRequestModel data) {
                        Log.d(TAG, "transferRequestObject: successfully acquired request object " +
                                "from Requests child.");

                        mFirebase.addOrUpdate(

                                data,
                                new FirebaseAccess.OnCompleteVoid() {
                                    @Override
                                    public void onSuccess() {
                                        Log.d(TAG, "transferRequestObject: successfully copied" +
                                                " request object from Requests to Deliveries " +
                                                "child.");

                                        mFirebase.delete(new FirebaseAccess.OnCompleteVoid() {

                                                            @Override
                                                            public void onSuccess() {
                                                                Log.d(TAG, "transferRequestObject: " +
                                                                        "successfully deleted request object " +
                                                                        "from Requests child.");

                                                                startDeliveryCallback.onStartDeliveryComplete();
                                                            }

                                                            @Override
                                                            public void onFailure() {
                                                                Log.d(TAG, "transferRequestObject: FAILED! " +
                                                                        "Could not delete request object from " +
                                                                        "Requests child.");

                                                                startDeliveryCallback.onError();
                                                            }
                                                        },
                                                REQUESTS_CHILD, getUid());
                                    }

                                    @Override
                                    public void onFailure() {
                                        Log.d(TAG, "transferRequestObject: FAILED! Could " +
                                                "not copy request object from Requests to " +
                                                "Deliveries child.");

                                        startDeliveryCallback.onError();
                                    }
                                },
                                DELIVERIES_CHILD, getUid());
                    }

                    @Override
                    public void onFailure(BikeRideRequestModel data) {

                        Log.d(TAG, "transferRequestObject: FAILED! Could not retrieve " +
                                "request object from Requests child.");

                        startDeliveryCallback.onError();
                    }
                },
                REQUESTS_CHILD, getUid()
        );

    }


    /*=======================================================================================
                                              OTHER
     =======================================================================================*/

    private LatLng getCoordinatesFromAddress (String strAddress) {

        Log.d(TAG, "getCoordinatesFromAddress: getting LatLng coordinates for address - " +
                strAddress);

        Geocoder coder = new Geocoder(referredActivity);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

            Log.d(TAG, "getCoordinatesFromAddress: coordinates found! (Latitude: " +
                    p1.latitude + " / Longitude: " + p1.longitude);

        } catch (IOException ex) {

            Log.d(TAG, "getCoordinatesFromAddress: could not locate coordinates for " +
                    "given address. Check stack trace below. Returning LatLng with null value.");

            ex.printStackTrace();
        }

        return p1;
    }

    private String getAddressFromCoordinates (double lat, double lon) {

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(referredActivity, Locale.getDefault());

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

        return addresses.get(0).getLocality() + " " + addresses.get(0).getPostalCode();

    }

    public void getGoogleDistanceMatrixData(String bikerLocation,
                                            final OnDistanceMatrixComplete callbacks) {

        // https://developers.google.com/maps/documentation/distance-matrix/intro

        String distanceMatrixBaseUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?";
        String unitType = "metric";  // "metric" or "imperial"
        String travelMode = "bicycling"; // "walking", "bicycling", "driving"

        String executeString = distanceMatrixBaseUrl
                + "units=" + unitType + "&"
                + "origins=" + bikerLocation + "|" + mRequest.pickupAddress + "&"
                + "destinations=" + mRequest.pickupAddress + "|" + mRequest.deliveryAddress + "&"
                + "mode=" + travelMode + "&"
                + "key=" + GOOGLE_API_KEY;


        new HttpRequest(new HttpRequest.Callbacks() {
            @Override
            public String OnComplete(String data) {
                callbacks.OnSuccess(data);
                return null;
            }

            @Override
            public String OnFailure(Exception e) {
                callbacks.OnFailure(e);
                return null;
            }
        }).execute(executeString);
    }

    public void mockBikerData () {

        ArrayList<BikeRideAvailableBikerModel> array = new ArrayList<>();

        array.add(new BikeRideAvailableBikerModel(
                "Biker Ipanema",
                "bikeripanemabikeripanemabikeripanema",
                -22.983546,
                -43.197581,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BikeRideAvailableBikerModel(
                "Biker Copacabana",
                "bikercopacabanabikercopacabanabikercopacabana",
                -22.973599,
                -43.189674,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BikeRideAvailableBikerModel(
                "Biker Flamengo",
                "bikerflamengobikerflamengobikerflamengo",
                -22.932376,
                -43.177529,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BikeRideAvailableBikerModel(
                "Biker Catete",
                "bikercatetebikercatetebikercatete",
                -22.925806,
                -43.176970,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        array.add(new BikeRideAvailableBikerModel(
                "Biker Centro",
                "bikercentrobikercentrobikercentro",
                -22.909491,
                -43.183332,
                getCurrentISODateTime(),
                getCurrentISODateTime()
        )) ;

        for (BikeRideAvailableBikerModel biker : array) {
            mFirebase.addOrUpdate(biker,
                    new FirebaseAccess.OnCompleteVoid() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure() {

                        }
                    }, AVAILABLE_BIKERS_CHILD, biker.bikerId);
        }
    }

    private String getCurrentISODateTime () {
        TimeZone tz = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName());

        // Quoted "Z" to indicate UTC, no timezone offset
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public void decodeGoogleDistanceMatrixData (String s, OnDistanceMatrixDecode callbacks) {

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

            Log.d(TAG, "decodeGoogleDistanceMatrixData: decoding serialized JSON object " +
                    "FAILED!");

            callbacks.OnDecodeFailure(e);
        }

        Log.d(TAG, "decodeGoogleDistanceMatrixData: successfully decoded JSON object.");

        if (statusOverall.equals("OK") &&
                statusPickup.equals("OK") &&
                statusDelivery.equals("OK") &&
                !pickupDistance.equals("") &&
                !pickupDuration.equals("") &&
                !deliveryDistance.equals("") &&
                !deliveryDuration.equals("")) {

            Log.d(TAG, "decodeGoogleDistanceMatrixData: information is complete and " +
                    "can be trusted. Updating request estimates. (pickupDistance: " +
                    pickupDistance + " / pickupDuration: " + pickupDuration + " / " +
                    "deliveryDistance: " + deliveryDistance + " / deliveryDuration: " +
                    deliveryDuration + ")");

            mRequest.estimatesPickupDistance = pickupDistance;
            mRequest.estimatesPickupDuration = pickupDuration;
            mRequest.estimatesDeliveryDistance = deliveryDistance;
            mRequest.estimatesDeliveryDuration = deliveryDuration;

            callbacks.OnSuccess();
        }

        else {
            callbacks.OnFailure("Overall Status: " + statusOverall + " / Pickup Status: " +
            statusPickup + " / Delivery Status: " + statusDelivery);
        }

    }

    private String getUid() {
        return "EvenAnotherUserId";
    }
}

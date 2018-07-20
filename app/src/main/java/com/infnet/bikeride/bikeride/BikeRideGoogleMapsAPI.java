package com.infnet.bikeride.bikeride;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BikeRideGoogleMapsAPI {

    /*

    CREDITS
    =======

    This class has been developed by Roger Freret, based on the following tutorial:
    https://www.youtube.com/playlist?list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt

    INSTRUCTIONS
    ============

    1. Install Google Play services SDK on your Android Studio.

        - On TOOLS Menu on Android Studio, click SDK MANAGER
        - Click on SDK TOOLS TAB
        - GOOGLE PLAY SERVICES must be installed.
        - SUPPORT REPOSITORY > GOOGLE REPOSITORY must be installed.

    2. Add Google Play Services dependency to project.

        - On BUILD.GRADLE(MODULE.APP), within DEPENDENCIES scope, add the following lines:

            // Google play services
            implementation 'com.google.android.gms:play-services:11.4.0'

    3. Include MAVEN REPOSITORY on project if not already there.

        - On BUILD.GRADLE(PROJECT), within ALLPROJECTS > REPOSITORIES scope, add:

            maven {
                url "http://maven.google.com"
            }

    4. Get a Google Maps API Key.

        - Go to GOOGLE API CONSOLE (https://console.cloud.google.com/apis/dashboard)
        - Create a new Google project for your app, if not done before.
        - Navigate to your project, CREDENTIALS > CREATE CREDENTIALS > API KEY, copy your
          API KEY so a safe place within Android Studio Project (Values strings?);

    5. Enable Google Maps API on Google project.

        - Navigate to LIBRARY > MAPS SDK FOR ANDROID > ACTIVATE.

    6. Add the following to MANIFEST.XML file.

        - Inside MANIFEST element, add:

            <uses-permission android:name="android.permission.INTERNET" />
            <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
            <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
            <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

        - Inside APPLICATION element, add:

            <meta-data
                android:name="com.google.android.gms.version"
                android:value="@integer/google_play_services_version" />

            <meta-data
                android:name="com.google.android.geo.API_KEY"
                android:value="YOUR_API_KEY" />

    7. Add Google Maps Fragment to your activity.

        <fragment xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:id="@+id/map"
            tools:context=".MapsActivity"
            android:name="com.google.android.gms.maps.SupportMapFragment" />

     */

    private static final String TAG = "BikeRideGoogleMapsClass";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String API_KEY = "AIzaSyD3w91P6nWokP4GvyKkkGpnlJ--EmcgOHA";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final float DEFAULT_ZOOM = 17f;

    private boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private int mMapFragmentId;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private AppCompatActivity refferedActivity;

    public BikeRideGoogleMapsAPI(AppCompatActivity activity, int mapFragmentId) {

        refferedActivity = activity;
        mMapFragmentId = mapFragmentId;

        if (isGoogleMapsServicesOK()) {
            getLocationPermissions();
        }

    }

    private void getDeviceLocation() {

        Log.d(TAG, "getDeviceLocation: getting current device's location.");
        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(refferedActivity);

        try {
            if (mLocationPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "getDeviceLocation: onComplete - found location.");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "getDeviceLocation: onComplete - current location is null.");
                            Toast.makeText(refferedActivity, "Unable to get current location..",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security exception - " + e.getMessage());

        }

    }

    private void moveCamera(LatLng latlng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera (LAT: " + latlng.latitude +
                " / LNG: " + latlng.longitude + ")");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    private void initMap() {
        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        refferedActivity.getSupportFragmentManager()
                                .findFragmentById(mMapFragmentId);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: map is ready. ");
//                Toast.makeText(refferedActivity, "Map is ready.", Toast.LENGTH_SHORT).show();

                mMap = googleMap;

                if (mLocationPermissionsGranted) {
                    getDeviceLocation();
                    if (ActivityCompat.checkSelfPermission(refferedActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager
                                .PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(refferedActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                                .PERMISSION_GRANTED) {

                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }

            }
        });
    }

    private boolean isGoogleMapsServicesOK() {
        Log.d(TAG, "isGoogleMapsServicesOK: checking Google Services version. ");

        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(refferedActivity);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isGoogleMapsServicesOK: Google Services is working.");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isGoogleMapsServicesOK: an error occured but we can fix it.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(refferedActivity,
                    available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(refferedActivity, "You can't make map requests.",
                    Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void getLocationPermissions() {

        Log.d(TAG, "getLocationPermissions: getting location permissions. ");

        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        boolean fineLocationPermissionsGranted =
                ContextCompat.checkSelfPermission(refferedActivity, FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;

        boolean coarseLocationPermissionsGranted =
                ContextCompat.checkSelfPermission(refferedActivity, COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;

        if (fineLocationPermissionsGranted && coarseLocationPermissionsGranted) {
                Log.d(TAG, "getLocationPermissions: location permissions have already " +
                        "been granted.");
                mLocationPermissionsGranted = true;
                initMap();

        } else {
            ActivityCompat.requestPermissions(refferedActivity, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
            Log.d(TAG, "getLocationPermissions: location permissions haven't been found." +
                    " Asking for new ones");

        }
    }

    public void verifyPermissionRequestResult(int requestCode, int[] grantResults) {

        Log.d(TAG, "verifyPermissionRequestResult: called. ");

        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int i = 0; i<grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "verifyPermissionRequestResult: permission denied. ");
                            return;
                        }
                    }
                    Log.d(TAG, "verifyPermissionRequestResult: permission granted. ");
                    mLocationPermissionsGranted = true;
                    // initialize your map
                    initMap();
                }
        }
    }

    public void getEstimatesFromWebAsync(String bikerLocation, String pickupLocation,
                                         String deliveryLocation, String onCompleteMethodName) {

        // https://developers.google.com/maps/documentation/distance-matrix/intro

        String distanceMatrixBaseUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?";
        String unitType = "metric";  // "metric" or "imperial"
        String travelMode = "bicycling"; // "walking", "bicycling", "driving"

        String executeString = distanceMatrixBaseUrl
                + "units=" + unitType + "&"
                + "origins=" + bikerLocation + "|" + pickupLocation + "&"
                + "destinations=" + pickupLocation + "|" + deliveryLocation + "&"
                + "mode=" + travelMode + "&"
                + "key=" + API_KEY;

        AsyncReflectedHttpRequest googleMatrixData =
                new AsyncReflectedHttpRequest(refferedActivity, onCompleteMethodName);
        googleMatrixData.execute(executeString);
    }
}

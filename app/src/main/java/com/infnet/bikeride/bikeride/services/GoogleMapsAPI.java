package com.infnet.bikeride.bikeride.services;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.infnet.bikeride.bikeride.R;

import java.util.ArrayList;
import java.util.Collections;

public class GoogleMapsAPI {

    /*=======================================================================================
                                           INSTRUCTIONS
     =======================================================================================*/

    //region INSTRUCTIONS

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

    //endregion


    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "BikeRideGoogleMapsClass";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final int BIKER_ICON = R.drawable.bikerbluetiny;

    private static final float DEFAULT_ZOOM = 17f;

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private int mMapFragmentId;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Maps mCallbacks;

    private Marker mBikerMarker;

    private AppCompatActivity mReferredActivity;

    private ArrayList<Polyline> mPolylineStorage = new ArrayList<>();

    //endregion


    /*=======================================================================================
                                            INTERFACES
     =======================================================================================*/

    //region INTERFACES

    public interface Maps {
        void OnMapReady();
    }

    //endregion


    /*=======================================================================================
                                           CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public GoogleMapsAPI(AppCompatActivity activity, int mapFragmentId, Maps callbacks) {

        mReferredActivity = activity;
        mMapFragmentId = mapFragmentId;
        mCallbacks = callbacks;

        if (isGoogleMapsServicesOK()) {
            getLocationPermissions();
        }
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                      LOGIC                                       |
       |                                                                                  |
       \=================================================================================*/

    public void centerMapOnDeviceLocation() {

        Log.d(TAG, "centerMapOnDeviceLocation: getting current device's location.");

        mFusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(mReferredActivity);

        try {

            Task location = mFusedLocationProviderClient.getLastLocation();

            location.addOnCompleteListener(

                new OnCompleteListener() {

                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful()) {

                            Log.d(TAG, "centerMapOnDeviceLocation: onComplete - found location.");
                            Log.d(TAG, "centerMapOnDeviceLocation: TASK - " + task.toString());

                            Location currentLocation = (Location) task.getResult();

                            if (currentLocation == null) {
                                Log.d(TAG, "centerMapOnDeviceLocation: unable to get device " +
                                        "location. Please check if location services " +
                                        "are enabled on device's settings.");
                                return;
                            }

                            Log.d(TAG, "centerMapOnDeviceLocation: LOCATION - "
                                    + currentLocation.toString());


                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM);

                        } else {

                            Log.d(TAG, "centerMapOnDeviceLocation: centerMapOnDeviceLocation " +
                                    "- current location is null.");

                            Toast.makeText(mReferredActivity, "Unable to get current " +
                                    "location.", Toast.LENGTH_SHORT).show();

                        }
                    }
            });

        } catch (SecurityException e) {
            Log.e(TAG, "centerMapOnDeviceLocation: Security exception - " + e.getMessage());

        }

    }

    public void moveCamera(LatLng latlng, float zoom) {

        Log.d(TAG, "moveCamera: moving camera (LAT: " + latlng.latitude +
                " / LNG: " + latlng.longitude + ")");

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom));
    }

    public void addMarkerOnMap (LatLng latLng, String title) {
        mMap.addMarker(new MarkerOptions().position(latLng)
                .title(title));
    }

    public void addBikerMarkerOnMap (LatLng latLng) {

        mBikerMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(BIKER_ICON)));

    }

    public void moveBikerMarkerOnMap (LatLng latLng) {

        mBikerMarker.setPosition(latLng);
    }

    public void centerMapWithinArea (Double... data) {

        LatLngBounds boundaries = getBoundariesFromCoordinates(data);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundaries, 0));
    }

    public void drawPolylines(ArrayList<LatLng> data) {

        Log.d(TAG, "drawPolylines: drawing ...");

        if (mPolylineStorage.size() > 0) {

            mPolylineStorage.get(0).remove();
            mPolylineStorage.clear();
        }

        PolylineOptions waypoints = new PolylineOptions();

        for (int i = 0; i<data.size(); i++) {
            waypoints.add(data.get(i));
        }

        Polyline polyline = mMap.addPolyline(waypoints);

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(10);
        polyline.setColor(COLOR_PURPLE_ARGB);
        polyline.setJointType(JointType.ROUND);

        mPolylineStorage.add(polyline);
    }


    /*=======================================================================================
                                           SUPPORT METHODS
     =======================================================================================*/

    private LatLngBounds getBoundariesFromCoordinates (Double... data) {

        if (data.length == 0 || data.length % 2 != 0) return null;

        ArrayList<Double> lats = new ArrayList<>();
        ArrayList<Double> longs = new ArrayList<>();

        for (int i = 0; i<data.length; i +=2) {

            lats.add(data[i]);
            longs.add(data[i+1]);
        }

        Collections.sort(lats);
        Collections.sort(longs);

        Double north = lats.get(lats.size()-1);
        Double south = lats.get(0);

        Double west = longs.get(0);
        Double east = longs.get(lats.size()-1);

        Double verticalDistance = north - south;
        Double horizontalDistance = east - west;

        final Double verticalMargin = 0.2;
        final Double horizontalMargin = 0.2;

        LatLng southwest = new LatLng(
                south-verticalDistance*verticalMargin,
                west-horizontalDistance*horizontalMargin);

        LatLng northeast = new LatLng(
                north+verticalDistance*verticalMargin,
                east+horizontalDistance*horizontalMargin);

        return new LatLngBounds(southwest, northeast);
    }

    public void setPadding (int leftInDP, int topInDP, int rightInDP, int bottomInDP) {

        Resources r = mReferredActivity.getResources();

        leftInDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                leftInDP, r.getDisplayMetrics());

        topInDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                topInDP, r.getDisplayMetrics());

        rightInDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                rightInDP, r.getDisplayMetrics());

        bottomInDP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                bottomInDP, r.getDisplayMetrics());

        mMap.setPadding(leftInDP, topInDP, rightInDP, bottomInDP);
    }


       /*=================================================================================\
       |                                                                                  |
       |                  API AVAILABILITY, PERMISSIONS, INITIALIZATION                   |
       |                                                                                  |
       \=================================================================================*/

    //region API AVAILABILITY, PERMISSIONS, INITIALIZATION

    private boolean isGoogleMapsServicesOK() {

        Log.d(TAG, "isGoogleMapsServicesOK: checking Google Services version. ");

        int available = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(mReferredActivity);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isGoogleMapsServicesOK: Google Services is working.");
            return true;
        }

        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {

            Log.d(TAG, "isGoogleMapsServicesOK: an error occured but we can fix it.");

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(mReferredActivity,
                    available, ERROR_DIALOG_REQUEST);

            dialog.show();
        }

        else {

            Toast.makeText(mReferredActivity, "You can't make map requests.",
                    Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void getLocationPermissions() {

        Log.d(TAG, "getLocationPermissions: getting location permissions. ");

        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};

        boolean fineLocationPermissionsGranted =
                ContextCompat.checkSelfPermission(mReferredActivity, FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;

        boolean coarseLocationPermissionsGranted =
                ContextCompat.checkSelfPermission(mReferredActivity, COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;

        if (fineLocationPermissionsGranted && coarseLocationPermissionsGranted) {

            Log.d(TAG, "getLocationPermissions: location permissions have already " +
                    "been granted.");

            mLocationPermissionsGranted = true;

            initMap();

        } else {

            ActivityCompat.requestPermissions(mReferredActivity, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);

            Log.d(TAG, "getLocationPermissions: location permissions haven't been found." +
                    " Asking for new ones.");

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

                    initMap();
                }
        }
    }

    private void initMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) mReferredActivity
                .getSupportFragmentManager().findFragmentById(mMapFragmentId);

        mapFragment.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {

                Log.d(TAG, "onMapReady: map is ready. ");

                mMap = googleMap;

                try {

                    mMap.setMyLocationEnabled(true);

                } catch (SecurityException e) {

                    Log.d(TAG, "onMapReady: location permissions were not granted.");

                    e.printStackTrace();
                }

                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                // loadMapStyle();

                mCallbacks.OnMapReady();
            }
        });
    }

    private void loadMapStyle () {

        try {

            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            mReferredActivity, R.raw.maps_styles1_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }

        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

    }

    //endregion

}

package com.infnet.bikeride.bikeride.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class BRLocations {

    /*=======================================================================================
                                             INSTRUCTIONS
     =======================================================================================*/

    //region INSTRUCTIONS

    /*

    In order for this class to work on your project you need to:

    1 - Add the following line to MANIFEST.XML:

        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    2 - Declare the BRLocations object on Activity scope
        (IE: private BRLocations mUserLocation;)

    3 - Instantiate the BRLocations object on mReferredActivity's onCreate method passing mReferredActivity
        and onLocationChange method name.
        (IE: mUserLocation = new BRLocations(this, "onLocationChanged");)

    4 - Override this method on your referred mReferredActivity like this

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            mUserLocation.verifyPermissionRequestResult(grantResults);
        }

     */

    //endregion


    /*=======================================================================================
                                            CONSTANTS
     =======================================================================================*/

    private static String TAG = "BRLocations";

    /*=======================================================================================
                                            VARIABLES
     =======================================================================================*/

    //region VARIABLES

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private AppCompatActivity mReferencedActivity;

    private Location mUserLocation;
    private Double   mUserLatitude;
    private Double   mUserLongitude;
    private Double   mUserAltitude;
    private float    mUserAccuracy;

    //endregion


    /*=======================================================================================
                                            INTERFACES
     =======================================================================================*/

    //region INTERFACES

    public interface OnLocationChanged {
        void OnChange();
    }

    //endregion


    /*=======================================================================================
                                            CONSTRUCTORS
     =======================================================================================*/

    //region CONSTRUCTORS

    public BRLocations(AppCompatActivity activity, final OnLocationChanged callbacks) {

        mReferencedActivity = activity;

        mLocationManager = (LocationManager) mReferencedActivity.getSystemService(
                Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateUserLocation(location);
                callbacks.OnChange();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            startListening();
        }

        else {

            // --- Checks if fine location access has already been granted
            if (ContextCompat.checkSelfPermission(mReferencedActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                // ---> If not, request permission...
                ActivityCompat.requestPermissions(mReferencedActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            // --- Fine location access has already been granted, so...
            else {

                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, mLocationListener);

                // ---> Gets last known location and initializes class properties
                Location location =
                        mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) updateUserLocation(location);
            }
        }
    }

    //endregion

    /*=======================================================================================
                                         GETTERS & SETTERS
     =======================================================================================*/

    //region GETTERS & SETTERS

    public Location getLocation () {
        return mUserLocation;
    }

    public Double getLatitude () {
        return mUserLatitude;
    }

    public Double getLongitude () {
        return mUserLongitude;
    }

    public Double getAltitude () {
        return mUserAltitude;
    }

    public float getAccuracy () {
        return mUserAccuracy;
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                       LOGIC                                      |
       |                                                                                  |
       \=================================================================================*/


    public void verifyPermissionRequestResult (int requestCode, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(mReferencedActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                mLocationManager =
                        (LocationManager) mReferencedActivity.getSystemService(
                                Context.LOCATION_SERVICE);

                // ---> Gets last known location and initializes class properties
                Location location = mLocationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER);
                if (location != null) updateUserLocation(location);

            }
        }
    }

    private void startListening() {

        if (ContextCompat.checkSelfPermission(mReferencedActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager =
                    (LocationManager) mReferencedActivity.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void updateUserLocation (Location location) {
        mUserLocation = location;
        mUserLatitude = location.getLatitude();
        mUserLongitude = location.getLongitude();
        mUserAltitude = location.getAltitude();
        mUserAccuracy = location.getAccuracy();
    }

    public void removeLocationListener() {

        Log.d(TAG, "removeLocationListener: removing location listener.");
        mLocationManager.removeUpdates(mLocationListener);
        mLocationManager = null;
    }



    public String getAddress() {

        Geocoder geocoder = new Geocoder(mReferencedActivity, Locale.getDefault());

        String address = "Unable to find address";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(mUserLocation.getLatitude(),
                    mUserLocation.getLongitude(), 1);

            if (listAddresses != null && listAddresses.size() > 0 ) {

                Log.i("PlaceInfoObject", listAddresses.get(0).toString());

                address = "";

                if (listAddresses.get(0).getThoroughfare() != null) {
                    address += listAddresses.get(0).getThoroughfare() + ", no. ";
                }

                if (listAddresses.get(0).getSubThoroughfare() != null) {
                    address += listAddresses.get(0).getSubThoroughfare() + " - ";
                }

                if (listAddresses.get(0).getLocality() != null) {
                    address += listAddresses.get(0).getLocality() + " - CEP: ";
                }

                if (listAddresses.get(0).getPostalCode() != null) {
                    address += listAddresses.get(0).getPostalCode() + " - ";
                }

                if (listAddresses.get(0).getCountryName() != null) {
                    address += listAddresses.get(0).getCountryName();
                }

            }


        } catch (IOException e) {

            e.printStackTrace();

        }

        return address;
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
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

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }
}

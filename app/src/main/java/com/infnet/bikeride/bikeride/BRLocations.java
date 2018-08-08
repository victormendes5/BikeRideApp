package com.infnet.bikeride.bikeride;

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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public class BRLocations {

    /*

    INSTRUCTIONS
    ============

    In order for this class to work on your project you need to:

    1 - Add the following line to MANIFEST.XML:

        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    2 - Declare the BRLocations object on Activity scope
        (IE: private BRLocations mUserLocation;)

    3 - Instantiate the BRLocations object on activity's onCreate method passing activity
        and onLocationChange method name.
        (IE: mUserLocation = new BRLocations(this, "onLocationChanged");)

    4 - Override this method on your referred activity like this

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {

            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            mUserLocation.verifyPermissionRequestResult(grantResults);
        }

     */

    private LocationManager locationManager;
    private LocationListener locationListener;
    private AppCompatActivity referencedActivity;

    private Location userLocation;
    private Double userLatitude;
    private Double userLongitude;
    private Double userAltitude;
    private float userAccuracy;

    public BRLocations(AppCompatActivity activity, final String onLocationChangedMethodName) {

        referencedActivity = activity;

        locationManager = (LocationManager) referencedActivity.getSystemService(
                Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateUserLocation(location);

                if (onLocationChangedMethodName.equals("")) return;

                try {
                    Method sentMethod = referencedActivity.getClass().getDeclaredMethod(
                            onLocationChangedMethodName);
                    sentMethod.setAccessible(true);
                    sentMethod.invoke(referencedActivity);
                }

                catch (Exception e) {
                    e.printStackTrace();
                }

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
            if (ContextCompat.checkSelfPermission(referencedActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // ---> If not, request permission...
                ActivityCompat.requestPermissions(referencedActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            // --- Fine location access has already been granted, so...
            else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locationListener);

                // ---> Gets last known location and initializes class properties
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) updateUserLocation(location);
            }
        }
    }

    public void verifyPermissionRequestResult (int requestCode, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(referencedActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager =
                        (LocationManager) referencedActivity.getSystemService(
                                Context.LOCATION_SERVICE);

                // ---> Gets last known location and initializes class properties
                Location location = locationManager.getLastKnownLocation(
                        LocationManager.GPS_PROVIDER);
                if (location != null) updateUserLocation(location);

            }
        }
    }

    private void startListening() {

        if (ContextCompat.checkSelfPermission(referencedActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager =
                    (LocationManager) referencedActivity.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void updateUserLocation (Location location) {
        userLocation = location;
        userLatitude = location.getLatitude();
        userLongitude = location.getLongitude();
        userAltitude = location.getAltitude();
        userAccuracy = location.getAccuracy();
    }

    public Location getLocation () {
        return userLocation;
    }

    public Double getLatitude () {
        return userLatitude;
    }

    public Double getLongitude () {
        return userLongitude;
    }

    public Double getAltitude () {
        return userAltitude;
    }

    public float getAccuracy () {
        return userAccuracy;
    }

    public String getAddress() {

        Geocoder geocoder = new Geocoder(referencedActivity, Locale.getDefault());

        String address = "Unable to find address";

        try {

            List<Address> listAddresses = geocoder.getFromLocation(userLocation.getLatitude(),
                    userLocation.getLongitude(), 1);

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

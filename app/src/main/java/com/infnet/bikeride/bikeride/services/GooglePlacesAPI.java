package com.infnet.bikeride.bikeride.services;

import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.infnet.bikeride.bikeride.services.adapters.PlaceAutoCompleteAdapter;

public class GooglePlacesAPI {

        /*

    INSTRUCTIONS
    ============

    1. Make sure Google Play services SDK is installed on your Android Studio.

        - On TOOLS Menu on Android Studio, click SDK MANAGER
        - Click on SDK TOOLS TAB
        - GOOGLE PLAY SERVICES must be installed.
        - SUPPORT REPOSITORY > GOOGLE REPOSITORY must be installed.

    2. Make sure Google Play Services dependency has been added to project.

        - On BUILD.GRADLE(MODULE.APP), within DEPENDENCIES scope, add the following lines:

            // Google play services
            implementation 'com.google.android.gms:play-services:11.4.0'

    3. Include MAVEN REPOSITORY on project if not already there.

        - On BUILD.GRADLE(PROJECT), within ALLPROJECTS > REPOSITORIES scope, add:

            maven {
                url "http://maven.google.com"
            }

    4. Get a Google API Key if the project doesn't have one yet.

        - Go to GOOGLE API CONSOLE (https://console.cloud.google.com/apis/dashboard)
        - Create a new Google project for your app, if not done before.
        - Navigate to your project, CREDENTIALS > CREATE CREDENTIALS > API KEY, copy your
          API KEY so a safe place within Android Studio Project (Values strings?);

    5. Add the following to MANIFEST.XML file.

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

    6. Sign your API Key.

        - Go to Google Cloud Platform
        - Home (Navigation Drawer)
        - API and Services > Credentials > Click on your Key
        - Set KEY RESTRICTION to ANDROID APPS
        - Click on ADD PACKAGE NAME AND FINGERPRINT
        - Input package name
        - Get SHA-1 certificate fingerprint of your Android Studio project by:
            - In Android Studio, open GRADLE tab on the right side of screen.
            - APP > TASKS > ANDROID > SIGNINREPORT
            - Get SHA1 from prompt.
        - Save API Key modifications.

     7. Enable Google Places API on Google project.

        - APIs & SERVICES > LIBRARY > PLACES SDK FOR ANDROID > ENABLE

     8. This CLASS is dependant on the CLASS PlaceAutoCompleteAdapter, so make
        sure you have is as well. Download at:
        https://github.com/googlesamples/android-play-places/tree/master/PlaceCompleteAdapter/Application/src/main/java/com/example/google/playservices/placecomplete

     9. Views passed to setAutoComplete must be of type AutoCompleteTextView

     */

    private AppCompatActivity mRefferedActivity;

    private static final LatLngBounds LAT_LONG_BOUNDS_BRAZIL = new LatLngBounds(
            new LatLng(-33.391381, -72.187674),
            new LatLng(4.438078, -32.812675));


    public GooglePlacesAPI(AppCompatActivity activity) {

        mRefferedActivity = activity;
    }

    public void setAutoComplete(int... viewIds) {

        for (int viewId : viewIds) {

            GeoDataClient geoDataClient = Places.getGeoDataClient(mRefferedActivity,
                    null);

            PlaceAutoCompleteAdapter placeAutoCompleteAdapter = new PlaceAutoCompleteAdapter(
                    mRefferedActivity, geoDataClient, LAT_LONG_BOUNDS_BRAZIL,
                    null);

            final AutoCompleteTextView autoCompleteTextView =
                    (AutoCompleteTextView) mRefferedActivity.findViewById(viewId);

            autoCompleteTextView.setAdapter(placeAutoCompleteAdapter);

            autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            || event.getAction() == KeyEvent.KEYCODE_ENTER) {

                        InputMethodManager imm = (InputMethodManager)
                                mRefferedActivity.getSystemService(
                                        mRefferedActivity.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(
                                autoCompleteTextView.getApplicationWindowToken(), 0);

                    }

                    // execute method for searching

                    return false;
                }
            });

            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    InputMethodManager imm = (InputMethodManager)
                            mRefferedActivity.getSystemService(
                                    mRefferedActivity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(
                            autoCompleteTextView.getApplicationWindowToken(), 0);
                }
            });
        }
    }
}

package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DeliveryTrackingActivity extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    BikeRideContentViewBuilder mContentViewBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tracking_delivery);

        mContentViewBuilder = new BikeRideContentViewBuilder(this,
                R.layout.activity_tracking_delivery);
    }

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }
}

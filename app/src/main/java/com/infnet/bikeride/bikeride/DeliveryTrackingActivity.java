package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DeliveryTrackingActivity extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    BRContentViewBuilder mContentViewBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_tracking_delivery);

        mContentViewBuilder = new BRContentViewBuilder(this,
                R.layout.activity_tracking_delivery);
    }

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }
}

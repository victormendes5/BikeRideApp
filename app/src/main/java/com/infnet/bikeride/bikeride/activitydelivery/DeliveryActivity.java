package com.infnet.bikeride.bikeride.activitydelivery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.infnet.bikeride.bikeride.services.ContentViewBuilder;
import com.infnet.bikeride.bikeride.R;

public class DeliveryActivity extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_delivery);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_delivery);
    }

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }
}

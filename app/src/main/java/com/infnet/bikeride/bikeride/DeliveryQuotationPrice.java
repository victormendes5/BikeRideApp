package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DeliveryQuotationPrice extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    BikeRideContentViewBuilder mContentViewBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_delivery_quotation_price);

        mContentViewBuilder = new BikeRideContentViewBuilder(this,
                R.layout.activity_delivery_quotation_price);
    }

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }
}

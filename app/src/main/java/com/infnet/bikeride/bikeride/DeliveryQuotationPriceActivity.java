package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

public class DeliveryQuotationPriceActivity extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_delivery_quotation_price);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_delivery_quotation_price);
    }

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }
}

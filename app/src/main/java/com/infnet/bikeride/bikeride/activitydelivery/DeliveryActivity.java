package com.infnet.bikeride.bikeride.activitydelivery;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.services.Abstractions;
import com.infnet.bikeride.bikeride.services.Animations;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

public class DeliveryActivity extends AppCompatActivity {


    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "DeliveryActivity";

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    // ---> Animations
    private Animations mAnimate = new Animations(200);

    // ---> Customized setContentView with navigation drawer and toolbar
    private ContentViewBuilder mContentViewBuilder;

    private DeliveryManager mDeliveryManager;

    // ---> General abstractions (on this Activity, for navigation)
    private Abstractions mAbst;

    //endregion


    /*=======================================================================================
                                        ACTIVITY MAIN METHODS
     =======================================================================================*/

    //region ACTIVITY MAIN METHODS (ONCREATE, ONDESTROY)

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_delivery);

        mDeliveryManager = new DeliveryManager(this);

        mAbst = new Abstractions(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mDeliveryManager.verifyPermissionsRequestResult(requestCode, grantResults);
    }

    @Override
    protected void onStop() {

        // mRequestManager.removeLocationListener();
        // mRequestManager.removeAllListeners();

        super.onStop();
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                 DELIVERY LOGIC                                   |
       |                                                                                  |
       \=================================================================================*/

    //region BIKER LOGIC



    //endregion

                /*---------------------------------------------------------------\
                                            MODALS LOGIC
                \---------------------------------------------------------------*/

    //region MODALS LOGIC

    private void enterModalState () {
        // mAnimate.fadeInIfInvisible(mModalOverlay);
        // mAnimate.translateFromBottomIfInvisible(mModalRequestsList);
    }

    private void exitModalState () {
        // mAnimate.translateToBottomIfVisible(mModalRequestsList, mModalAwaitingConfirmation);
        // mAnimate.fadeOutIfVisible(mModalOverlay);
    }

    //endregion
}

package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class BikerActivity extends AppCompatActivity {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "BikerActivity";

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    // ---> Animations
    private BRAnimations mAnimate = new BRAnimations(200);

    // ---> Google APIs
    private BRGoogleMapsAPI mGoogleMaps;

    // ---> BikeRide Request Manager
    private BRRequestManagerBiker mRequestManager;

    // ---> Customized setContentView with navigation drawer and toolbar
    private BRContentViewBuilder mContentViewBuilder;

    // ---> View variables
    private View mModalOverlay;
    private View mModalRequestsList;

    // ---> Control variables
    private boolean mHasChosenRequest = false;

    //endregion


    /*=======================================================================================
                                        ACTIVITY MAIN METHODS
     =======================================================================================*/

    //region ACTIVITY MAIN METHODS (ONCREATE, ONDESTTROY)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContentViewBuilder = new BRContentViewBuilder(this,
                R.layout.activity_biker);

        mGoogleMaps = new BRGoogleMapsAPI(this, R.id.map);

        mRequestManager = new BRRequestManagerBiker(this);

        mModalOverlay      = findViewById(R.id.modalOverlay);
        mModalRequestsList = findViewById(R.id.include_modal_requests_list);

        connectDataToListAndEvaluateSelections();
        updateAvailableRequestsDataContinuously();
    }

    @Override
    protected void onDestroy() {
        mRequestManager.removeAllListeners();
        super.onDestroy();
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                    BIKER METHODS                                 |
       |                                                                                  |
       \=================================================================================*/

    private void connectDataToListAndEvaluateSelections () {

        mRequestManager.connectDataToRequestsListAndGetSelection(R.id.newRequestsList,

                new BRRequestManagerBiker.OnRequestSelected() {

                    @Override
                    public void onSelected() {

                    }

                    @Override
                    public void onUnavailable() {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });
    }

    private void updateAvailableRequestsDataContinuously () {

        mRequestManager.monitorAvailableRequests(

                new BRRequestManagerBiker.MonitorRequests() {

                    @Override
                    public void onRequestsUpdate() {
                        enterModalState ();
                    }

                    @Override
                    public void onNoRequestsAtThisMoment() {
                        if (!mHasChosenRequest) exitModalState ();
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

                /*---------------------------------------------------------------\
                                            MODALS LOGIC
                \---------------------------------------------------------------*/

    //region MODALS LOGIC

    private void enterModalState () {
        mAnimate.fadeInIfInvisible(mModalOverlay);
        mAnimate.translateFromBottomIfInvisible(mModalRequestsList);
    }

    private void exitModalState () {
        mAnimate.translateToBottomIfVisible(mModalRequestsList);
        mAnimate.fadeOutIfVisible(mModalOverlay);
    }

    //endregion
}

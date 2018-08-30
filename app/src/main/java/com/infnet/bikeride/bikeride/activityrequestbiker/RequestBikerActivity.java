package com.infnet.bikeride.bikeride.activityrequestbiker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.activitydelivery.DeliveryActivity;
import com.infnet.bikeride.bikeride.models.RequestModel;
import com.infnet.bikeride.bikeride.services.Abstractions;
import com.infnet.bikeride.bikeride.services.Animations;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

public class RequestBikerActivity extends AppCompatActivity {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "RequestBikerActivity";

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    // ---> Animations
    private Animations mAnimate = new Animations(200);

    // ---> BikeRide Request Manager
    private RequestBikerManager mRequestManager;

    // ---> Customized setContentView with navigation drawer and toolbar
    private ContentViewBuilder mContentViewBuilder;

    // ---> View variables
    private View mModalOverlay;
    private View mModalRequestsList;
    private View mModalAwaitingConfirmation;
    private ImageView mRequestWaitingWheel;

    // ---> Control variables
    private boolean mHasChosenRequest = false;

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
                R.layout.activity_request_biker);

        mAbst = new Abstractions(this);

        mRequestManager = new RequestBikerManager(this);

        mModalOverlay      = findViewById(R.id.modalOverlay);
        mModalRequestsList = findViewById(R.id.include_modal_requests_list);
        mModalAwaitingConfirmation = findViewById(R.id.include_modal_awaiting_confirmation);
        mRequestWaitingWheel = findViewById(R.id.awaitingRequestConfirmationWaitingWheel);

        mAnimate.rotate360Infinitely(mRequestWaitingWheel, 2000);

        connectDataToListAndEvaluateSelections();
        updateAvailableRequestsDataContinuously();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestManager.verifyPermissionsRequestResult(requestCode, grantResults);
    }

    @Override
    protected void onStop() {

        mRequestManager.removeLocationListener();
        mRequestManager.removeAllValueEventListeners();
        mRequestManager.deleteThisBikersObjectFromAvailableBikersNode();

        super.onStop();
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                    BIKER LOGIC                                   |
       |                                                                                  |
       \=================================================================================*/

    //region BIKER LOGIC

    private void connectDataToListAndEvaluateSelections () {

        mRequestManager.connectDataToRequestsListAndManageSelection(R.id.newRequestsList,

            new RequestBikerManager.OnRequestSelected() {

                @Override
                public void onSelected() {
                    mAnimate.swapViewsLeft(mModalRequestsList, mModalAwaitingConfirmation);

                    mHasChosenRequest = true;
                }

                @Override
                public void onUnavailable() {

                    mHasChosenRequest = false;

                    Toast.makeText(getApplicationContext(),
                            "Request not available anymore",
                            Toast.LENGTH_SHORT).show();

                    if (mRequestManager.noRequestsAvailable()) exitModalState();
                    else mAnimate.swapViewsRight(mModalAwaitingConfirmation, mModalRequestsList);
                }

                @Override
                public void onSuccess(final RequestModel request) {

                    Intent newIntent = new Intent(getApplicationContext(), DeliveryActivity.class);
                    newIntent.putExtra("isBiker", "true");
                    newIntent.putExtra("requestData", request);
                    startActivity(newIntent);
                }

                @Override
                public void onProcedureTimeout() {

                    mHasChosenRequest = false;

                    Toast.makeText(getApplicationContext(),
                            "Timed out",
                            Toast.LENGTH_SHORT).show();

                    if (mRequestManager.noRequestsAvailable()) exitModalState();
                    else mAnimate.swapViewsRight(mModalAwaitingConfirmation, mModalRequestsList);
                }
            });
    }

    private void updateAvailableRequestsDataContinuously () {

        mRequestManager.monitorAvailableRequests(

            new RequestBikerManager.MonitorRequests() {

                @Override
                public void onRequestsUpdate() {
                    enterModalState ();
                }

                @Override
                public void onNoRequestsAtThisMoment() {

                    if (!mHasChosenRequest) exitModalState();
                }

                @Override
                public void onError() {

                }
            });
    }

    //endregion

                /*---------------------------------------------------------------\
                                            MODALS LOGIC
                \---------------------------------------------------------------*/

    //region MODALS LOGIC

    private void enterModalState () {
        mAnimate.fadeInIfInvisible(mModalOverlay);
        mAnimate.translateFromBottomIfInvisible(mModalRequestsList);
    }

    private void exitModalState () {
        mAnimate.translateToBottomIfVisible(mModalRequestsList, mModalAwaitingConfirmation);
        mAnimate.fadeOutIfVisible(mModalOverlay);
    }

    //endregion
}

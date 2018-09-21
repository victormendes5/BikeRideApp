package com.infnet.bikeride.bikeride.activitydelivery;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.infnet.bikeride.bikeride.DeliverymanReviewActivity;
import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.activityrequestbiker.RequestBikerActivity;
import com.infnet.bikeride.bikeride.activityrequestuser.RequestUserActivity;
import com.infnet.bikeride.bikeride.services.Abstractions;
import com.infnet.bikeride.bikeride.services.Animations;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

public class DeliveryActivity extends AppCompatActivity {


    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    private static final String TAG = "DeliveryActivity";


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    // ---> Layout elements
    private Button mCancelDeliveryBtn, mConfirmPickupBtn, mConfirmDeliveryBtn;
    private View mModalOverlay, mModalAwaitingDelivery, mModalWrapping, mModalCancelled;
    private View mDeliveryDisplay;
    private TextView mAwaitingTitle, mAwaitingMessage;
    private ImageView mWaitingWheel, mWrappingWheel, mCancelledWheel;
    private TextView mDisplayRequesterName, mDisplayBikerName, mDisplayHeadingAddress,
            mDisplayPackageInfo;

    // ---> Animations
    private Animations mAnimate = new Animations(200);

    // ---> Customized setContentView with navigation drawer and toolbar
    private ContentViewBuilder mContentViewBuilder;

    // ---> Business logic
    private DeliveryManager mManager;

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

        mAbst = new Abstractions(this);

        mAbst.connectVariableToViewIdAndOnClickMethod(
                "mCancelDeliveryBtn", R.id.cancelDeliveryBtn, "oC_cancelDeliveryBtn",
                "mConfirmPickupBtn", R.id.confirmPickupBtn, "oC_confirmPickupBtn",
                "mConfirmDeliveryBtn", R.id.confirmDeliveryBtn, "oC_confirmDeliveryBtn");

        mModalOverlay = findViewById(R.id.modalOverlay);
        mModalAwaitingDelivery = findViewById(R.id.include_modal_awaiting_delivery);
        mModalWrapping = findViewById(R.id.include_modal_awaiting_wrapping);
        mModalCancelled = findViewById(R.id.include_modal_awaiting_cancelled);
        mAwaitingTitle = findViewById(R.id.awaitingTitle);
        mAwaitingMessage = findViewById(R.id.awaitingMessage);
        mWaitingWheel = findViewById(R.id.awaitingConfirmationWaitingWheel);
        mWrappingWheel = findViewById(R.id.awaitingWrappingWaitingWheel);
        mCancelledWheel = findViewById(R.id.awaitingCancelledWaitingWheel);
        mDisplayRequesterName = findViewById(R.id.requestername);
        mDisplayBikerName = findViewById(R.id.bikername);
        mDisplayHeadingAddress = findViewById(R.id.headingAddress);
        mDisplayPackageInfo = findViewById(R.id.packageInfo);

        mAnimate.rotate360Infinitely(mWaitingWheel, 2000);
        mAnimate.rotate360Infinitely(mWrappingWheel, 2000);
        mAnimate.rotate360Infinitely(mCancelledWheel, 2000);

        initializeManagerAndMonitorBiker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mManager.verifyPermissionsRequestResult(requestCode, grantResults);
    }

    @Override
    protected void onStop() {

        if (mManager.isBiker()) mManager.removeLocationListener();
        mManager.removeAllValueEventListeners();

        super.onStop();
    }



    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                  DELIVERY LOGIC                                  |
       |                                                                                  |
       \=================================================================================*/

                /*---------------------------------------------------------------\
                                               LOGIC
                \---------------------------------------------------------------*/

    //region LOGIC

    private void initializeManagerAndMonitorBiker () {

        mManager = new DeliveryManager(this,

            new DeliveryManager.OnUiUpdate() {

                @Override
                public void isUnderWay() { bikerIsUnderWay(); }

                @Override
                public void onBikerIsAtPickupAddress() { bikerIsAtPickupAddress(); }

                @Override
                public void onBikerIsAtDeliveryAddress() { bikerIsAtDeliveryAddress(); }

                @Override
                public void onCancel() { requestCancelled(); }

                @Override
                public void onComplete() { deliveryCompleted(); }

                @Override
                public void onFinish() {

//                    if (mManager.isBiker()) mAbst.navigate(RequestBikerActivity.class);  //TODO ANTIGO!
//                    else mAbst.navigate(RequestUserActivity.class);
                    if (mManager.isBiker()) mAbst.navigate(DeliverymanReviewActivity.class);  // TODO NOVO PARA DEBUG
                    else mAbst.navigate(DeliverymanReviewActivity.class);
                }
            });

        initializeDeliveryDisplay();
    }

    private void initializeDeliveryDisplay () {

        mDisplayRequesterName.setText(mManager.getRequestContract().sendersName);
        mDisplayBikerName.setText(mManager.getRequestContract().bikerName);
        mDisplayHeadingAddress.setText(mManager.getRequestContract().
                getPickupAddressShrt(33));

        String packageInfo = mManager.getRequestContract().packageSize + " package, " +
                mManager.getRequestContract().packageType + " shape.";

        mDisplayPackageInfo.setText(packageInfo);

        if (mManager.isBiker()) {
            mDeliveryDisplay = findViewById(R.id.bikerDeliveryData);
        }

        else {

            mDeliveryDisplay = findViewById(R.id.requesterDeliveryData);
        }

        mAnimate.translateFromBottomIfInvisible(mDeliveryDisplay);
    }

    private void bikerIsUnderWay () {

        // ---> Biker flow
        if (mManager.isBiker()) {

            // ---> Pickup IS confirmed
            if (mManager.isPickupConfirmed()) {

                mDisplayHeadingAddress.setText(mManager.getRequestContract()
                        .getDeliveryAddressShrt(33));

                mDisplayRequesterName.setText(mManager.getRequestContract().receiversName);

                exitModalState();

                mAnimate.translateToRightIfVisible(mConfirmDeliveryBtn);
            }

            // ---> Pickup NOT confirmed
            else {
                mAnimate.swapViewsAimingRigthSequentiallyIfVisible(mConfirmPickupBtn,
                        mCancelDeliveryBtn);
            }
        }

        /// ---> Requester flow
        else {

            if (mManager.bikerConfirmedPickupAndAwaitsRequesterConfirmation()) {

                mAnimate.translateFromRightIfInvisible(mConfirmPickupBtn);
            }

            else if (!mManager.isPickupConfirmed()) {

                mAnimate.translateFromRightIfInvisible(mCancelDeliveryBtn);
            }


        }
    }

    private void bikerIsAtPickupAddress () {

        // ---> Biker flow
        if (mManager.isBiker()) {

            if (!mManager.bikerConfirmedPickupAndAwaitsRequesterConfirmation()) {

                mAnimate.swapViewsAimingRigthSequentiallyIfVisible(mCancelDeliveryBtn,
                        mConfirmPickupBtn);
            }

            else if (mManager.isPickupConfirmed()) exitModalState();
        }

        // ---> Requester flow
        else {

            mAnimate.translateToRightIfVisible(mCancelDeliveryBtn);

            if (mManager.bikerConfirmedPickupAndAwaitsRequesterConfirmation()) {

                mAnimate.translateFromRightIfInvisible(mConfirmPickupBtn);
            }

            else if (mManager.isPickupConfirmed()) {

                mAnimate.translateToRightIfVisible(mConfirmPickupBtn);
            }
        }
    }

    private void bikerIsAtDeliveryAddress () {

        // ---> Biker flow
        if (mManager.isBiker()) {

            if (mManager.isDeliveryConfirmed()) {
                exitModalState();
            }
            else if (mManager.bikerConfirmedDeliveryAndAwaitsRequesterConfirmation()) {

            }

            else mAnimate.translateFromRightIfInvisible(mConfirmDeliveryBtn);
        }

        // ---> Requester flow
        else {

            if (mManager.bikerConfirmedDeliveryAndAwaitsRequesterConfirmation()) {

                mAnimate.translateFromRightIfInvisible(mConfirmDeliveryBtn);
            }

            else if (mManager.isDeliveryConfirmed()) {

                mAnimate.translateToRightIfVisible(mConfirmDeliveryBtn);
            }
        }
    }

    private void requestCancelled () {

        mAnimate.translateToLeftIfVisible(mCancelDeliveryBtn);

        // ---> if is BIKER
        if (mManager.isBiker()) {

            if (mModalOverlay.getVisibility() == View.VISIBLE) {

                mAnimate.translateToLeftIfVisible(mModalWrapping,
                        mModalAwaitingDelivery);

                mAnimate.translateFromRightIfInvisible(mModalCancelled);
            }

            else {

                enterModalState("cancel");
            }

            new android.os.Handler().postDelayed(

                    new Runnable() {

                        public void run() {

                            mAbst.navigate(RequestBikerActivity.class);
                        }

                    }, 3000);
        }

        // ---> If is REQUESTER
        else {

            enterModalState("cancel");

            new android.os.Handler().postDelayed(

                    new Runnable() {

                        public void run() {

                            mAbst.navigate(RequestUserActivity.class);
                        }

                    }, 3000);
        }
    }

    private void deliveryCompleted () {

        mAwaitingTitle.setText("Delivery completed!");

        if (mManager.isBiker()) {

            mAnimate.translateToLeftIfVisible(mModalAwaitingDelivery);
            mAnimate.translateFromRightIfInvisible(mModalWrapping);

            mManager.finishDeliveryBiker();
        }

        else {

            mAwaitingTitle.setText("Delivery completed!");
            mAwaitingMessage.setText("Wrapping up ...");
            enterModalState("");
            mManager.finishDeliveryRequester();
        }
    }

    //endregion


                /*---------------------------------------------------------------\
                                          USER INTERACTION
                \---------------------------------------------------------------*/

    //region USER INTERACTION

    private void oC_cancelDeliveryBtn () {

        // ---> Biker flow
        if (mManager.isBiker()) {

        }

        // ---> Requester flow
        else {

        }

        mManager.cancelRequest();
    }

    private void oC_confirmPickupBtn () {

        // ---> Biker flow
        if (mManager.isBiker()) {

            enterModalState("");
            mAwaitingTitle.setText("Package received!");
            mAnimate.translateToRightIfVisible(mConfirmPickupBtn);
            mManager.bikerConfirmsPickup();
        }

        // ---> Requester flow
        else {

            mManager.requesterConfirmsPickup();
        }
    }

    private void oC_confirmDeliveryBtn () {

        // ---> Biker flow
        if (mManager.isBiker()) {

            enterModalState("");
            mAwaitingTitle.setText("Package delivered!");
            mAnimate.translateToRightIfVisible(mConfirmDeliveryBtn);
            mManager.bikerConfirmsDelivery();
        }

        // ---> Requester flow
        else {

            mManager.requesterConfirmsDelivery();
        }
    }

    //endregion


                /*---------------------------------------------------------------\
                                            MODALS LOGIC
                \---------------------------------------------------------------*/

    //region MODALS LOGIC

    private void enterModalState (String type) {

        mAnimate.fadeInIfInvisible(mModalOverlay);
        mAnimate.translateToBottomIfVisible(mDeliveryDisplay);

        if (type.equals("cancel")) {

            mAnimate.translateFromBottomIfInvisible(mModalCancelled);
        }

        else {

            mAnimate.translateFromBottomIfInvisible(mModalAwaitingDelivery);
        }
    }

    private void exitModalState () {
         mAnimate.translateToBottomIfVisible(mModalAwaitingDelivery);
         mAnimate.fadeOutIfVisible(mModalOverlay);
         mAnimate.translateFromBottomIfInvisible(mDeliveryDisplay);
    }

    //endregion
}

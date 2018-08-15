package com.infnet.bikeride.bikeride.activityrequestuser;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.activitydelivery.DeliveryActivity;
import com.infnet.bikeride.bikeride.services.Abstractions;
import com.infnet.bikeride.bikeride.services.Animations;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;
import com.infnet.bikeride.bikeride.services.GoogleMapsAPI;
import com.infnet.bikeride.bikeride.services.GooglePlacesAPI;

public class RequestUserActivity extends AppCompatActivity {

    /*=======================================================================================
                                             CONSTANTS
     =======================================================================================*/

    //region CONSTANTS

    private static final String TAG = "RequestUserActivity";

    //endregion


    /*=======================================================================================
                                             VARIABLES
     =======================================================================================*/

    //region VARIABLES

    // ---> Modals
    private View mModalPackageInformation, mModalAddressInformation, mModalSearchingBiker,
            mModalChoosePickupAddress, mModalChooseDeliveryAddress, mModalRequestDetails,
            mModalAwaitingEstimates;
    private RelativeLayout mModalOverlay;

    // ---> Modals interaction
    private ImageView mPackageInfoCloseModals, mAddressesCloseModals, mAddressesBack,
            mBikerSearchWaitingWheel, mPickupAddressBackIcon, mPickupAddressCloseIcon,
            mDeliveryAddressBackIcon, mDeliveryAddressCloseIcon, mRequestDetailsBackIcon,
            mRequestDetailsCloseIcon, mAwaitingEstimatesWaitingWheel;

    // ---> Buttons
    private Button mRequestBikerBtn, mEnterAddressBtn, mFindBikerBtn, mBikerSearchCancelBtn,
            mChoosePickupAddrBtn, mChooseDeliveryAddrBtn, mConfirmPickupAddrBtn,
            mConfirmDeliveryAddrBtn, mConfirmBikerRequestBtn;

    // ---> Type selectors and backgrounds
    private RelativeLayout mPackageTypeMailSlc, mPackageTypeBoxSlc, mPackageTypeUnusualSlc;
    private View mPackageTypeMailSlc_bg, mPackageTypeBoxSlc_bg, mPackageTypeUnusualSlc_bg;

    // ---> Size selectors and backgrounds
    private RelativeLayout mPackageSizeSmallSlc, mPackageSizeMediumSlc, mPackageSizeLargeSlc;
    private View mPackageSizeSmallSlc_bg, mPackageSizeMediumSlc_bg, mPackageSizeLargeSlc_bg;

    // ---> Estimates display textviews
    private TextView mEstimatesPickupDistanceTxv, mEstimatesPickupDurationTxv,
            mEstimatesDeliveryDistanceTxv, mEstimatesDeliveryDurationTxv, mEstimatesFeeTxv,
            mDetailsPickupLocation, mDetailsDeliveryLocation;

    // ---> Address AutoComplete textviews;
    private AutoCompleteTextView mPickupAddressAtxv, mDeliveryAddressAtxv;

    // ---> EditTexts
    private EditText mSenderNameEditText, mReceiverNameEditText;

    // ---> Animations
    private Animations mAnimate = new Animations(200);

    // ---> Google APIs
    GoogleMapsAPI mGoogleMaps;
    GooglePlacesAPI mGooglePlaces;

    // ---> BikeRide Request Manager
    RequestUserManager mRequestManager;

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    // ---> General abstractions;
    Abstractions mAbst;

    //endregion


    /*=======================================================================================
                                        ACTIVITY MAIN METHODS
     =======================================================================================*/

    //region ACTIVITY MAIN METHODS (ONCREATE, ONBACKPRESSED, ONREQUESTPERMISSIONRESULT, ONDESTROY)

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_request_user);

        mGoogleMaps = new GoogleMapsAPI(this, R.id.map);

        mGooglePlaces = new GooglePlacesAPI(this);
        mGooglePlaces.setAutoComplete(R.id.pickupAddAutoCompTxtView,
                R.id.deliveryAddAutoCompTxtView);

        mRequestManager = new RequestUserManager(this);

        mAbst = new Abstractions(this);

        //region CONNECT VARIABLES TO VIEW AND ON CLICK METHODS

        // ---> Modals
        mAbst.connectVariableToViewIdAndOnClickMethod(
                "mModalOverlay", R.id.modalOverlay, "exitModalState",
                "mModalPackageInformation", R.id.include_modal_package_info, "",
                "mModalAddressInformation", R.id.include_modal_addresses, "",
                "mModalSearchingBiker", R.id.include_modal_searching_biker, "",
                "mModalChoosePickupAddress", R.id.include_modal_choose_pickup_address, "",
                "mModalChooseDeliveryAddress", R.id.include_modal_choose_delivery_address, "",
                "mModalRequestDetails", R.id.include_modal_request_details, "",
                "mModalAwaitingEstimates", R.id.include_modal_awaiting_estimates, ""
        );

        // ---> Modals interaction
        mAbst.connectVariableToViewIdAndOnClickMethod(
                "mPackageInfoCloseModals", R.id.packageInfoCloseModals, "exitModalState",
                "mAddressesCloseModals", R.id.addressesCloseModals, "exitModalState",
                "mAddressesBack", R.id.addressesBack, "oC_addressesBack",
                "mPickupAddressBackIcon", R.id.pickupAddressBackIcon, "oC_pickupAddressBackIcon",
                "mPickupAddressCloseIcon", R.id.pickupAddressCloseIcon, "exitModalState",
                "mDeliveryAddressBackIcon", R.id.deliveryAddressBackIcon, "oC_deliveryAddressBackIcon",
                "mDeliveryAddressCloseIcon", R.id.deliveryAddressCloseIcon, "exitModalState",
                "mRequestDetailsBackIcon", R.id.requestDetailsBackIcon, "oC_requestDetailsBackIcon",
                "mRequestDetailsCloseIcon", R.id.requestDetailsCloseIcon, "exitModalState"
        );

        // ---> Buttons
        mAbst.connectVariableToViewIdAndOnClickMethod(
                "mRequestBikerBtn", R.id.requestBikerBtn, "oC_requestBikerBtn",
                "mEnterAddressBtn", R.id.enterAddressBtn, "oC_enterAddressBtn",
                "mFindBikerBtn", R.id.getEstimatesBtn, "oC_getEstimatesBtn",
                "mBikerSearchCancelBtn", R.id.bikerSearchCancelBtn, "oC_bikerSearchCancelBtn",
                "mChoosePickupAddrBtn", R.id.choosePickupAddrBtn, "oC_choosePickupAddrBtn",
                "mChooseDeliveryAddrBtn", R.id.chooseDeliveryAddrBtn, "oC_chooseDeliveryAddrBtn",
                "mConfirmPickupAddrBtn", R.id.confirmPickupAddrBtn, "oC_confirmPickupAddrBtn",
                "mConfirmDeliveryAddrBtn", R.id.confirmDeliveryAddrBtn, "oC_confirmDeliveryAddrBtn",
                "mConfirmBikerRequestBtn", R.id.confirmBikerRequestBtn, "oC_confirmBikerRequestBtn"
        );

        // ---> Selectors
        mAbst.connectVariableToViewIdAndOnClickMethod(
                "mPackageTypeMailSlc", R.id.packageTypeMailSlc, "oC_packageTypeMailSlc",
                "mPackageTypeBoxSlc", R.id.packageTypeBoxSlc, "oC_packageTypeBoxSlc",
                "mPackageTypeUnusualSlc", R.id.packageTypeUnusualSlc, "oC_packageTypeUnusualSlc",
                "mPackageSizeSmallSlc", R.id.packageSizeSmallSlc, "oC_packageSizeSmallSlc",
                "mPackageSizeMediumSlc", R.id.packageSizeMediumSlc, "oC_packageSizeMediumSlc",
                "mPackageSizeLargeSlc", R.id.packageSizeLargeSlc, "oC_packageSizeLargeSlc"
        );

        mAbst.connectVariableToViewIdAndOnChangeMethod(
                "mSenderNameEditText", R.id.senderNameEditText, "oCh_verifyAddressesEdtTxt",
                "mReceiverNameEditText", R.id.receiverNameEditText, "oCh_verifyAddressesEdtTxt",
                "mPickupAddressAtxv", R.id.pickupAddAutoCompTxtView, "oCh_verifyAddressesEdtTxt",
                "mDeliveryAddressAtxv", R.id.deliveryAddAutoCompTxtView, "oCh_verifyAddressesEdtTxt"
        );

        //endregion

        //region FIND VIEWS BY ID

        // ---> Selectors' backgrounds
        mPackageTypeMailSlc_bg     = findViewById(R.id.packageTypeMailSlc_bg);
        mPackageTypeBoxSlc_bg      = findViewById(R.id.packageTypeBoxSlc_bg);
        mPackageTypeUnusualSlc_bg  = findViewById(R.id.packageTypeUnusualSlc_bg);
        mPackageSizeSmallSlc_bg    = findViewById(R.id.packageSizeSmallSlc_bg);
        mPackageSizeMediumSlc_bg   = findViewById(R.id.packageSizeMediumSlc_bg);
        mPackageSizeLargeSlc_bg    = findViewById(R.id.packageSizeLargeSlc_bg);

        // ---> Rotating bike wheels
        mBikerSearchWaitingWheel = findViewById(R.id.bikerSearchWaitingWheel);
        mAwaitingEstimatesWaitingWheel = findViewById(R.id.awaitingEstimatesWaitingWheel);

        // ---> Estimates display textviews
        mEstimatesPickupDistanceTxv = findViewById(R.id.estimatesPickupDistance);
        mEstimatesPickupDurationTxv = findViewById(R.id.estimatesPickupDuration);
        mEstimatesDeliveryDistanceTxv = findViewById(R.id.estimatesDeliveryDistance);
        mEstimatesDeliveryDurationTxv = findViewById(R.id.estimatesDeliveryDuration);
        mEstimatesFeeTxv = findViewById(R.id.estimatesFee);
        mDetailsPickupLocation = findViewById(R.id.detailsPickupLocation);
        mDetailsDeliveryLocation = findViewById(R.id.detailsDeliveryLocation);

        //endregion

        // ---> Activate continuous animations
        mAnimate.rotate360Infinitely(mBikerSearchWaitingWheel, 2000);
        mAnimate.rotate360Infinitely(mAwaitingEstimatesWaitingWheel, 2000);
    }

    @Override
    public void onBackPressed() {

        if (mModalOverlay.getVisibility() == View.VISIBLE) {
            exitModalState();
        } else {
            if (mContentViewBuilder.isNavigationDrawerClosed()) {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mGoogleMaps.verifyPermissionRequestResult(requestCode, grantResults);
    }

    @Override
    public void onStop() {

        mRequestManager.removeAllListeners();
        super.onStop();
    }

    //endregion


       /*=================================================================================\
       |                                                                                  |
       |                                  ONCLICK METHODS                                 |
       |                                                                                  |
       \=================================================================================*/


                /*---------------------------------------------------------------\
                                            CORE LOGIC
                \---------------------------------------------------------------*/

    //region CORE LOGIC

    private void oC_getEstimatesBtn() {
        mAnimate.swapViewsLeft(mModalAddressInformation, mModalAwaitingEstimates);

        mRequestManager.getEstimates(

            new RequestUserManager.GetEstimatesResponses() {

                @Override
                public void onSuccess() {

                    mEstimatesPickupDistanceTxv.setText(mRequestManager.getPickupDistanceEstimate());
                    mEstimatesPickupDurationTxv.setText(mRequestManager.getPickupDurationEstimate());
                    mEstimatesDeliveryDistanceTxv.setText(mRequestManager.getDeliveryDistanceEstimate());
                    mEstimatesDeliveryDurationTxv.setText(mRequestManager.getDeliveryDurationEstimate());
                    mEstimatesFeeTxv.setText(mRequestManager.getFeeEstimate());
                    mDetailsPickupLocation.setText(mRequestManager.getPickupAddressShort());
                    mDetailsDeliveryLocation.setText(mRequestManager.getDeliveryAddressShort());

                    mAnimate.swapViewsLeft(mModalAwaitingEstimates,
                            mModalRequestDetails);
                }

                @Override
                public void onInvalidAddresses() {
                    mAnimate.swapViewsRight(mModalAwaitingEstimates,
                            mModalAddressInformation);

                    Toast.makeText(getApplicationContext(), "Invalid address",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void noBikersAvailable() {
                    mAnimate.swapViewsRight(mModalAwaitingEstimates,
                            mModalAddressInformation);

                    Toast.makeText(getApplicationContext(), "No bikers available",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {
                    mAnimate.swapViewsRight(mModalAwaitingEstimates,
                            mModalAddressInformation);

                    Toast.makeText(getApplicationContext(), "Some error occurred",
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void oC_confirmBikerRequestBtn() {
        mAnimate.swapViewsLeft(mModalRequestDetails, mModalSearchingBiker);

        mRequestManager.setSendersName(mSenderNameEditText.getText().toString());
        mRequestManager.setReceiversName(mReceiverNameEditText.getText().toString());

        mRequestManager.postNewDeliveryRequest(

            new RequestUserManager.RequestStatus() {

                @Override
                public void onRequestAccepted() {
                    mAbst.navigate(DeliveryActivity.class);
                }

                @Override
                public void onSearchTimedOut() {
                    mAnimate.swapViewsRight(mModalSearchingBiker, mModalRequestDetails);

                    Toast.makeText(getApplicationContext(), "Search timed out",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError() {
                    mAnimate.swapViewsRight(mModalSearchingBiker, mModalRequestDetails);

                    Toast.makeText(getApplicationContext(), "Some error occurred",
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void oC_bikerSearchCancelBtn () {
        mAnimate.swapViewsRight(mModalSearchingBiker, mModalRequestDetails);
        mRequestManager.cancelNonAcceptedRequest();
    }

    private void oC_confirmPickupAddrBtn () {
        mRequestManager.setPickupAddress(mPickupAddressAtxv.getText().toString());
        mAnimate.crossFadeViews(mModalChoosePickupAddress, mModalAddressInformation,
                0,400);
    }

    private void oC_confirmDeliveryAddrBtn () {
        mRequestManager.setDeliveryAddress(mDeliveryAddressAtxv.getText().toString());
        mAnimate.crossFadeViews(mModalChooseDeliveryAddress, mModalAddressInformation,
                0,400);
    }

    //endregion


                /*---------------------------------------------------------------\
                                            SELECTORS
                \---------------------------------------------------------------*/

    //region SELECTORS METHODS (REFLECTED)

    private void oC_packageTypeMailSlc() {
        mRequestManager.setPackageType("mail");
        mAnimate.selectionFader(mPackageTypeMailSlc_bg, mPackageTypeBoxSlc_bg,
                mPackageTypeUnusualSlc_bg);
        oCh_verifyTypeAndSizeSelectors();
    }

    private void oC_packageTypeBoxSlc() {
        mRequestManager.setPackageType("box");
        mAnimate.selectionFader(mPackageTypeBoxSlc_bg, mPackageTypeMailSlc_bg,
                mPackageTypeUnusualSlc_bg);
        oCh_verifyTypeAndSizeSelectors();
    }

    private void oC_packageTypeUnusualSlc() {
        mRequestManager.setPackageType("unusual");
        mAnimate.selectionFader(mPackageTypeUnusualSlc_bg, mPackageTypeBoxSlc_bg,
                mPackageTypeMailSlc_bg);
        oCh_verifyTypeAndSizeSelectors();
    }

    private void oC_packageSizeSmallSlc() {
        mRequestManager.setPackageSize("Small");
        mAnimate.selectionFader(mPackageSizeSmallSlc_bg, mPackageSizeMediumSlc_bg,
                mPackageSizeLargeSlc_bg);
        oCh_verifyTypeAndSizeSelectors();
    }

    private void oC_packageSizeMediumSlc() {

        mRequestManager.setPackageSize("Medium");
        mAnimate.selectionFader(mPackageSizeMediumSlc_bg, mPackageSizeSmallSlc_bg,
                mPackageSizeLargeSlc_bg);
        oCh_verifyTypeAndSizeSelectors();
    }

    private void oC_packageSizeLargeSlc() {
        mRequestManager.setPackageSize("Large");
        mAnimate.selectionFader(mPackageSizeLargeSlc_bg, mPackageSizeMediumSlc_bg,
                mPackageSizeSmallSlc_bg);
        oCh_verifyTypeAndSizeSelectors();
    }

    //endregion


                /*---------------------------------------------------------------\
                                          NAVIGATION ONLY
                \---------------------------------------------------------------*/

    //region NAVIGATION ONLY METHODS (REFLECTED)

    private void oC_requestBikerBtn () {
        enterModalState();
    }

    private void oC_enterAddressBtn () {
        mAnimate.swapViewsLeft(mModalPackageInformation, mModalAddressInformation);
    }

    private void oC_choosePickupAddrBtn () {
        mAnimate.crossFadeViews(mModalAddressInformation, mModalChoosePickupAddress,
                0,400);
    }

    private void oC_chooseDeliveryAddrBtn () {
        mAnimate.crossFadeViews(mModalAddressInformation, mModalChooseDeliveryAddress,
                0,400);
    }

    private void oC_addressesBack () {
        mAnimate.swapViewsRight(mModalAddressInformation, mModalPackageInformation);
    }

    private void oC_deliveryAddressBackIcon () {
        mAnimate.crossFadeViews(mModalChooseDeliveryAddress, mModalAddressInformation,
                0,400);
    }

    private void oC_requestDetailsBackIcon () {
        mAnimate.swapViewsRight(mModalRequestDetails, mModalAddressInformation);
    }

    private void oC_pickupAddressBackIcon () {
        mAnimate.crossFadeViews(mModalChoosePickupAddress, mModalAddressInformation,
                0, 400);
    }

    //endregion


                /*---------------------------------------------------------------\
                                      ON CHANGE ERROR VERIFIERS
                \---------------------------------------------------------------*/

    //region ERROR VERIFYING METHODS (REFLECTED)

    private void oCh_verifyAddressesEdtTxt () {

        if (mAbst.checkForNullEditTextContent(
                mSenderNameEditText,
                mReceiverNameEditText,
                mPickupAddressAtxv,
                mDeliveryAddressAtxv)) {

            mAnimate.fadeOutIfVisible(mFindBikerBtn);
        }

        else {
            mAnimate.fadeInIfInvisible(mFindBikerBtn);

        }
    }

    private void oCh_verifyTypeAndSizeSelectors() {

        boolean isTypeSelected =  mAbst.checkForViewVisibility(mPackageTypeMailSlc_bg,
                mPackageTypeBoxSlc_bg, mPackageTypeUnusualSlc_bg);

        boolean isSizeSelected = mAbst.checkForViewVisibility(mPackageSizeSmallSlc_bg,
                mPackageSizeMediumSlc_bg, mPackageSizeLargeSlc_bg);

        if (isTypeSelected && isSizeSelected) {

            mAnimate.fadeInIfInvisible(mEnterAddressBtn);
        }

        else {

            mAnimate.fadeOutIfVisible(mEnterAddressBtn);
        }
    }

    //endregion


                /*---------------------------------------------------------------\
                                            MODALS LOGIC
                \---------------------------------------------------------------*/

    //region MODALS METHODS

    private void enterModalState () {
        mAnimate.crossFadeViews(mRequestBikerBtn, mModalOverlay);
        mAnimate.translateFromBottom(mModalPackageInformation, 200);
    }

    private void exitModalState () {
        mAnimate.translateToBottomIfVisible(mModalAddressInformation, mModalPackageInformation,
                mModalSearchingBiker, mModalChoosePickupAddress, mModalChooseDeliveryAddress,
                mModalRequestDetails);

        mAnimate.crossFadeViews(mModalOverlay, mRequestBikerBtn, 200);

        mAnimate.fadeAllOut(mPackageTypeMailSlc_bg, mPackageTypeBoxSlc_bg,
                mPackageTypeUnusualSlc_bg, mPackageSizeSmallSlc_bg,
                mPackageSizeMediumSlc_bg, mPackageSizeLargeSlc_bg, mEnterAddressBtn,
                mFindBikerBtn);

        mRequestManager.resetRequestProperties();

        mAbst.setEditTextContentToEmpty(mSenderNameEditText, mReceiverNameEditText,
                mPickupAddressAtxv, mDeliveryAddressAtxv);
    }

    //endregion
}

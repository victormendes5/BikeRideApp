package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class DeliveryMainActivity extends AppCompatActivity {

    // ---> Modals
    private View mModalPackageInformation, mModalAddressInformation, mModalSearchingBiker,
            mModalChoosePickupAddress, mModalChooseDeliveryAddress, mModalRequestDetails;
    private RelativeLayout mModalOverlay;

    // ---> Modals interaction
    private ImageView mPackageInfoCloseModals, mAddressesCloseModals, mAddressesBack,
            mBikerSearchWaitingWheel, mPickupAddressBackIcon, mPickupAddressCloseIcon,
            mDeliveryAddressBackIcon, mDeliveryAddressCloseIcon, mRequestDetailsBackIcon,
            mRequestDetailsCloseIcon;

    // ---> Buttons
    public Button mRequestBikerBtn, mEnterAddressBtn, mFindBikerBtn, mBikerSearchCancelBtn,
            mChoosePickupAddrBtn, mChooseDeliveryAddrBtn, mConfirmPickupAddrBtn,
            mConfirmDeliveryAddrBtn, mConfirmBikerRequestBtn;

    // ---> Type selectors and backgrounds
    private RelativeLayout mPackageTypeMailSlc, mPackageTypeBoxSlc, mPackageTypeUnusualSlc;
    private View mPackageTypeMailSlc_bg, mPackageTypeBoxSlc_bg, mPackageTypeUnusualSlc_bg;

    // ---> Size selectors and backgrounds
    private RelativeLayout mPackageSizeSmallSlc, mPackageSizeMediumSlc, mPackageSizeLargeSlc;
    private View mPackageSizeSmallSlc_bg, mPackageSizeMediumSlc_bg, mPackageSizeLargeSlc_bg;

    // ---> Animations
    private BikeRideAnimations mAnimate = new BikeRideAnimations(200);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_delivery);

        BikeRideAbstractions abst = new BikeRideAbstractions(this);

        // ---> Modals
        abst.connectVariableToViewIdAndOnClickMethod(
            "mModalOverlay", R.id.modalOverlay, "exitModalState",
            "mModalPackageInformation", R.id.include_modal_package_info, "",
            "mModalAddressInformation", R.id.include_modal_addresses, "",
            "mModalSearchingBiker", R.id.include_modal_searching_biker, "",
            "mModalChoosePickupAddress", R.id.include_modal_choose_pickup_address, "",
            "mModalChooseDeliveryAddress", R.id.include_modal_choose_delivery_address, "",
            "mModalRequestDetails", R.id.include_modal_request_details, ""
        );

        // ---> Modals interaction
        abst.connectVariableToViewIdAndOnClickMethod(
            "mPackageInfoCloseModals", R.id.packageInfoCloseModals, "exitModalState",
            "mAddressesCloseModal", R.id.addressesCloseModals, "exitModalState",
            "mAddressesBack", R.id.addressesBack, "oC_addressesBack",
            "mPickupAddressBackIcon", R.id.pickupAddressBackIcon, "oC_pickupAddressBackIcon",
            "mPickupAddressCloseIcon", R.id.pickupAddressCloseIcon, "exitModalState",
            "mDeliveryAddressBackIcon", R.id.deliveryAddressBackIcon, "oC_deliveryAddressBackIcon",
            "mDeliveryAddressCloseIcon", R.id.deliveryAddressCloseIcon, "exitModalState",
            "mRequestDetailsBackIcon", R.id.requestDetailsBackIcon, "oC_requestDetailsBackIcon",
            "mRequestDetailsCloseIcon", R.id.requestDetailsCloseIcon, "exitModalState"
        );

        // ---> Buttons
        abst.connectVariableToViewIdAndOnClickMethod(
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
        abst.connectVariableToViewIdAndOnClickMethod(
            "mPackageTypeMailSlc", R.id.packageTypeMailSlc, "oC_packageTypeMailSlc",
            "mPackageTypeBoxSlc", R.id.packageTypeBoxSlc, "oC_packageTypeBoxSlc",
            "mPackageTypeUnusualSlc", R.id.packageTypeUnusualSlc, "oC_packageTypeUnusualSlc",
            "mPackageSizeSmallSlc", R.id.packageSizeSmallSlc, "oC_packageSizeSmallSlc",
            "mPackageSizeMediumSlc", R.id.packageSizeMediumSlc, "oC_packageSizeMediumSlc",
            "mPackageSizeLargeSlc", R.id.packageSizeLargeSlc, "oC_packageSizeLargeSlc"
        );

        // ---> Selectors' backgrounds
        mPackageTypeMailSlc_bg     = findViewById(R.id.packageTypeMailSlc_bg);
        mPackageTypeBoxSlc_bg      = findViewById(R.id.packageTypeBoxSlc_bg);
        mPackageTypeUnusualSlc_bg  = findViewById(R.id.packageTypeUnusualSlc_bg);
        mPackageSizeSmallSlc_bg    = findViewById(R.id.packageSizeSmallSlc_bg);
        mPackageSizeMediumSlc_bg   = findViewById(R.id.packageSizeMediumSlc_bg);
        mPackageSizeLargeSlc_bg    = findViewById(R.id.packageSizeLargeSlc_bg);

        // ---> Rotating bike wheel
        mBikerSearchWaitingWheel = findViewById(R.id.bikerSearchWaitingWheel);

        // ---> Activate continuous animations
        mAnimate.rotate360Infinitely(mBikerSearchWaitingWheel, 2000);
    }

    @Override
    public void onBackPressed() {
        if (mModalOverlay.getVisibility() == View.VISIBLE) {
            exitModalState();
        } else {
            super.onBackPressed();
        }
    }


    //   /==================================================================================\
    //   |                                ONCLICK METHODS                                   |
    //   \==================================================================================/

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

    private void oC_getEstimatesBtn() {
        mAnimate.swapViewsLeft(mModalAddressInformation, mModalRequestDetails);
    }

    private void oC_confirmBikerRequestBtn() {
        mAnimate.swapViewsLeft(mModalRequestDetails, mModalSearchingBiker);
    }

    private void oC_bikerSearchCancelBtn () {
        exitModalState();
    }

    private void oC_confirmPickupAddrBtn () {
        mAnimate.crossFadeViews(mModalChoosePickupAddress, mModalAddressInformation,
                0,400);
    }

    private void oC_pickupAddressBackIcon () {
        mAnimate.crossFadeViews(mModalChoosePickupAddress, mModalAddressInformation,
                0,400);
    }

    private void oC_confirmDeliveryAddrBtn () {
        mAnimate.crossFadeViews(mModalChooseDeliveryAddress, mModalAddressInformation,
                0,400);
    }

    private void oC_deliveryAddressBackIcon () {
        mAnimate.crossFadeViews(mModalChooseDeliveryAddress, mModalAddressInformation,
                0,400);
    }

    private void oC_requestDetailsBackIcon () {
        mAnimate.swapViewsRight(mModalRequestDetails, mModalAddressInformation);
    }

    private void oC_packageTypeMailSlc() {
        mAnimate.selectionFader(mPackageTypeMailSlc_bg, mPackageTypeBoxSlc_bg,
                mPackageTypeUnusualSlc_bg);
    }

    private void oC_packageTypeBoxSlc() {
        mAnimate.selectionFader(mPackageTypeBoxSlc_bg, mPackageTypeMailSlc_bg,
                mPackageTypeUnusualSlc_bg);
    }

    private void oC_packageTypeUnusualSlc() {
        mAnimate.selectionFader(mPackageTypeUnusualSlc_bg, mPackageTypeBoxSlc_bg,
                mPackageTypeMailSlc_bg);
    }

    private void oC_packageSizeSmallSlc() {
        mAnimate.selectionFader(mPackageSizeSmallSlc_bg, mPackageSizeMediumSlc_bg,
                mPackageSizeLargeSlc_bg);
    }

    private void oC_packageSizeMediumSlc() {
        mAnimate.selectionFader(mPackageSizeMediumSlc_bg, mPackageSizeSmallSlc_bg,
                mPackageSizeLargeSlc_bg);
    }

    private void oC_packageSizeLargeSlc() {
        mAnimate.selectionFader(mPackageSizeLargeSlc_bg, mPackageSizeMediumSlc_bg,
                mPackageSizeSmallSlc_bg);
    }

    private void oC_addressesBack () {
        mAnimate.swapViewsRight(mModalAddressInformation, mModalPackageInformation);
    }

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
                mPackageSizeMediumSlc_bg, mPackageSizeLargeSlc_bg);
    }
}

package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    // ~~ Main Profile
    ImageView mProfileImgView;
    CardView mCardViewName, mCardViewNumber, mCardViewEmail, mCardViewPassword;

    // ~~ Modals
    View mModalChangeName;
    ImageView mCloseModals, mBackModals;
    RelativeLayout mModalOverlay;

    // ~~ Modal: Change Name
    TextView mTxtProfileName;
    EditText mEdtProfileName;
    Button mChangeName;

    // ~~ Animations
    private BikeRideAnimations mAnimate = new BikeRideAnimations(200);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        BikeRideAbstractions abst = new BikeRideAbstractions(this);

        // ~~ Main Profile
        abst.connectVariableToViewIdAndOnClickMethod(
                     "mProfileImgView", R.id.profile_photo, "",
                            "mCardViewName", R.id.profile_cardView_edtName, "oC_changeName",
                            "mCardViewNumber", R.id.profile_cardView_edtNumber,"",
                            "mCardViewEmail", R.id.profile_cardView_edtEmail, "",
                            "mCardViewPassword", R.id.profile_cardView_edtPassword, "");

        // ~~ Modals
        abst.connectVariableToViewIdAndOnClickMethod(
                "mCloseModals", R.id.changesCloseModals, "exitModalState",
                       "mModalOverlay", R.id.modalOverlayProfile, "exitModalState",
                       "mModalChangeName", R.id.include_modal_changeName, "");

        // ~~ Modal: Change Name
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileName", R.id.profileNameTitle, "",
                       "mEdtProfileName", R.id.profileNameEditText, "",
                       "mChangeName", R.id.changeNameBtn, "");
    }

    private void oC_changeName () {
        enterModalState();
    }

    private void enterModalState () {
        mAnimate.crossFadeViews(mCardViewName, mModalOverlay);
        mAnimate.translateFromBottom(mModalChangeName, 200);
    }

    @Override
    public void onBackPressed() {
        if (mModalOverlay.getVisibility() == View.VISIBLE) {
            exitModalState();
        } else {
            super.onBackPressed();
        }
    }

    private void exitModalState () {
        mAnimate.translateToBottomIfVisible(mModalChangeName);

        mAnimate.crossFadeViews(mModalOverlay, mCardViewName, 200);
    }

}

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
    CardView mCardViewName, mCardViewLastName, mCardViewNumber, mCardViewEmail, mCardViewPassword;

    // ~~ Modals
    View mModalChangeName, mModalChangeLastName, mModalChangeNumber, mModalChangeEmail;
    ImageView mCloseModalName, mCloseModalLastName, mCloseModalNumber, mCloseModelEmail ;
    RelativeLayout mModalOverlay;

    // ~~ Modal: Change Name
    TextView mTxtProfileName, mTxtProfileLastName, mTxtProfileNumber, mTxtProfileEmail;
    EditText mEdtProfileName, mEdtProfileLastName, mEdtProfileNumber, mEdtProfileEmail;
    Button mChangeName, mChangeLastName, mChangeNumber, mChangeEmail;

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
                            "mCardViewLastName", R.id.profile_cardView_edtLastName, "oC_changeLastName",
                            "mCardViewNumber", R.id.profile_cardView_edtNumber,"oC_changeNumber",
                            "mCardViewEmail", R.id.profile_cardView_edtEmail, "oC_changeEmail",
                            "mCardViewPassword", R.id.profile_cardView_edtPassword, "");

        // ~~ Modals
        abst.connectVariableToViewIdAndOnClickMethod(
                "mCloseModalName", R.id.changesCloseModalName, "exitModalStateName",
                       "mCloseModalLastName", R.id.changesCloseModalLastName, "exitModalStateLastName",
                       "mCloseModalNumber", R.id.changesCloseModalNumber, "exitModalStateNumber",
                       "mModalOverlay", R.id.modalOverlayProfile, "exitModalStates",
                       "mModalChangeLastName", R.id.include_modal_changeLastName, "",
                       "mModalChangeNumber", R.id.include_modal_changeNumber, "",
                       "mModalChangeEmail", R.id.include_modal_changeEmail, "",
                       "mModalChangeName", R.id.include_modal_changeName, "");

        // ~~ Modal: Change Name
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileName", R.id.profileNameTitle, "",
                       "mEdtProfileName", R.id.profileNameEditText, "",
                       "mChangeName", R.id.changeNameBtn, "");

        // ~~ Modal: Change Last Name
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileLastName", R.id.profileLastNameTitle, "",
                       "mEdtProfileLastName", R.id.profileLastNameEditText, "",
                       "mChangeLastName", R.id.changeLastNameBtn, "");

        // ~~ Modal: Change Number
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileNumber", R.id.profileNumberTitle, "",
                       "mEdtProfileNumber", R.id.profileNumberEditText, "",
                       "mChangeNumber", R.id.changeNumberBtn, "");

        // ~~ Modal: Change Email
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileEmail", R.id.profileEmailTitle, "",
                       "mEdtProfileEmail", R.id.profileEmailEditText, "",
                       "mChangeEmail", R.id.changeEmailBtn, "");
    }

    private void oC_changeName () {
        enterModalStateName();
    }

    private void oC_changeLastName () { enterModalStateLastName(); }

    private void oC_changeNumber () { enterModalStateNumber(); }

    private void oC_changeEmail () { enterModalStateEmail(); }

    private void enterModalStateName () {
        mAnimate.crossFadeViews(mCardViewName, mModalOverlay);
        mAnimate.translateFromBottom(mModalChangeName, 200);
    }

    private void enterModalStateLastName () {
        mAnimate.crossFadeViews(mCardViewLastName, mModalOverlay);
        mAnimate.translateFromBottom(mModalChangeLastName, 200);
    }

    private void enterModalStateNumber () {
        mAnimate.crossFadeViews(mCardViewNumber, mModalOverlay);
        mAnimate.translateFromBottom(mModalChangeNumber, 200);
    }

    private void enterModalStateEmail () {
        mAnimate.crossFadeViews(mCardViewEmail, mModalOverlay);
        mAnimate.translateFromBottom(mModalChangeEmail, 200);
    }

    @Override
    public void onBackPressed() {
        if (mModalOverlay.getVisibility() == View.VISIBLE) {
            exitModalStates();
        } else {
            super.onBackPressed();
        }
    }

    private void exitModalStates () {
        exitModalStateName();
        exitModalStateLastName();
        exitModalStateNumber();
        exitModalStateEmail();
    }

    private void exitModalStateName () {
        mAnimate.translateToBottomIfVisible(mModalChangeName);

        mAnimate.crossFadeViews(mModalOverlay, mCardViewName, 200);
    }

    private void exitModalStateLastName () {
        mAnimate.translateToBottomIfVisible(mModalChangeLastName);

        mAnimate.crossFadeViews(mModalOverlay, mCardViewLastName, 200);
    }

    private void exitModalStateNumber () {
        mAnimate.translateToBottomIfVisible(mModalChangeNumber);

        mAnimate.crossFadeViews(mModalOverlay, mCardViewNumber, 200);
    }

    private void exitModalStateEmail () {
        mAnimate.translateToBottomIfVisible(mModalChangeEmail);

        mAnimate.crossFadeViews(mModalOverlay, mCardViewEmail, 200);
    }

}

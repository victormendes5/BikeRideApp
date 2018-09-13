package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.infnet.bikeride.bikeride.services.Abstractions;
import com.infnet.bikeride.bikeride.services.Animations;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

public class ProfileActivity extends AppCompatActivity {

    // ~~ Main Profile
    ImageView mProfileImgView;
    CardView mCardViewName, mCardViewLastName, mCardViewNumber, mCardViewEmail, mCardViewPassword;
//    UserManager mUserManager = new UserManager(this);
    TextView mTxtViewUserName, mTxtViewUserLastname, mTxtViewUserNumber, mTxtViewUserEmail;

    // ~~ Modals
    View mModalChangeName, mModalChangeLastName, mModalChangeNumber, mModalChangeEmail;
    ImageView mCloseModalName, mCloseModalLastName, mCloseModalNumber, mCloseModelEmail ;
    RelativeLayout mModalOverlay;

    // ~~ User props
    TextView mTxtProfileName, mTxtProfileLastName, mTxtProfileNumber, mTxtProfileEmail; // <-- Statics
    EditText mEdtProfileName, mEdtProfileLastName, mEdtProfileNumber, mEdtProfileEmail; // <-- Dinamics
    Button mChangeName, mChangeLastName, mChangeNumber, mChangeEmail;

    // ~~ Animations
    private Animations mAnimate = new Animations(200);

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    // ---> User Manager
    UserManager mUserManager = new UserManager();
//    private Users users;
    Users mUsersNew = new Users();
    private FirebaseAuth autentication;
    //    private String userId = firebaseUser.getUid();
    private Users currentUser = new Users();

    // ---> Constantes para edição
    private static final int EDIT_NAME = 0;
    private static final int EDIT_LASTNAME = 1;
    private static final int EDIT_EMAIL = 2;
    private static final int EDIT_NUMBER = 3;
    private static final int EDIT_PASSWORD = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_profile);

        Abstractions abst = new Abstractions(this);

        // ~~ Main Profile
        abst.connectVariableToViewIdAndOnClickMethod(
                     "mProfileImgView", R.id.profile_photo, "",
                            "mCardViewName", R.id.profile_cardView_edtName, "oC_cardViewChangeName",
                            "mCardViewLastName", R.id.profile_cardView_edtLastName, "oC_cardViewChangeLastName",
                            "mCardViewNumber", R.id.profile_cardView_edtNumber,"oC_cardViewChangeNumber",
                            "mCardViewEmail", R.id.profile_cardView_edtEmail, "oC_cardViewChangeEmail",
                            "mCardViewPassword", R.id.profile_cardView_edtPassword, "");

        mTxtViewUserName = findViewById(R.id.profile_txtView_userName);
        mTxtViewUserLastname = findViewById(R.id.profile_txtView_userLastName);
        mTxtViewUserEmail = findViewById(R.id.profile_txtView_userEmail);
        mTxtViewUserNumber = findViewById(R.id.profile_txtView_userNumber);

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
                       "mChangeName", R.id.changeNameBtn, "oC_btnChangeName");

        // ~~ Modal: Change Last Name
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileLastName", R.id.profileLastNameTitle, "",
                       "mEdtProfileLastName", R.id.profileLastNameEditText, "",
                       "mChangeLastName", R.id.changeLastNameBtn, "oC_btnChangeLastName");

        // ~~ Modal: Change Number
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileNumber", R.id.profileNumberTitle, "",
                       "mEdtProfileNumber", R.id.profileNumberEditText, "",
                       "mChangeNumber", R.id.changeNumberBtn, "oC_btnChangeNumber");

        // ~~ Modal: Change Email
        abst.connectVariableToViewIdAndOnClickMethod(
                "mTxtProfileEmail", R.id.profileEmailTitle, "",
                       "mEdtProfileEmail", R.id.profileEmailEditText, "",
                       "mChangeEmail", R.id.changeEmailBtn, "oC_btnChangeEmail");


        // ~~ Setting data from Current User

        autentication = ConfigurationFirebase.getFirebaseAuth();

        FirebaseUser firebaseUser = autentication.getCurrentUser();

        if(firebaseUser != null){

            mUserManager.getPerfil(new UserManager.OnUserComplete() {
                @Override
                public void onUserComplete(Users data) {
                    currentUser = data;
                    Log.e("MainRonanError", currentUser.getEmail());//Para pegar email
                    Log.e("MainRonanError", currentUser.getName());//Para pegar nome

                    mTxtViewUserName.setText(currentUser.getName());
                    mTxtViewUserLastname.setText(currentUser.getLastName());
                    mTxtViewUserEmail.setText(currentUser.getEmail());
                    mTxtViewUserNumber.setText(currentUser.getPhoneNumber());

                }
                @Override
                public void onErrorUserComplete(Users data) {
                    Log.v("MainRonanError", data.toString());
                }
            }, firebaseUser.getUid());

            mUsersNew.setId(firebaseUser.getUid());

            updateProfile();

        }

    }

    // ~ Click to Enter in Modal States
    private void oC_cardViewChangeName () {
        enterModalStateName();
    }

    private void oC_cardViewChangeLastName () { enterModalStateLastName(); }

    private void oC_cardViewChangeNumber () { enterModalStateNumber(); }

    private void oC_cardViewChangeEmail () { enterModalStateEmail(); }

    // ~ Enter in Modal States methods
    private void enterModalStateName () {
        mAnimate.crossFadeViews(mCardViewName, mModalOverlay);
        mAnimate.translateFromBottom(mModalChangeName, 200);
        mEdtProfileName.findFocus();
        mEdtProfileName.clearComposingText();
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

    // ~ Exit Modal States
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

    // ~ Change users props

    private void oC_btnChangeName () {
        String newName = mEdtProfileName.getText().toString();
        editUser(EDIT_NAME, newName);
        exitModalStateName();
    }

    private void oC_btnChangeLastName () {
        String newLastName = mEdtProfileLastName.getText().toString();
        editUser(EDIT_LASTNAME, newLastName);
        exitModalStateLastName();
    }
    private void oC_btnChangeNumber () {
        String newNumber = mEdtProfileNumber.getText().toString();
        editUser(EDIT_NUMBER, newNumber);
        exitModalStateNumber();
    }
    private void oC_btnChangeEmail () {
        String newEmail = mEdtProfileEmail.getText().toString();
        editUser(EDIT_EMAIL, newEmail);
        exitModalStateEmail();
    }

    private void editUser(int field, String value){

        // TODO: Discovers what field should change and then change by the new value

        switch (field){
            case EDIT_NAME:{

                mUsersNew.setName(value);
                mUsersNew.setLastName(currentUser.getLastName());
                mUsersNew.setEmail(currentUser.getEmail());
                mUsersNew.setPhoneNumber(currentUser.getPhoneNumber());

                break;
            }
            case EDIT_LASTNAME:{

                mUsersNew.setName(currentUser.getName());
                mUsersNew.setLastName(value);
                mUsersNew.setEmail(currentUser.getEmail());
                mUsersNew.setPhoneNumber(currentUser.getPhoneNumber());

                break;
            }
            case EDIT_EMAIL:{

                mUsersNew.setName(currentUser.getName());
                mUsersNew.setLastName(currentUser.getLastName());
                mUsersNew.setEmail(value);
                mUsersNew.setPhoneNumber(currentUser.getPhoneNumber());

                break;
            }
            case EDIT_NUMBER:{

                mUsersNew.setName(currentUser.getName());
                mUsersNew.setLastName(currentUser.getLastName());
                mUsersNew.setEmail(currentUser.getEmail());
                mUsersNew.setPhoneNumber(value);

                break;
            }

        }

        mUserManager.adicionarOuAtualizarPerfil(mUsersNew);

        currentUser = mUsersNew;

        updateProfile();
    }

    private void updateProfile(){
        mTxtProfileName.setText(currentUser.getName());
        mTxtProfileLastName.setText(currentUser.getLastName());
        mTxtProfileEmail.setText(currentUser.getEmail());
        mTxtProfileNumber.setText(currentUser.getPhoneNumber());

        mEdtProfileName.setText(currentUser.getName());
        mEdtProfileLastName.setText(currentUser.getLastName());
        mEdtProfileEmail.setText(currentUser.getEmail());
        mEdtProfileNumber.setText(currentUser.getPhoneNumber());

        mTxtViewUserName.setText(currentUser.getName());
        mTxtViewUserLastname.setText(currentUser.getLastName());
        mTxtViewUserEmail.setText(currentUser.getEmail());
        mTxtViewUserNumber.setText(currentUser.getPhoneNumber());
    }
}

package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.infnet.bikeride.bikeride.Tabbar.SignIn;
import com.infnet.bikeride.bikeride.Tabbar.SignInSocialMedia;
import com.infnet.bikeride.bikeride.Tabbar.SignUp;
import com.infnet.bikeride.bikeride.activityrequestbiker.RequestBikerActivity;
import com.infnet.bikeride.bikeride.services.Animations;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity{

    private GoogleSignInClient mGoogleSignInClient;

    // Modal

    private View mForgotPasswordModal;
    private View mForgotPasswordFrag;
    RelativeLayout mModalOverlay;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Button mQuickSignIn;
    private Button mQuickSignInDown;

    private Users users = new Users();
    private UserManager mUserManager = new UserManager();

    private FirebaseAuth autentication;

    // Animation
    private Animations mAnimate = new Animations(200);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Modal
        mForgotPasswordModal = findViewById(R.id.include_modal_forgotPassword);
        mForgotPasswordFrag = findViewById(R.id.include_frag_forgotPassword);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mQuickSignIn = findViewById(R.id.quickSignInButton);
        mQuickSignIn.setOnClickListener(QuickSignInEnter);

        mQuickSignInDown = findViewById(R.id.quickSignInButtonDown);
        mQuickSignInDown.setOnClickListener(QuickSignInExit);

        SignInButton mGoogleButton = findViewById(R.id.googleSignInButton);
        mGoogleButton.setOnClickListener(GoogleSignIn);

        TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso);

    }

        private View.OnClickListener GoogleSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 1);
        }
    };

    private View.OnClickListener QuickSignInEnter = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            mAnimate.crossFadeViews(mQuickSignIn, mQuickSignInDown);
            mAnimate.translateFromBottomIfInvisible(mForgotPasswordModal);
            mQuickSignIn.setVisibility(INVISIBLE);
            mQuickSignInDown.setVisibility(VISIBLE);
        }
    };

    private View.OnClickListener QuickSignInExit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            mAnimate.crossFadeViews(mQuickSignInDown, mQuickSignIn);
            mAnimate.translateToBottomIfVisible(mForgotPasswordModal);
            mQuickSignIn.setVisibility(VISIBLE);
            mQuickSignInDown.setVisibility(INVISIBLE);
        }
    };

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new SignIn();
                case 1:
                    return new SignUp();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

//    public void GoogleSignIn(View view){
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, 1);
//    }

    public void fechaModal(View view){
        mAnimate.translateToBottomIfVisible(mForgotPasswordFrag);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {

            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                users.setEmail(account.getEmail());
                users.setUrlPhoto(account.getPhotoUrl().toString());
                users.setName(account.getGivenName());
                users.setLastName(account.getFamilyName());

                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Handle error
            }

        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        autentication.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "login com google sucesso", Toast.LENGTH_SHORT).show();
                    FirebaseUser userLogad = autentication.getCurrentUser();
                    users.setId(userLogad.getUid());
                    mUserManager.adicionarOuAtualizarPerfil(users);
                    Redirect(RequestBikerActivity.class);
                } else {
                    Toast.makeText(getApplicationContext(), FirebaseAuthException.class.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void Redirect(Class destination){
        Intent newIntent = new Intent(getApplicationContext(), destination);
        startActivity(newIntent);
    }

}


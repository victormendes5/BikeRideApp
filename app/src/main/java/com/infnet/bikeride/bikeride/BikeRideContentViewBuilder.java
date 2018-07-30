package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class BikeRideContentViewBuilder {

    private static final int NAVIGATION_DRAWER_LAYOUT_FILE_ID = R.layout.main_drawer_layout;
    private static final int NAVIGATION_DRAWER_GROUPVIEW_ID = R.id.main_drawer_groupview;
    private static final int CUSTOM_TOOLBAR_ID = R.id.customToolbar;

    private AppCompatActivity mRefferedActivity;
    private int mActivityLayoutId;
    private DrawerLayout mDrawerLayout;

    private FirebaseAuth autentication;
    private GoogleSignInClient apiGoogle;


    public BikeRideContentViewBuilder(Context context, int activityLayoutId) {

        mRefferedActivity = (AppCompatActivity) context;
        mActivityLayoutId = activityLayoutId;

        mRefferedActivity.setContentView(NAVIGATION_DRAWER_LAYOUT_FILE_ID);

        mDrawerLayout = mRefferedActivity.findViewById(NAVIGATION_DRAWER_GROUPVIEW_ID);

        setActivityContent();
        initializeToolbar();
    }

    private void setActivityContent () {
        ViewGroup view = (ViewGroup) View.inflate(mRefferedActivity, mActivityLayoutId,null);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(0, 0);
        params.setMargins(0, 140, 0, 0);
        view.setLayoutParams(params);

        mDrawerLayout.addView(view, 0);
    }

    private void initializeToolbar () {

        Toolbar toolbar = mRefferedActivity.findViewById(CUSTOM_TOOLBAR_ID);
        mRefferedActivity.setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(mRefferedActivity,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        actionBarDrawerToggle.syncState();

        NavigationView navigationView = mRefferedActivity.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemID = item.getItemId();

                switch (itemID) {
                    case R.id.deliveryman_review:
                        navigate(DeliverymanReviewActivity.class);
                        break;
                    case R.id.delivery_tracking:
                        navigate(DeliveryTrackingActivity.class);
                        break;
                    case R.id.delivery_main:
                        navigate(DeliveryMainActivity.class);
                        break;
                    case R.id.delivery_quotation:
                        navigate(DeliveryQuotationPriceActivity.class);
                        break;
                    case R.id.logout_app:
                        autentication = ConfigurationFirebase.getFirebaseAuth();
                        Logout();
                        navigate(MainActivity.class);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    public void navigate (Class destination) {

        Intent newIntent = new Intent(mRefferedActivity, destination);
        mRefferedActivity.startActivity(newIntent);
    }

    public boolean isNavigationDrawerClosed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return false;
        }
        return true;
    }

    private void Logout(){


        autentication.signOut();

        if (LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut();

        } else {
            apiGoogle.signOut();
            apiGoogle.revokeAccess();
        }




    }
}

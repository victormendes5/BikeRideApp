package com.infnet.bikeride.bikeride.services;

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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.infnet.bikeride.bikeride.CardsActivity;
import com.infnet.bikeride.bikeride.ConfigurationFirebase;
import com.infnet.bikeride.bikeride.DeliveryQuotationPriceActivity;
import com.infnet.bikeride.bikeride.DeliverymanReviewActivity;
import com.infnet.bikeride.bikeride.MainActivity;
import com.infnet.bikeride.bikeride.ProfileActivity;
import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.activitydelivery.DeliveryActivity;
import com.infnet.bikeride.bikeride.activityrequestbiker.RequestBikerActivity;
import com.infnet.bikeride.bikeride.activityrequestuser.RequestUserActivity;
import com.infnet.bikeride.bikeride.constants.Constants;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.RequestModel;

public class ContentViewBuilder extends FirebaseAccess {

    private static final int NAVIGATION_DRAWER_LAYOUT_FILE_ID = R.layout.main_drawer_layout;
    private static final int NAVIGATION_DRAWER_GROUPVIEW_ID = R.id.main_drawer_groupview;
    private static final int CUSTOM_TOOLBAR_ID = R.id.customToolbar;

    private AppCompatActivity mRefferedActivity;
    private int mActivityLayoutId;
    private DrawerLayout mDrawerLayout;

    private FirebaseAuth autentication;
    private GoogleSignInClient apiGoogle;


    public ContentViewBuilder(Context context, int activityLayoutId) {

        mRefferedActivity = (AppCompatActivity) context;
        mActivityLayoutId = activityLayoutId;

        mRefferedActivity.setContentView(NAVIGATION_DRAWER_LAYOUT_FILE_ID);

        mDrawerLayout = mRefferedActivity.findViewById(NAVIGATION_DRAWER_GROUPVIEW_ID);

        setActivityContent();
        initializeToolbar();
    }

    private void setActivityContent () {
        ViewGroup view = (ViewGroup) View.inflate(mRefferedActivity, mActivityLayoutId,null);

        // ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(0, 0);
        // params.setMargins(0, 140, 0, 0);
        // view.setLayoutParams(params);

        mDrawerLayout.addView(view, 0);
    }

    private void initializeToolbar () {

        Toolbar toolbar = mRefferedActivity.findViewById(CUSTOM_TOOLBAR_ID);
        mRefferedActivity.setSupportActionBar(toolbar);
        mRefferedActivity.getSupportActionBar().setTitle("");

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(mRefferedActivity,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        actionBarDrawerToggle.syncState();

        // actionBarDrawerToggle.getDrawerArrowDrawable().setColor(0xFFFFFFFF);

        final NavigationView navigationView = mRefferedActivity.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int itemID = item.getItemId();

                        switch (itemID) {
                            case R.id.deliveryman_review:
                                navigate(DeliverymanReviewActivity.class);
                                break;
//                            case R.id.delivery_tracking:
//                                navigate(DeliveryActivity.class);
//                                break;
                            case R.id.delivery_main:
                                navigate(RequestUserActivity.class);
                                break;
//                            case R.id.delivery_quotation:
//                                navigate(DeliveryQuotationPriceActivity.class);
//                                break;
                            case R.id.profile:
                                navigate(ProfileActivity.class);
                                break;
                            case R.id.biker_activity:
                                navigate(RequestBikerActivity.class);
                                break;
//                            case R.id.delivery_activity:
//                                getDummyRequestContractAndNavigateToDeliveryActivity();
//                                break;
                            case R.id.payment_methods:
                                navigate(CardsActivity.class);
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

        } else if (apiGoogle.getInstanceId() != 0){
            apiGoogle.revokeAccess();
            apiGoogle.signOut();
        }
    }

    private void getDummyRequestContractAndNavigateToDeliveryActivity () {

        getObjectOrProperty(RequestModel.class,

                new FirebaseAccess.OnComplete<RequestModel>() {

                    @Override
                    public void onSuccess(RequestModel data) {

                        Intent newIntent = new Intent(mRefferedActivity, DeliveryActivity.class);

                        newIntent.putExtra("isBiker", "false");
                        newIntent.putExtra("requestData", data);

                        mRefferedActivity.startActivity(newIntent);

                    }

                    @Override
                    public void onFailure(RequestModel data) {

                    }

                }, Constants.ChildName.DELIVERIES, Constants.MockedIds.User);

    }
}
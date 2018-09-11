package com.infnet.bikeride.bikeride;

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

import com.infnet.bikeride.bikeride.Tabbar.SignIn;
import com.infnet.bikeride.bikeride.Tabbar.SignInSocialMedia;
import com.infnet.bikeride.bikeride.Tabbar.SignUp;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity{

    // Modal

    private View mForgotPasswordModal;
    private View mForgotPasswordFrag;
    RelativeLayout mModalOverlay;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Button mQuickSignIn;
    private Button mQuickSignInDown;

    // Animation
    private BRAnimations mAnimate = new BRAnimations(200);

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

        TabLayout tabLayout = findViewById(R.id.tabs);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

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

    public void fechaModal(View view){
        mAnimate.translateToBottomIfVisible(mForgotPasswordFrag);
    }

}


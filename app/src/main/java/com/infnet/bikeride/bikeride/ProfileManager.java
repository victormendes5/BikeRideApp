package com.infnet.bikeride.bikeride;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Imperiali on 21/07/18.
 */

public class ProfileManager{
    Profile profile;
    TextView mDrawerUserName;
    Activity activity;

    public ProfileManager (Activity context) {
        this.activity = context;
//        this.mDrawerUserName = context.findViewById(R.id.drawer_header_profileName);
    }

    public Profile user = new Profile();

    public void setName(String name) {
        this.user.name = name;
        this.mDrawerUserName.setText(name);
    }

    public String getName() {
        return user.name;
    }

    public void setLastName(String lastName) {
        this.user.lastName = Integer.toString(R.string.profile_txtView_userLastName);
    }

    public void setNumber(int number) {
        this.user.number = R.string.profile_txtView_userNumber;
    }

    public void setEmail(String email) {
        this.user.email = Integer.toString(R.string.profile_txtView_userEmail);
    }
}

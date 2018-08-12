package com.infnet.bikeride.bikeride.Tabbar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.infnet.bikeride.bikeride.R.layout.fragment_sign_up;

public class SignUp extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(fragment_sign_up, container, false);
    }

}
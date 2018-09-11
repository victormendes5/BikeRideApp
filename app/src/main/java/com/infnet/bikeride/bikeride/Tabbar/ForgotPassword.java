package com.infnet.bikeride.bikeride.Tabbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.infnet.bikeride.bikeride.BRAnimations;
import com.infnet.bikeride.bikeride.R;

public class ForgotPassword extends Fragment {

    private EditText mEmail;
    private Button mSendEmail;
    private Button mCloseModal;

    private View mForgotPasswordFrag;

    private BRAnimations mAnimate = new BRAnimations(200);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmail = getView().findViewById(R.id.forgotPasswordEditText);
        mSendEmail = getView().findViewById(R.id.sendEmailButton);
        mSendEmail.setOnClickListener(SendEmail);

        mForgotPasswordFrag = getActivity().findViewById(R.id.include_frag_forgotPassword);

    }

    private View.OnClickListener SendEmail = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Send Email
        }
    };

}

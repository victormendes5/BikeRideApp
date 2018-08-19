package com.infnet.bikeride.bikeride.Tabbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.infnet.bikeride.bikeride.R;

public class ForgotPassword extends Fragment {

    private EditText mEmail;
    private Button mSendEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmail = getActivity().findViewById(R.id.forgotPasswordEditText);
        mSendEmail = getActivity().findViewById(R.id.sendEmailButton);
        mSendEmail.setOnClickListener(SendEmail);
    }

    private View.OnClickListener SendEmail = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Send Email
        }
    };

}

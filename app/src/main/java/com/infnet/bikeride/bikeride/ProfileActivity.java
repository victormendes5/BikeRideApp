package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileImgView;
    CardView cardViewName;
    CardView cardViewNumber;
    CardView cardViewEmail;
    CardView cardViewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImgView = findViewById(R.id.profile_photo);
        cardViewName = findViewById(R.id.profile_cardView_edtName);
        cardViewNumber = findViewById(R.id.profile_cardView_edtNumber);
        cardViewEmail = findViewById(R.id.profile_cardView_edtEmail);
        cardViewPassword = findViewById(R.id.profile_cardView_edtPassword);
    }

    View.OnClickListener changePhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
}

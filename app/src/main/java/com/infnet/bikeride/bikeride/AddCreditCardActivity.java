package com.infnet.bikeride.bikeride;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AddCreditCardActivity extends AppCompatActivity {

    TextInputEditText mCardName;
    TextInputEditText mCardNumber;
    TextInputEditText mExpiration;
    TextInputEditText mCVC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credit_card);

        mCardName = findViewById(R.id.creditCardNameTextInputEditText);
        mCardNumber = findViewById(R.id.creditCardNumberTextInputEditText);
        mExpiration = findViewById(R.id.expirationDateTextInputEditText);
        mCVC = findViewById(R.id.cvcTextInputEditText);
    }

}

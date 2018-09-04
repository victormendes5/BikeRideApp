package com.infnet.bikeride.bikeride;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.infnet.bikeride.bikeride.models.CreditCardModel;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;
import com.infnet.bikeride.bikeride.services.CurrentUserData;

public class AddCreditCardActivity extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    // Inputs
    TextInputEditText mCardName;
    TextInputEditText mCardNumber;
    TextInputEditText mExpiration;
    TextInputEditText mCVC;

    //Buttons
    private Button btnAddCard;

    // Instances
    private CreditCardModel mCard;
    private CreditCardManager mCardManager =  new CreditCardManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_credit_card);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_add_credit_card);

        mCardName = findViewById(R.id.creditCardNameTextInputEditText);
        mCardNumber = findViewById(R.id.creditCardNumberTextInputEditText);
        mExpiration = findViewById(R.id.expirationDateTextInputEditText);
        mCVC = findViewById(R.id.cvcTextInputEditText);
        btnAddCard = (Button) findViewById(R.id.addCreditCardButton);

        btnAddCard.setOnClickListener(on_btnAddCard);

    }


    // Button Adicionar Cartão de Credito
    public View.OnClickListener on_btnAddCard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mCard = new CreditCardModel();

            mCard.setName(mCardName.getText().toString());
            mCard.setNumberCard(mCardNumber.getText().toString());
            mCard.setExpiration(mExpiration.getText().toString());
            mCard.setCvc(mCVC.getText().toString());
            mCard.setUserId(CurrentUserData.getId());

            AddCreditCard();
        }
    };


    // Adicionando o Cartão de Crédito no Firebase com key
    public void AddCreditCard (){

        if (mCard != null) {
            mCardManager.adicionarCards(mCard);

        }else {
                    Log.e("Cards", "Ta Errado");

        }


    }

}

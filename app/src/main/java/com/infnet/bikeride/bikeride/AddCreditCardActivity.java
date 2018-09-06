package com.infnet.bikeride.bikeride;

import android.content.Intent;
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

            if (!mCardName.getText().toString().equals("") ){

                if (!mCardNumber.getText().toString().equals("") && mCardNumber.getText().toString().length() != 16){

                    if (!mExpiration.getText().toString().equals("") && mExpiration.getText().toString().length() != 4){

                        if (!mCVC.getText().toString().equals("") && mCVC.getText().toString().length() != 3){

                            mCard = new CreditCardModel();

                            mCard.setName(mCardName.getText().toString());
                            mCard.setNumberCard(mCardNumber.getText().toString());
                            mCard.setExpiration(mExpiration.getText().toString());
                            mCard.setCvc(mCVC.getText().toString());
                            mCard.setUserId(CurrentUserData.getId());

                            AddCreditCard();

                        }else {
                            // Cvc vazio ou diferente de 3 dígitos
                            Toast.makeText(AddCreditCardActivity.this, "Por favor corrija o campo de cvc", Toast.LENGTH_SHORT).show();

                        }
                    }else {
                        // DAta de Espiração vazia ou diferente de 4 digitos
                        Toast.makeText(AddCreditCardActivity.this, "Por favor corrija o campo de expiração", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    // Numero Vazio / Diferente de 16 dígitos
                    Toast.makeText(AddCreditCardActivity.this, "Por favor corrija o campo de numeros do cartão.", Toast.LENGTH_SHORT).show();
                }
            }else{
                //Nome vazio
                Toast.makeText(AddCreditCardActivity.this, "Por favor digite um nome.", Toast.LENGTH_SHORT).show();

            }


        }
    };


    // Adicionando o Cartão de Crédito no Firebase com key
    public void AddCreditCard (){

        if (mCard != null) {
            mCardManager.adicionarCards(mCard);
            Redirect(CardsActivity.class);

        }else {
                    Log.e("Cards", "Ta Errado");

        }


    }

    // Redirect Page
    private void Redirect(Class destination){


        Intent newIntent = new Intent(AddCreditCardActivity.this, destination);
        startActivity(newIntent);

    }


    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }

}

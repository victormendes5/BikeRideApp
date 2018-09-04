package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.infnet.bikeride.bikeride.models.CreditCardModel;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;
import com.infnet.bikeride.bikeride.services.CurrentUserData;

import java.util.ArrayList;

public class CardsActivity extends AppCompatActivity {

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    private Button btnAddCards;
    private ListView mlistCards;

    // Instances
    private CreditCardModel mCard;
    private CreditCardManager mCardManager =  new CreditCardManager();

    private ArrayList<CreditCardModel>  listCre=  new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cards);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_cards);

        mlistCards = (ListView) findViewById(R.id.cards_listCards);
        btnAddCards = (Button) findViewById(R.id.cards_addCardButton);

        ListaDeCartões();

        btnAddCards.setOnClickListener(AddCardRedirect);

//        mlistCards.setAdapter();

    }

    //Login Google
    public View.OnClickListener AddCardRedirect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Redirect(AddCreditCardActivity.class);
        }
    };


    // Redirect Page

    private void Redirect(Class destination){


        Intent newIntent = new Intent(CardsActivity.this, destination);
        startActivity(newIntent);

    }

    private void ListaDeCartões(){


        mCardManager.getCards(new CreditCardManager.OnCardsComplete() {
            @Override
            public void OnCardsComplete(ArrayList<CreditCardModel> data) {
                listCre = data;
                Log.e("Array Cards", listCre.toString());
            }

            @Override
            public void OnErrorCardsComplete(ArrayList<CreditCardModel> data) {

            }
        },CurrentUserData.getId());




    }
}

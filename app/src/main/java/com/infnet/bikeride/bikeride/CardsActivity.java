package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
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

    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_cards);

        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_cards);

        ListaDeCartões();

        mlistCards = (ListView) findViewById(R.id.cards_listCards);
        adapter = new CardsAdapter(this, listCre);
        mCardManager =  new CreditCardManager(adapter, listCre);
        mlistCards.setAdapter(adapter);


        btnAddCards = (Button) findViewById(R.id.cards_addCardButton);
        btnAddCards.setOnClickListener(AddCardRedirect);

    }


    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }

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
                adapter = new CardsAdapter(getApplicationContext(), listCre);
                mlistCards.setAdapter(adapter);
            }

            @Override
            public void OnErrorCardsComplete(ArrayList<CreditCardModel> data) {

            }
        },CurrentUserData.getId());




    }
}

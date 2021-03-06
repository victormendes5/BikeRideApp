package com.infnet.bikeride.bikeride;

import android.util.Log;
import android.widget.BaseAdapter;

import com.google.firebase.database.FirebaseDatabase;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.CreditCardModel;

import java.util.ArrayList;
import java.util.List;

public class CreditCardManager {

    private FirebaseAccess mFirebase = new FirebaseAccess();
    private FirebaseDatabase mDatabase;

    private CreditCardModel mCreditCard;

    private ArrayList<CreditCardModel> arrayCradit = new ArrayList<>();

    private BaseAdapter adapter;
    private List<CreditCardModel> acoes;

    private static final String Cards_CHILD = "CredtidCards";

    public CreditCardManager(){

    }

    public CreditCardManager(BaseAdapter adapter, List<CreditCardModel> acoes){
        this();
        this.adapter = adapter;
        this.acoes = acoes;
    }

    //Interface de acesso ao getPerfil
    public interface OnCardsComplete {
        void OnCardsComplete(ArrayList<CreditCardModel> data);
        void OnErrorCardsComplete(ArrayList<CreditCardModel> data);
    }

    // Cria o usuário sempre que logar ou atualiza sempre que loga pois está sempre em mudança
    public void adicionarCards(final CreditCardModel card){

//        mFirebase.addOrUpdate(card,
//
//                new FirebaseAccess.OnCompleteVoid() {
//                    @Override
//                    public void onSuccess() {
//                        CreditCardModel currentProfile = card;
//                    }
//
//                    @Override
//                    public void onFailure() {
//                        Log.e("ERRO", "Deu ruim");
//                    }
//
//                }, Cards_CHILD, card.getUserId());



        mFirebase.addUsingKey(card,

                new FirebaseAccess.OnCompleteKey() {
            @Override
            public void onSuccess(String key) {
                CreditCardModel currentProfile = card;
            }

            @Override
            public void onFailure() {
                Log.e("ERRO", "Deu ruim");
            }
        },Cards_CHILD, card.getUserId());
    }


    // Função que Chama o firebaseAccess e faz o fetch direto na child
    public void getCards(final OnCardsComplete callback,String id) {

    mFirebase.setListenerToChild(CreditCardModel.class, new FirebaseAccess.ListenToChanges<ArrayList<CreditCardModel>>() {
        @Override
        public void onChange(ArrayList<CreditCardModel> data) {

            callback.OnCardsComplete(data);
//            for (int i = 0; i<data.size();i++){
//            }
        }

        @Override
        public void onError(ArrayList<CreditCardModel> data) {

        }
    },Cards_CHILD, id);

    }
}


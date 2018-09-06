package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.infnet.bikeride.bikeride.models.CreditCardModel;

import java.util.List;

public class CardsAdapter extends ArrayAdapter<CreditCardModel> {

    private Context contexto;

    public CardsAdapter(@NonNull Context context, List<CreditCardModel> acoes) {
        super(context, R.layout.list_credit_cards_item, acoes);
        this.contexto = context;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        CreditCardModel card = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(contexto).inflate(
                    R.layout.list_credit_cards_item, parent,false);
        }

        TextView txtNome = convertView.findViewById(R.id.creditCardList);
//        TextView txtEmail = convertView.findViewById(R.id.txtEmail);
//        TextView txtTelefone = convertView.findViewById(R.id.txtTelefone);
//        TextView txtCidade = convertView.findViewById(R.id.txtCidade);

        assert card != null;
        txtNome.setText(card.getName());
//        txtEmail.setText(acao.getEmail());
//        txtTelefone.setText(acao.getTelefone());
//        txtCidade.setText(acao.getCidade());

        return convertView;
    }
}

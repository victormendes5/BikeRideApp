package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RequestsListAdapter extends ArrayAdapter<RequestListDataModel> {

    private Context mContext;
    private List<RequestListDataModel> mList = new ArrayList<>();
    private OnItemSelected mCallback;

    public interface OnItemSelected {
        void onRequestChosen(String requesterId);
    }

    public RequestsListAdapter (@NonNull Context context,
                                ArrayList<RequestListDataModel> list,
                                OnItemSelected callback) {
        super(context, 0, list);

        mContext = context;
        mList = list;
        mCallback = callback;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.requests_list_item,parent,
                    false);

        final RequestListDataModel currentRequest = mList.get(position);

//        ImageView image = (ImageView)listItem.findViewById(R.id.imageView_poster);
//        image.setImageResource(currentMovie.getmImageDrawable());

        TextView description = (TextView) listItem.findViewById(R.id.requestsListItemItemDescriptionTxv);
        description.setText(currentRequest.getPackageTypeAndSize());

        TextView distance = (TextView) listItem.findViewById(R.id.requestsListItemDistanceTxv);
        distance.setText(currentRequest.getDistance());

        TextView fee = (TextView) listItem.findViewById(R.id.requestsListItemFeeTxv);
        fee.setText(currentRequest.getFee());

        Button btn = listItem.findViewById(R.id.requestsListItemGetBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onRequestChosen(currentRequest.getRequesterId());
            }
        });

        return listItem;
    }



}

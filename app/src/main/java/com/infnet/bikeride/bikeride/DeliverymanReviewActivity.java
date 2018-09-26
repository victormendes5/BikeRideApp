package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.infnet.bikeride.bikeride.dao.FirebaseAccess;
import com.infnet.bikeride.bikeride.models.RequestModel;
import com.infnet.bikeride.bikeride.models.ReviewDeliveryModel;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;
import com.infnet.bikeride.bikeride.services.CurrentUserData;

public class DeliverymanReviewActivity extends AppCompatActivity {

    private RatingBar mReviewStars;
    private EditText mReviewComment;
    private Button mSubmitReview;

    private float starsResult;
    private String commentResult;

    // Firebase
    private FirebaseAccess mFirebase = new FirebaseAccess();
    // Modelo
    private ReviewDeliveryModel mreviewDeliveryModel = new ReviewDeliveryModel();

    // String com a key
    private String mkeyHistory  = "";

    // Constant com nome da child
    private static final String Cards_CHILD = "History";


    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_review_deliveryman);
        mContentViewBuilder = new ContentViewBuilder(this,
                R.layout.activity_review_deliveryman);

        mReviewStars = findViewById(R.id.myRating);
        mReviewComment = findViewById(R.id.commentInput);
        mSubmitReview = findViewById(R.id.btnSendAvaliation);

        mReviewStars.setOnRatingBarChangeListener(starsAvaliation);
        mSubmitReview.setOnClickListener(submitReview);

        getBundledData ();

    }

    private RatingBar.OnRatingBarChangeListener starsAvaliation = new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            starsResult = rating;
        }
    };

    private View.OnClickListener submitReview = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            commentResult = mReviewComment.getText().toString();
            Toast.makeText(DeliverymanReviewActivity.this, "Avaliado em: " + starsResult, Toast.LENGTH_SHORT).show();

            mreviewDeliveryModel.setComment(commentResult);
            mreviewDeliveryModel.setNota(starsResult);

            if (!mreviewDeliveryModel.getComment().toString().equals("") && mreviewDeliveryModel.getNota() != 0.00f){

                AddReview();

            } else {
                Toast.makeText(DeliverymanReviewActivity.this, "Faça a avaliação corretamente ", Toast.LENGTH_SHORT).show();

            }



        }
    };

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }

    private void getBundledData () {

        Bundle extras = getIntent().getExtras();

        if (extras != null) {

            String isBiker = extras.getString("isBiker");

            if (isBiker.equals("true")) {

                mkeyHistory = extras.getString("bikerContractKey");

                Log.v("KeyHash",mkeyHistory);
            }

            else if (isBiker.equals("false")) {

                mkeyHistory = extras.getString("requesterContractKey");

                Log.v("KeyHash",mkeyHistory);

            }

        }
    }


    public void AddReview(){

                mFirebase.addOrUpdate(mreviewDeliveryModel,

                new FirebaseAccess.OnCompleteVoid() {
                    @Override
                    public void onSuccess() {
                        ReviewDeliveryModel currentProfile = mreviewDeliveryModel;
                    }

                    @Override
                    public void onFailure() {
                        Log.e("ERRO", "Deu ruim");
                    }

                }, Cards_CHILD, CurrentUserData.getId(),mkeyHistory);
    }
}

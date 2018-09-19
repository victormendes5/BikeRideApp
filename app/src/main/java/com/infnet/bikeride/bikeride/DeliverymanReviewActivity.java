package com.infnet.bikeride.bikeride;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

public class DeliverymanReviewActivity extends AppCompatActivity {

    private RatingBar mReviewStars;
    private EditText mReviewComment;
    private Button mSubmitReview;

    private float starsResult;
    private String commentResult;

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


            // TODO: Receber o usuário, passar {
            // TODO: commentResult como bikerReviewComment
            // TODO: starsResult como bikerReviewRating
            // TODO: }
            // TODO: para =>
            // TODO: history
        }
    };

    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }
}

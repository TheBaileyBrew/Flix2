package com.thebaileybrew.flix2.loaders;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.flix2.BuildConfig;
import com.thebaileybrew.flix2.interfaces.adapters.ReviewAdapter;
import com.thebaileybrew.flix2.models.Review;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.jsonUtils;

import java.net.URL;
import java.util.List;

public class ReviewLoader extends AsyncTask<String, Void, List<Review>> {
    private static final String TAG = ReviewLoader.class.getSimpleName();

    private final ReviewAdapter reviewAdapter;

    public ReviewLoader(ReviewAdapter reviewAdapter) {this.reviewAdapter = reviewAdapter;}

    @Override
    protected List<Review> doInBackground(String... strings) {
        if (strings.length <1 || strings[0] == null) {
            return null;
        }
        String movieID = strings[0];
        URL reviewRequestUrl = UrlUtils.buildReviewUrl(BuildConfig.API_KEY, movieID);
        try {
            String jsonReviewResponse = jsonUtils.requestHttpsMovieReviews(reviewRequestUrl);
            return jsonUtils.extractReviewDetails(jsonReviewResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: can't request reviews", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        if (reviews != null) {
            reviewAdapter.setReviewCollection(reviews);
        }
        super.onPostExecute(reviews);
    }
}

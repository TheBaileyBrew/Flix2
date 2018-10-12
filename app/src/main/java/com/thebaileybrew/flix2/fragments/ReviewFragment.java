package com.thebaileybrew.flix2.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thebaileybrew.flix2.DetailsActivity;
import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.interfaces.adapters.ReviewAdapter;
import com.thebaileybrew.flix2.loaders.ReviewLoader;
import com.thebaileybrew.flix2.loaders.SingleMovieLoader;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.models.Review;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewFragment extends Fragment {

    private final static String TAG = ReviewFragment.class.getSimpleName();
    private RecyclerView reviewRecycler;
    private Movie movie;
    private List<Review> reviews = new ArrayList<>();

    public ReviewFragment newInstance () {
        return new ReviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.reviews_layout, container, false);
        movie = DetailsActivity.getSelected();
        Log.e(TAG, "onCreateView: movie name is: " + movie.getMovieTitle() );
        Log.e(TAG, "onCreateView: movie overview is: " + movie.getMovieOverview() );
        //DO STUFF HERE
        reviewRecycler = rootView.findViewById(R.id.review_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FlixApplication.getContext());
        reviewRecycler.setLayoutManager(linearLayoutManager);
        ReviewAdapter reviewAdapter = new ReviewAdapter(FlixApplication.getContext(), reviews, new ReviewAdapter.ReviewClickHandler() {
            @Override
            public void onClick(View view, Review review) {
                String reviewLink = review.getReviewLink();
                Intent reviewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(reviewLink));
                if (reviewIntent.resolveActivity(FlixApplication.getContext().getPackageManager()) != null) {
                    startActivity(reviewIntent);
                }
            }
        });
        reviewRecycler.setAdapter(reviewAdapter);
        ReviewLoader reviewLoader = new ReviewLoader(reviewAdapter);
        reviewLoader.execute(String.valueOf(movie.getMovieID()));
        reviewAdapter.notifyDataSetChanged();

        return rootView;
    }

}

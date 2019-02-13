package com.thebaileybrew.flix2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thebaileybrew.flix2.DetailsActivity;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.loaders.SingleMovieLoader;
import com.thebaileybrew.flix2.models.Movie;

import androidx.fragment.app.Fragment;

public class OverviewFragment extends Fragment {

    private final static String TAG = OverviewFragment.class.getSimpleName();
    private final static String MOVIE_KEY = "parcel_movie";

    private TextView overviewTextView;
    private TextView genresTextView;
    private TextView taglineTextView;
    private Movie movie;

    public OverviewFragment newInstance () {
        return new OverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.overview_layout, container, false);
        movie = DetailsActivity.getSelected();
        Log.e(TAG, "onCreateView: movie name is: " + movie.getMovieTitle() );
        Log.e(TAG, "onCreateView: movie overview is: " + movie.getMovieOverview() );
        //DO STUFF HERE
        overviewTextView = rootView.findViewById(R.id.synopsis_text);
        genresTextView = rootView.findViewById(R.id.movie_genres);
        taglineTextView = rootView.findViewById(R.id.movie_tagline);
        SingleMovieLoader singleMovieLoader = new SingleMovieLoader(taglineTextView, genresTextView);
        singleMovieLoader.execute(String.valueOf(movie.getMovieID()));
        overviewTextView.setText(movie.getMovieOverview());

        return rootView;
    }

}

package com.thebaileybrew.flix2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thebaileybrew.flix2.DetailsActivity;
import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.interfaces.adapters.CreditsAdapter;
import com.thebaileybrew.flix2.loaders.CreditsLoader;
import com.thebaileybrew.flix2.loaders.SingleMovieLoader;
import com.thebaileybrew.flix2.models.Credit;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.displayMetricsUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CreditsFragment extends Fragment {

    private final static String TAG = CreditsFragment.class.getSimpleName();
    private RecyclerView creditRecycler;
    private static Movie movie;
    private List<Credit> credits = new ArrayList<>();
    private boolean landscapeMode;
    private int columnCount;


    public CreditsFragment newInstance () {
        return new CreditsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.credits_layout, container, false);
        landscapeMode = getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels;
        movie = DetailsActivity.getSelected();
        Log.e(TAG, "onCreateView: movie name is: " + movie.getMovieTitle() );
        Log.e(TAG, "onCreateView: movie overview is: " + movie.getMovieOverview() );
        //DO STUFF HERE
        creditRecycler = rootView.findViewById(R.id.credit_recycler);
        if (landscapeMode) {
            columnCount = 4;
        } else {
            columnCount = 2;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(FlixApplication.getContext(), columnCount);
        creditRecycler.setLayoutManager(gridLayoutManager);
        CreditsAdapter creditsAdapter = new CreditsAdapter(FlixApplication.getContext(),credits, creditRecycler);
        creditRecycler.setAdapter(creditsAdapter);
        CreditsLoader creditsLoader = new CreditsLoader(creditsAdapter);
        creditsLoader.execute(String.valueOf(movie.getMovieID()));


        return rootView;
    }

}

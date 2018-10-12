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
import com.thebaileybrew.flix2.interfaces.adapters.VideosAdapter;
import com.thebaileybrew.flix2.loaders.SingleMovieLoader;
import com.thebaileybrew.flix2.loaders.VideosLoader;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.models.Videos;
import com.thebaileybrew.flix2.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VideosFragment extends Fragment {

    private final static String TAG = VideosFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private Movie movie;
    private List<Videos> videos = new ArrayList<>();

    public VideosFragment newInstance () {
        return new VideosFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.videos_layout, container, false);
        movie = DetailsActivity.getSelected();
        Log.e(TAG, "onCreateView: movie name is: " + movie.getMovieTitle() );
        Log.e(TAG, "onCreateView: movie overview is: " + movie.getMovieOverview() );
        //DO STUFF HERE
        recyclerView = rootView.findViewById(R.id.videos_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FlixApplication.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        VideosAdapter videosAdapter = new VideosAdapter(FlixApplication.getContext(), videos, new VideosAdapter.VideoClickHandler() {
            @Override
            public void onClick(View view, Videos video) {
                //Open Intent to view the video
                String trailerUrl = UrlUtils.buildYoutubeTrailerUrl(video.getVideoKey());

                Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                if(videoIntent.resolveActivity(FlixApplication.getContext().getPackageManager()) != null) {
                    startActivity(videoIntent);
                }
            }
        });
        recyclerView.setAdapter(videosAdapter);
        VideosLoader videosLoader = new VideosLoader(videosAdapter);
        videosLoader.execute(String.valueOf(movie.getMovieID()));
        videosAdapter.notifyDataSetChanged();
        return rootView;
    }

}

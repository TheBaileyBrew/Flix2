package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.RecyclerDiffCallback;
import com.thebaileybrew.flix2.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<Movie> movieCollection;

    final private MovieAdapterClickHandler adapterClickHandler;

    public interface MovieAdapterClickHandler {
        void onClick(View view, Movie movie);
    }

    //Create the recycler
    public MovieAdapter(Context context, List<Movie> movieCollection, MovieAdapterClickHandler clicker) {
        this.layoutInflater = LayoutInflater.from(context);
        this.movieCollection = movieCollection;
        this.adapterClickHandler = clicker;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.movie_card_view, parent, false);
        return new ViewHolder(view);
    }

    //Bind the Arraydata to the layoutview
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Movie currentMovie = movieCollection.get(position);
        String moviePosterPath = UrlUtils.buildPosterPathUrl(currentMovie.getMoviePosterPath());

        Picasso.get()
                .load(moviePosterPath)
                .placeholder(R.drawable.flix_logo)
                .into(holder.moviePoster);
    }

    @Override
    public int getItemCount() {
        if (movieCollection == null) {
            return 0;
        } else {
            return movieCollection.size();
        }
    }

    public void updateMovieList(List<Movie> movies) {
        final RecyclerDiffCallback diffCallback = new RecyclerDiffCallback(this.movieCollection, movies);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        if (this.movieCollection != null) {
            this.movieCollection.clear();
            this.movieCollection.addAll(movies);
        }

        if (movies == null) {
            assert this.movieCollection != null;
            this.movieCollection.clear();
            notifyDataSetChanged();
        }
        diffResult.dispatchUpdatesTo(this);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView moviePoster;
        final ImageView hiddenViewStar;

        private ViewHolder(View newView) {
            super(newView);
            moviePoster = newView.findViewById(R.id.movie_cardview_poster);
            hiddenViewStar = newView.findViewById(R.id.hidden_star);
            newView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie currentMovie = movieCollection.get(getAdapterPosition());
            adapterClickHandler.onClick(v, currentMovie);
        }

    }
}

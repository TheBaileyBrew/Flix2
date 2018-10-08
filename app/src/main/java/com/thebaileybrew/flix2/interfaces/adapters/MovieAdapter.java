package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

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

        holder.movieRatingView.setMax(10);
        holder.movieRatingView.setSuffixText(" ");
        float value = (float) (currentMovie.getMovieVoteAverage());
        holder.movieRatingView.setProgress(value);
        float currentProgress = holder.movieRatingView.getProgress();
        holder.updateMovieRating(currentProgress);
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final StaticProgressBar movieRatingView;
        final ImageView moviePoster;

        private ViewHolder(View newView) {
            super(newView);
            movieRatingView = newView.findViewById(R.id.movie_rating_view);
            moviePoster = newView.findViewById(R.id.movie_cardview_poster);
            newView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie currentMovie = movieCollection.get(getAdapterPosition());
            adapterClickHandler.onClick(v, currentMovie);
        }

        void updateMovieRating(double currentProgress) {
            if(currentProgress >= 7.00) {
                movieRatingView.setTextColor(Color.parseColor("#ffffff"));
                movieRatingView.setFinishedStrokeColor(Color.parseColor("#25cc00"));
                movieRatingView.setUnfinishedStrokeColor(Color.parseColor("#b8ffc3"));
                movieRatingView.setBackgroundResource(R.drawable.good_rounded_edge);
                if (currentProgress >= 8.00) {
                    movieRatingView.setBottomText("GREAT");
                } else if (currentProgress >= 9.00) {
                    movieRatingView.setBottomText("BEST");
                } else {
                    movieRatingView.setBottomText("GOOD");
                }
            } else if (currentProgress >= 4.25) {
                movieRatingView.setTextColor(Color.parseColor("#ffffff"));
                movieRatingView.setFinishedStrokeColor(Color.parseColor("#f5c400"));
                movieRatingView.setUnfinishedStrokeColor(Color.parseColor("#ffe7ab"));
                movieRatingView.setBackgroundResource(R.drawable.mid_rounded_edge);
                if (currentProgress >= 6.00) {
                    movieRatingView.setBottomText("AVERAGE");
                } else if (currentProgress >=4.75) {
                    movieRatingView.setBottomText("OKAY");
                } else {
                    movieRatingView.setBottomText("MEH..");
                }
            } else {
                movieRatingView.setTextColor(Color.parseColor("#ffffff"));
                movieRatingView.setFinishedStrokeColor(Color.parseColor("#dc0202"));
                movieRatingView.setUnfinishedStrokeColor(Color.parseColor("#ffa1a1"));
                movieRatingView.setBackgroundResource(R.drawable.bad_rounded_edge);
                if (currentProgress >= 3.5) {
                    movieRatingView.setBottomText("MEH");
                } else if (currentProgress >= 2.00) {
                    movieRatingView.setBottomText("AVOID");
                } else if (currentProgress == 0.00) {
                    movieRatingView.setBottomText("NO SCORE");
                } else {
                    movieRatingView.setBottomText("HARD PASS");
                }
            }

        }
    }
}

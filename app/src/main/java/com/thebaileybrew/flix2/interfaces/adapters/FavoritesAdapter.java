package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.UrlUtils;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<Movie> favoriteMovies;

    final private MovieAdapter.MovieAdapterClickHandler adapterClickHandler;

    public FavoritesAdapter(Context context, List<Movie> favoriteMovies,
                            MovieAdapter.MovieAdapterClickHandler adapterClickHandler) {
        this.layoutInflater = LayoutInflater.from(context);
        this.favoriteMovies = favoriteMovies;
        this.adapterClickHandler = adapterClickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.favorites_card_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Movie currentMovie = favoriteMovies.get(position);
        String posterPath = UrlUtils.buildPosterPathUrl(currentMovie.getMoviePosterPath());

        Picasso.get()
                .load(posterPath)
                .placeholder(R.drawable.flix_logo)
                .into(holder.moviePoster);
    }

    @Override
    public int getItemCount() {
        if (favoriteMovies == null) {
            return 0;
        } else {
            return favoriteMovies.size();
        }
    }

    public void setFavorites(List<Movie> movieFavorites) {
        favoriteMovies = movieFavorites;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView moviePoster;
        final ImageView hiddenStar;

        private ViewHolder(View favView) {
            super(favView);
            moviePoster = favView.findViewById(R.id.movie_cardview_favorite);
            hiddenStar = favView.findViewById(R.id.hidden_star_favorite);
            favView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Movie currentMovie = favoriteMovies.get(getAdapterPosition());
            adapterClickHandler.onClick(v, currentMovie);
        }

    }
}

package com.thebaileybrew.flix2.utils;

import com.thebaileybrew.flix2.models.Movie;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

public class RecyclerDiffCallback extends DiffUtil.Callback {

    private final List<Movie> mOriginalMovies;
    private final List<Movie> mNewMovies;

    public RecyclerDiffCallback(List<Movie> originalMovies, List<Movie> newMovies) {
        this.mOriginalMovies = originalMovies;
        this.mNewMovies = newMovies;
    }

    @Override
    public int getOldListSize() {
        return mOriginalMovies == null ? 0 : mOriginalMovies.size();
    }

    @Override
    public int getNewListSize() {
        return mNewMovies == null ? 0 : mNewMovies.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOriginalMovies.get(oldItemPosition).getMovieID()
                == mNewMovies.get(newItemPosition).getMovieID();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Movie oldMovie = mOriginalMovies.get(oldItemPosition);
        final Movie newMovie = mNewMovies.get(newItemPosition);
        return oldMovie.getMovieFavorite() == newMovie.getMovieFavorite();
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }

}

package com.thebaileybrew.flix2.database;

import android.app.Application;

import com.thebaileybrew.flix2.models.Movie;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class FavoritesViewModel extends AndroidViewModel {

    private MovieRepository movieRepository;
    private LiveData<List<Movie>> mAllMovies;

    private MutableLiveData<List<Movie>> mAllFavorites;

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        movieRepository = new MovieRepository(application);
        mAllMovies = movieRepository.getFavorites();
    }

    public LiveData<List<Movie>> getFavorites() {
        return mAllMovies;
    }

}

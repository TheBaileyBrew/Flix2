package com.thebaileybrew.flix2.database.viewmodels;

import android.app.Application;

import com.thebaileybrew.flix2.models.Movie;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MainViewModel extends AndroidViewModel {

    private MovieRepository mRepository;

    private LiveData<List<Movie>> movies;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new MovieRepository();

    }

    public LiveData<List<Movie>> getMovies() {
        movies = mRepository.getAllMovies();
        return movies;
    }

    public LiveData<List<Movie>> getPopularMovies() {
        movies = mRepository.getPopularMovies();
        return movies;
    }

    public LiveData<List<Movie>> getTopRatedMovies() {
        movies = mRepository.getRatedMovies();
        return movies;
    }

    public List<Movie> getFavoriteMovies() {
        return mRepository.getFavoriteMovies();
    }

    public List<Movie> getWatchListMovies() {
        return mRepository.getWatchListMovies();
    }

    public void update(Movie movie, int favorite) {
        mRepository.updateMovie(movie, favorite);
    }

}

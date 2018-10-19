package com.thebaileybrew.flix2.database.viewmodels;

import android.app.Application;

import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.database.DatabaseClient;
import com.thebaileybrew.flix2.database.MovieRepository;
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
        movies = mRepository.getAllMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    public void update(Movie movie, int favorite) {
        mRepository.updateMovie(movie, favorite);
    }

}

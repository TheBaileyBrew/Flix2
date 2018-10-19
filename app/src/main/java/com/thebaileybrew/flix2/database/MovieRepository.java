package com.thebaileybrew.flix2.database;

import android.app.Application;
import android.os.AsyncTask;

import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.models.Movie;

import java.util.List;

import androidx.lifecycle.LiveData;

public class MovieRepository {

    private MovieDao movieDao;
    private static int movieFavorite;
    private LiveData<List<Movie>> mAllMovies;

    public MovieRepository() {
        AppDatabase aDb = DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase();
        movieDao = aDb.movieDao();
        mAllMovies = movieDao.loadMovies();

    }


    public LiveData<List<Movie>> getAllMovies() {
        return mAllMovies;
    }

    public void insertMovie (Movie movie) {
        new insertAsyncTask(movieDao).execute(movie);
    }

    public void updateMovie (Movie movie, int favorite) {
        movieFavorite = favorite;
        new updateAsyncTask(movieDao).execute(movie);
    }

    private static class insertAsyncTask extends AsyncTask<Movie, Void, Void> {

        private MovieDao mInsertMovieDao;

        insertAsyncTask(MovieDao dao) {
            mInsertMovieDao = dao;
        }

        @Override
        protected Void doInBackground(final Movie... params) {
            mInsertMovieDao.insertMovie(params[0]);
            return null;
        }
    }

    private static class updateAsyncTask extends AsyncTask<Movie, Void, Void> {

        private MovieDao mUpdateMovieDao;

        updateAsyncTask(MovieDao dao) {
            mUpdateMovieDao = dao;
        }

        @Override
        protected Void doInBackground(final Movie ... params) {
            Movie movie = params[0];
            movie.setMovieFavorite(movieFavorite);
            mUpdateMovieDao.updateMovie(movie);
            return null;
        }
    }




}

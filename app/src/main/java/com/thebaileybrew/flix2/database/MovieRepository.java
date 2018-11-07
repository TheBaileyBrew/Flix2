package com.thebaileybrew.flix2.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.flix2.models.Movie;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.lifecycle.LiveData;

public class MovieRepository {
    private static final String TAG = MovieRepository.class.getSimpleName();

    private MovieDao mMovieDao;
    private LiveData<List<Movie>> mFavorites;

    public MovieRepository(Application application) {
        MovieDatabase db = MovieDatabase.getDatabase(application);
        mMovieDao = db.movieDao();
    }

    public LiveData<List<Movie>> getFavorites() {
        mFavorites = mMovieDao.getFavorites();
        return mFavorites;
    }

    public Movie getSingleFilm(int id) {
        try {
            return new checkForDatabaseRecordAsyncTask(mMovieDao).execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            Log.e(TAG, "getSingleFilm: ", e);;
            return null;
        }
    }

    public void insertFavorite(Movie movie) {
        new populateDatabaseFavoriteAsyncTask(mMovieDao).execute(movie);
    }

    public void removeFavorite(Movie movie) {
        new removeDatabaseFavoriteAsyncTask(mMovieDao).execute(movie);
    }


    //Add to Favorite
    private static class populateDatabaseFavoriteAsyncTask extends AsyncTask<Movie, Void, Void> {
        private MovieDao mMovieDao;

        populateDatabaseFavoriteAsyncTask(MovieDao mMovieDao) {
            this.mMovieDao = mMovieDao;
        }


        @Override
        protected Void doInBackground(Movie... movies) {
            Movie currentMovie = movies[0];
            mMovieDao.insertMovie(currentMovie);
            return null;
        }
    }

    //Remove Favorite
    private static class removeDatabaseFavoriteAsyncTask extends AsyncTask<Movie, Void, Void> {
        private MovieDao mMovieDao;

        removeDatabaseFavoriteAsyncTask(MovieDao mMovieDao) {
            this.mMovieDao = mMovieDao;
        }


        @Override
        protected Void doInBackground(Movie... movies) {
            Movie currentMovie = movies[0];
            Log.e(TAG, "doInBackground: movie to delete is: " + currentMovie.getMovieTitle() );
            mMovieDao.deleteMovie(currentMovie);
            Log.e(TAG, "doInBackground: movie deleted");
            return null;
        }
    }

    private static class checkForDatabaseRecordAsyncTask extends AsyncTask<Integer, Void, Movie> {
        private MovieDao mMovieDao;

        checkForDatabaseRecordAsyncTask(MovieDao movieDao) {
            this.mMovieDao = movieDao;
        }

        @Override
        protected Movie doInBackground(Integer... ints) {
            int currentInt = ints[0];
            mMovieDao.getMovieDetails(currentInt);
            return mMovieDao.getMovieDetails(currentInt);
        }
    }

}

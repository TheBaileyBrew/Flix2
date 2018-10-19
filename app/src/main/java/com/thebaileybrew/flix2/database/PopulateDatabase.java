package com.thebaileybrew.flix2.database;

import android.os.AsyncTask;

import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.models.Movie;

public class PopulateDatabase extends AsyncTask<Void, Void, Void> {

    private final MovieDao movieDao;
    private final Movie movie;

    public PopulateDatabase(Movie movie) {
        movieDao = DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase().movieDao();
        this.movie = movie;
    }

    @Override
    protected Void doInBackground(final Void... params) {
        movieDao.insertMovie(movie);

        return null;
    }
}



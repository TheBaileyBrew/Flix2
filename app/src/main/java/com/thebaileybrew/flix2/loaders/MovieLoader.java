package com.thebaileybrew.flix2.loaders;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.flix2.BuildConfig;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.jsonUtils;
import com.thebaileybrew.flix2.interfaces.adapters.MovieAdapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MovieLoader extends AsyncTask<String, Void, List<Movie>> {
    private static final String TAG = MovieLoader.class.getSimpleName();

    private final MovieAdapter mMovieAdapter;

    public MovieLoader(MovieAdapter movieAdapter) {
        mMovieAdapter = movieAdapter;
    }


    @Override
    protected List<Movie> doInBackground(String... strings) {
        if (strings.length < 3 || strings[0] == null) {
            return null;
        }
        String sortingOrder = strings[0];
        String languageFilter = strings[1];
        String filterYear = strings[2];
        String searchQuery = strings[3];


        URL moviesRequestUrl = UrlUtils.buildMovieUrl(
                    BuildConfig.API_KEY,
                languageFilter,
                    sortingOrder,
                    filterYear,
                    searchQuery);
        try {
            String jsonMoviesResponse = jsonUtils.makeHttpsRequest(moviesRequestUrl);

            return jsonUtils.extractMoviesFromJson(jsonMoviesResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: can't make http req", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if(movies != null) {
            mMovieAdapter.setMovieCollection(movies);
        }

        super.onPostExecute(movies);

    }
}

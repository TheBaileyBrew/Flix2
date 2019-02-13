package com.thebaileybrew.flix2.loaders;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.flix2.BuildConfig;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.jsonUtils;

import java.net.URL;
import java.util.List;

public class MovieLoader extends AsyncTask<String, Void, List<Movie>> {
    private static final String TAG = MovieLoader.class.getSimpleName();

    public MovieLoader() {
    }

    @Override
    protected List<Movie> doInBackground(String... strings) {
        if (strings.length < 3 || strings[0] == null) {
            return null;
        }
        String sortingOrder = strings[0];
        Log.e(TAG, "doInBackground: sortOrder" + sortingOrder );
        if (sortingOrder.equals("favorite")) {
            //TODO: Get films that have a DB value of FAVORITE

        } else if (sortingOrder.equals("watchlist")) {
            //TODO: Get films that have a DB value of WATCHLIST

        }
        String languageFilter = strings[1];
        Log.e(TAG, "doInBackground: language" + languageFilter);
        String filterYear = strings[2];
        Log.e(TAG, "doInBackground: year" + filterYear);
        String searchQuery = strings[3];
        Log.e(TAG, "doInBackground: searching" + searchQuery);
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
        super.onPostExecute(movies);

    }
}

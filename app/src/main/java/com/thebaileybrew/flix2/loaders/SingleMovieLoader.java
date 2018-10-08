package com.thebaileybrew.flix2.loaders;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.flix2.BuildConfig;
import com.thebaileybrew.flix2.models.Film;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.jsonUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SingleMovieLoader extends AsyncTask<String, Void, List<Film>> {
    private static final String TAG = SingleMovieLoader.class.getSimpleName();

    public SingleMovieLoader() {
    }

    @Override
    protected List<Film> doInBackground(String... params){
        if (params.length < 1 || params[0] == null) {
            return null;
        }
        String movieID = params[0];

        URL singleFilmRequest = UrlUtils.buildSingleMovieUrl(BuildConfig.API_KEY,movieID);
        try {
            String jsonFilmResponse = jsonUtils.requestHttpsSingleFilm(singleFilmRequest);

            return jsonUtils.extractSingleFilmData(jsonFilmResponse, movieID);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: can't make http single req", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Film> films) {

        super.onPostExecute(films);
    }
}

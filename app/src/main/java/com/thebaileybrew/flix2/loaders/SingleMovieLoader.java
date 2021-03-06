package com.thebaileybrew.flix2.loaders;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.thebaileybrew.flix2.BuildConfig;
import com.thebaileybrew.flix2.R;
import com.thebaileybrew.flix2.models.Film;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.jsonUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SingleMovieLoader extends AsyncTask<String, Void, List<Film>> {
    private static final String TAG = SingleMovieLoader.class.getSimpleName();

    private final TextView movieTag;
    private final TextView movieGenre;

    public SingleMovieLoader (TextView movieTag, TextView movieGenre) {
        this.movieTag = movieTag;
        this.movieGenre = movieGenre;
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

            return jsonUtils.extractSingleFilmData(jsonFilmResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: can't make http single req", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Film> films) {
        if (films != null) {
            Film currentFilm = films.get(0);
            movieTag.setText(currentFilm.getMovieTagLine());
            movieGenre.setText(currentFilm.getMovieGenre());
        }

        super.onPostExecute(films);
    }

}

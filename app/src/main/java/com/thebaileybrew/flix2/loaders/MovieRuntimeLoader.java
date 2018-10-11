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
import java.util.List;
import java.util.Locale;

public class MovieRuntimeLoader extends AsyncTask<String, Void, List<Film>> {
    private static final String TAG = MovieRuntimeLoader.class.getSimpleName();
    private final static String TIME_FORMAT = "%02d:%02d";

    private final TextView movieTime;

    public MovieRuntimeLoader(TextView movieTime) {
        this.movieTime = movieTime;
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
            if (currentFilm.getMovieRuntime() == 0) {
                movieTime.setText(R.string.unknown_time);
            } else {
                movieTime.setText(convertTime(currentFilm.getMovieRuntime()));
            }

        }

        super.onPostExecute(films);
    }

    private String convertTime(int runTime) {
        int hours = runTime / 60;
        int minutes = runTime % 60;
        return String.format(Locale.US, TIME_FORMAT, hours, minutes);
    }
}

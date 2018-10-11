package com.thebaileybrew.flix2.loaders;

import android.os.AsyncTask;
import android.util.Log;

import com.thebaileybrew.flix2.BuildConfig;
import com.thebaileybrew.flix2.interfaces.adapters.CreditsAdapter;
import com.thebaileybrew.flix2.models.Credit;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.jsonUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CreditsLoader extends AsyncTask<String, Void, List<Credit>> {
    private static final String TAG = CreditsLoader.class.getSimpleName();

    private final CreditsAdapter mCreditsAdapter;

    public CreditsLoader(CreditsAdapter creditsAdapter) {
        mCreditsAdapter = creditsAdapter;
    }

    @Override
    protected List<Credit> doInBackground(String... strings) {
        if (strings.length < 1 || strings[0] == null) {
            Log.e(TAG, "doInBackground: strings length= "+ strings.length );
            return null;
        }
        String movieID = strings[0];
        Log.e(TAG, "doInBackground: movieID" + movieID );

        URL creditRequestUrl = UrlUtils.buildCreditsMovieUrl(BuildConfig.API_KEY, movieID);
        try {
            String jsonCreditResponse = jsonUtils.requestHttpsMovieCredits(creditRequestUrl);
            return jsonUtils.extractCreditDetails(jsonCreditResponse);
        } catch (Exception e) {
            Log.e(TAG, "doInBackground: can't request credits", e);
            return null;
        }
    }

    @Override
    protected  void onPostExecute(List<Credit> credits) {
        if (credits != null) {
            mCreditsAdapter.setCreditCollection(credits);
        }
        super.onPostExecute(credits);
    }


}

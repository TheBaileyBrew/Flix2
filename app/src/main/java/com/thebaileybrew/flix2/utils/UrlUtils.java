package com.thebaileybrew.flix2.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UrlUtils {
    private static final String TAG = UrlUtils.class.getSimpleName();

    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/";
    private static final String BASE_IMAGE_LARGE = "w500";

    private static final String BASE_MOVIE_URL = "https://api.themoviedb.org/3";
    private static final String BASE_MOVIE_PATH = "movie";
    private static final String BASE_MOVIE_PATH_DISCOVER = "discover";
    private static final String BASE_MOVIE_PATH_SEARCH = "search";
    private static final String BASE_CREDIT_PATH = "credits";

    private static final String API_KEY = "api_key";
    private static final String SORT_BY = "sort_by";
    private static final String VOTE_COUNT = "vote_count.gte";
    private static final String VOTE_MINIMUM = "1000";
    private static final String WITH_ORIG_LANGUAGE = "with_original_language";
    private static final String PRIMARY_RELEASE_YEAR = "primary_release_year";
    private static final String RELEASE_DATE_START = "release_date.gte";
    private static final String RELEASE_DATE_END = "release_date.lte";
    private static final String QUERY = "query";

    private static String baseMovieQueryUrl(String apiKey) {
        Uri movieQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                    .appendPath(BASE_MOVIE_PATH_DISCOVER)
                    .appendPath(BASE_MOVIE_PATH)
                    .appendQueryParameter(API_KEY, apiKey)
                    .build();
        return movieQuery.toString();
    }

    private static String baseMovieSearchUrl(String apiKey) {
        Uri movieQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(BASE_MOVIE_PATH_SEARCH)
                .appendPath(BASE_MOVIE_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        return movieQuery.toString();
    }

    private static String setMaxYear() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.US);
        Date curDate = Calendar.getInstance(Locale.US).getTime();
        return dateFormat.format(curDate);
    }
    private static String setCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date curDate = Calendar.getInstance(Locale.US).getTime();
        return dateFormat.format(curDate);
    }
    private static String setMaxDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar curCal = Calendar.getInstance(Locale.US);
        int currentYear = Calendar.YEAR;
        Calendar maxCal = Calendar.getInstance(Locale.US);
        maxCal.set(Calendar.MONTH, Calendar.OCTOBER);
        maxCal.set(Calendar.YEAR, currentYear);
        long calTime = curCal.getTimeInMillis();
        long maxTime = maxCal.getTimeInMillis();
        if (maxTime < calTime) {
            curCal.add(Calendar.YEAR, 1);
            curCal.add(Calendar.MONTH, -9);
        } else {
            curCal.add(Calendar.MONTH, 3);
        }
        Date getDate = curCal.getTime();
        return(dateFormat.format(getDate));
    }

    private static String sortWithLanguage(String uriBuilder, String value) {
        Uri movieQuery = Uri.parse(uriBuilder).buildUpon()
                .appendQueryParameter(WITH_ORIG_LANGUAGE, value)
                .build();
        return movieQuery.toString();
    }

    private static String sortWithFilterYear(String uriBuilder, String value) {
        Uri movieQuery = Uri.parse(uriBuilder).buildUpon()
                .appendQueryParameter(PRIMARY_RELEASE_YEAR, value)
                .build();
        return movieQuery.toString();
    }

    //Build the URL for querying all movies in the database
    public static URL buildMovieUrl(
            String apiKey, String languageSort, String sortingOrder, String filterYear, String searchQuery){
        Uri movieQueryUri;

        //If user enters search terms then that string will be queued and Shared Prefs ignored.
        //This is per the standards of the API query since /search functions without sort parameters
        //as compared to the /discover methodology that allows for a variety of sort methods
        if (!TextUtils.isEmpty(searchQuery)) {
            String baseMovie = baseMovieSearchUrl(apiKey);
            movieQueryUri = Uri.parse(baseMovie).buildUpon()
                    .appendQueryParameter(QUERY,searchQuery)
                    .build();
        } else {
            String baseMovie = baseMovieQueryUrl(apiKey);
            switch (sortingOrder) {
                case "vote_average.desc":
                    movieQueryUri = Uri.parse(baseMovie).buildUpon()
                            .appendQueryParameter(SORT_BY, sortingOrder)
                            .appendQueryParameter(VOTE_COUNT, VOTE_MINIMUM)
                            .build();
                    baseMovie = movieQueryUri.toString();
                    break;
                case "release_date.desc":
                    movieQueryUri = Uri.parse(baseMovie).buildUpon()
                            .appendQueryParameter(SORT_BY, sortingOrder)
                            .appendQueryParameter(RELEASE_DATE_START, setCurrentDate())
                            .appendQueryParameter(RELEASE_DATE_END, setMaxDate())
                            .build();
                    baseMovie = movieQueryUri.toString();
                    break;
                case "popularity.desc":
                    movieQueryUri = Uri.parse(baseMovie).buildUpon()
                            .appendQueryParameter(SORT_BY, sortingOrder)
                            .build();
                    baseMovie = movieQueryUri.toString();
                    break;
                case "favorite":
                    movieQueryUri = Uri.parse(baseMovie).buildUpon()
                            .appendQueryParameter(SORT_BY, "popularity.desc")
                            .build();
                    baseMovie = movieQueryUri.toString();
                    break;
                case "watchlist":
                    movieQueryUri = Uri.parse(baseMovie).buildUpon()
                            .appendQueryParameter(SORT_BY, "popularity.desc")
                            .build();
                    baseMovie = movieQueryUri.toString();
                    break;
            }

            //Checks for LANGUAGE SORT ALL
            if (!languageSort.equals("all")) {
                movieQueryUri = Uri.parse(sortWithLanguage(baseMovie, languageSort)).buildUpon().build();
                baseMovie = movieQueryUri.toString();
            }

            //Checks for FILTER YEAR PREFERENCE
            if (!filterYear.equals("0000")) {
                movieQueryUri = Uri.parse(sortWithFilterYear(baseMovie, filterYear)).buildUpon().build();
                baseMovie = movieQueryUri.toString();
            }
            movieQueryUri = Uri.parse(baseMovie).buildUpon().build();
        }

        URL movieQueryURL;
        try {
            movieQueryURL = new URL(movieQueryUri.toString());
            return movieQueryURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildMovieUrl: failed to build full db URL", me);
            return null;
        }
    }

    //Build the URL for querying a single movie in the database
    public static URL buildSingleMovieUrl(String apiKey, String movieID) {

        Uri singleMovieQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(BASE_MOVIE_PATH)
                .appendPath(movieID)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        URL singleMovieURL;
        try {
            singleMovieURL = new URL(singleMovieQuery.toString());
            return singleMovieURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildSingleMovieUrl: failed to build single URL", me);
            return null;
        }
    }

    //Build the poster path url
    public static String buildPosterPathUrl(String posterPath) {
        return BASE_IMAGE_URL + BASE_IMAGE_LARGE + posterPath;
    }
    //Build the backdrop path url
    public static String buildBackdropUrl(String backdropPath, String posterPath) {
        if (backdropPath.equals("null")) {
            return BASE_IMAGE_URL + BASE_IMAGE_LARGE + posterPath;
        } else {
            return BASE_IMAGE_URL + BASE_IMAGE_LARGE + backdropPath;
        }
    }

    //Build the credit details url
    public static URL buildCreditsMovieUrl(String apiKey, String movieID) {
        Uri movieCreditsQuery = Uri.parse(BASE_MOVIE_URL).buildUpon()
                .appendPath(BASE_MOVIE_PATH)
                .appendPath(movieID)
                .appendPath(BASE_CREDIT_PATH)
                .appendQueryParameter(API_KEY, apiKey)
                .build();
        URL creditsQueryURL;
        try {
            creditsQueryURL = new URL(movieCreditsQuery.toString());
            return creditsQueryURL;
        } catch (MalformedURLException me) {
            Log.e(TAG, "buildCreditsMovieUrl: failed to build credits url", me);
            return null;
        }
    }

    //Build the credit image url
    public static String buildCreditImageUrl (String creditPath) {
        return BASE_IMAGE_URL + BASE_IMAGE_LARGE + creditPath;
    }
}

package com.thebaileybrew.flix2.utils;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.thebaileybrew.flix2.FlixApplication;
import com.thebaileybrew.flix2.database.DatabaseClient;
import com.thebaileybrew.flix2.models.Credit;
import com.thebaileybrew.flix2.models.Film;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.models.Review;
import com.thebaileybrew.flix2.models.Videos;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class jsonUtils {
    private static final String TAG = jsonUtils.class.getSimpleName();

    //static resource references for querying API results
    private static final String MOVIE_ID = "id";
    private static final String MOVIE_NAME = "title";
    private static final String MOVIE_VOTE_COUNT = "vote_count";
    private static final String MOVIE_AVERAGE = "vote_average";
    private static final String MOVIE_POPULARITY = "popularity";
    private static final String MOVIE_ORIG_LANGUAGE = "original_language";
    private static final String MOVIE_POSTER_PATH = "poster_path";
    private static final String MOVIE_BACKDROP = "backdrop_path";
    private static final String MOVIE_SYNOPSIS = "overview";
    private static final String MOVIE_RELEASE_DATE = "release_date";

    private static final String MOVIE_TAGLINE = "tagline";
    private static final String MOVIE_GENRE = "genres";
    private static final String MOVIE_GENRE_NAME = "name";
    private static final String MOVIE_RUNTIME = "runtime";

    private static final String MOVIE_CAST = "cast";
    private static final String CREDIT_CHARACTER = "character";
    private static final String CREDIT_ACTOR = "name";
    private static final String CREDIT_IMAGE = "profile_path";

    private static final String VIDEO_LIST = "results";
    private static final String VIDEO_ID = "id";
    private static final String VIDEO_KEY = "key";
    private static final String VIDEO_NAME = "name";
    private static final String VIDEO_SIZE = "size";
    private static final String VIDEO_TYPE = "type";

    private static final String REVIEW_LIST = "results";
    private static final String REVIEW_AUTHOR = "author";
    private static final String REVIEW_CONTENT = "content";
    private static final String REVIEW_ID = "id";
    private static final String REVIEW_LINK = "url";



    private jsonUtils(){}

    //Get All Movie Data
    public static String makeHttpsRequest(URL url) throws IOException {
        String jsonResponse = "";
        //Check for null URL
        if (url == null) {
            return jsonResponse;
        }
        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            Log.e(TAG, "Full url is:" + String.valueOf(url));
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(12000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //If successful request
            if(urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse= readFromStream(inputStream);
            } else {
                Log.e(TAG, "makeHttpsRequest: Error Code: " +urlConnection.getResponseCode());
            }
        } catch (IOException ioe) {
            Log.e(TAG, "makeHttpsRequest: Cound not retrieve JSON result", ioe);
        } finally {
            if (urlConnection !=null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    //Extract & Return All Movie Data
    public static ArrayList<Movie> extractMoviesFromJson(String jsonData) {
        int movieID;
        int movieVoteCount;
        double movieVoteAverage;
        String movieTitle;
        long moviePopularity;
        String movieLanguage;
        String moviePosterPath;
        String movieBackdrop;
        String movieOverview;
        String movieReleaseDate;

        //Check for NULL jsonData
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }
        ArrayList<Movie> movieCollection = new ArrayList<>();
        try {
            JSONObject baseJSONResponse = new JSONObject(jsonData);

            JSONArray baseJSONArray = baseJSONResponse.getJSONArray("results");
            //Loop through film results
            for (int m = 0; m < baseJSONArray.length(); m++) {
                JSONObject currentFilm = baseJSONArray.getJSONObject(m);
                //Extract the movie ID
                movieID = currentFilm.optInt(MOVIE_ID);
                //Extract the movie vote count
                movieVoteCount = currentFilm.optInt(MOVIE_VOTE_COUNT);
                //Extract the movie vote average
                movieVoteAverage = currentFilm.optDouble(MOVIE_AVERAGE);
                //Extract the movie title
                movieTitle = currentFilm.optString(MOVIE_NAME);
                //Extract the movie popularity
                moviePopularity = currentFilm.optLong(MOVIE_POPULARITY);
                //Extract the movie language
                movieLanguage = currentFilm.optString(MOVIE_ORIG_LANGUAGE);
                //Extract the movie poster path and pass through UrlUtils to build full path
                moviePosterPath = currentFilm.optString(MOVIE_POSTER_PATH);
                //Extract the movie backdrop and pass through UrlUtils to build full path
                movieBackdrop = currentFilm.optString(MOVIE_BACKDROP, "null");
                //Extract the movie overview
                movieOverview = currentFilm.optString(MOVIE_SYNOPSIS);
                //Extract the movie release date
                movieReleaseDate = currentFilm.optString(MOVIE_RELEASE_DATE);
                //Pass the JSON into the Async for database loading
                Movie movie = new Movie();
                movie.setMovieID(movieID);
                movie.setMovieVoteCount(movieVoteCount);
                movie.setMovieVoteAverage(movieVoteAverage);
                movie.setMovieTitle(movieTitle);
                movie.setMoviePopularity(moviePopularity);
                movie.setMovieLanguage(movieLanguage);
                movie.setMoviePosterPath(moviePosterPath);
                movie.setMovieBackdrop(movieBackdrop);
                movie.setMovieOverview(movieOverview);
                movie.setMovieReleaseDate(movieReleaseDate);
                movieCollection.add(movie);
                loadMovieDetail(movie);
                
            }

        } catch (JSONException je) {
            Log.e(TAG, "extractMoviesFromJson: problems extracting film details from json", je);
        }
        return movieCollection;
    }
    //Create the Async to load movie details into the database
    private static void loadMovieDetail(final Movie movie) {


        class loadDatabase extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {


                //Add the movie to the Database
                DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .movieDao().insertMovie(movie);
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                super.onPostExecute(v);

            }
        }
        loadDatabase ldb = new loadDatabase();
        ldb.execute();
    }

    //Get Single Movie Detail Extra Data
    public static String requestHttpsSingleFilm (URL url) throws IOException {
        String jsonResponse = "";
        //Check for NULL
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            Log.e(TAG, "requestHttpsSingleFilm: full url is: " + String.valueOf(url));
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(12000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //If successful request
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "requestHttpsSingleFilm: Errod code: "
                        + urlConnection.getResponseCode());
            }
        } catch (IOException ie) {
            Log.e(TAG, "requestHttpsSingleFilm: Could not retrieve JSON", ie);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    //Extract & Return Single Movie Detail Extra Data
    public static ArrayList<Film> extractSingleFilmData(String jsonReturn) {
        String movieTagline;
        int movieRuntime;
        String movieGenre;

        if (TextUtils.isEmpty(jsonReturn)) {
            return null;
        }
        ArrayList<Film> movieExtraDetails = new ArrayList<>();
        try {
            JSONObject baseJsonResponse = new JSONObject(jsonReturn);
            movieTagline = baseJsonResponse.optString(MOVIE_TAGLINE, "");
            movieRuntime = baseJsonResponse.optInt(MOVIE_RUNTIME, 0);
            StringBuilder outputString = new StringBuilder();
            JSONArray genreFilmArray = baseJsonResponse.getJSONArray(MOVIE_GENRE);
            for (int g = 0; g < genreFilmArray.length(); g ++) {
                JSONObject currentGenres = genreFilmArray.getJSONObject(g);
                String tempGenre = currentGenres.getString(MOVIE_GENRE_NAME);
                outputString.append(tempGenre);
                outputString.append(" ");
            }
            movieGenre = outputString.toString();
            Film film = new Film();
            film.setMovieTagLine(movieTagline);
            film.setMovieGenre(movieGenre);
            film.setMovieRuntime(movieRuntime);
            movieExtraDetails.add(film);

        } catch (JSONException je) {
            Log.e(TAG, "extractSingleFilmData: Problem extracting details", je);
        }
        return movieExtraDetails;
    }

    //Get Single Movie Credit Details
    public static String requestHttpsMovieCredits (URL url) throws IOException {
        String jsonResponse = "";
        //Check for NULL
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(12000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //If successful
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "requestHttpsMovieCredits: Error code: "
                        + urlConnection.getResponseCode() );
            }
        } catch (IOException ie) {
            Log.e(TAG, "requestHttpsMovieCredits: Could not retrieve JSON", ie);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    //Extract Single Movie Credit Details
    public static ArrayList<Credit> extractCreditDetails(String jsonReturn) {
        String characterName;
        String characterActor;
        String characterImage;

        if (TextUtils.isEmpty(jsonReturn)) {
            return null;
        }
        ArrayList<Credit> movieCredits = new ArrayList<>();
        try {
            JSONObject baseJSONResponse = new JSONObject(jsonReturn);
            JSONArray creditsList = baseJSONResponse.getJSONArray(MOVIE_CAST);
            if (TextUtils.isEmpty(String.valueOf(creditsList))) {
                return null;
            }
            if (creditsList.length() > 10) {
                for (int c = 0; c < 10; c++) {
                    JSONObject currentCharacter = creditsList.getJSONObject(c);
                    characterName = currentCharacter.getString(CREDIT_CHARACTER);
                    characterActor = currentCharacter.getString(CREDIT_ACTOR);
                    characterImage = currentCharacter.getString(CREDIT_IMAGE);
                    Credit credit = new Credit();
                    credit.setCreditActorName(characterActor);
                    credit.setCreditCharacterName(characterName);
                    credit.setCreditPath(characterImage);
                    movieCredits.add(credit);
                }
            } else {
                for (int c = 0; c < creditsList.length(); c++) {
                    JSONObject currentCharacter = creditsList.getJSONObject(c);
                    characterName = currentCharacter.getString(CREDIT_CHARACTER);
                    characterActor = currentCharacter.getString(CREDIT_ACTOR);
                    characterImage = currentCharacter.getString(CREDIT_IMAGE);
                    Credit credit = new Credit();
                    credit.setCreditActorName(characterActor);
                    credit.setCreditCharacterName(characterName);
                    credit.setCreditPath(characterImage);
                    movieCredits.add(credit);
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "extractCreditDetails: problem getting credits", je);
        }
        return movieCredits;
    }

    //Get Single Movie Video Details
    public static String requestHttpsMovieVideos (URL url) throws IOException {
        String jsonResponse = "";
        //Check for NULL
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(12000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //If successful
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "requestHttpsMovieVideos: Error code: "
                        + urlConnection.getResponseCode() );
            }
        } catch (IOException ie) {
            Log.e(TAG, "requestHttpsMovieVideos: Could not retrieve JSON", ie);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    //Extract Single Movie Credit Details
    public static ArrayList<Videos> extractVideosDetails(String jsonReturn) {
        String videoID;
        String videoName;
        String videoKey;
        String videoSize;
        String videoType;

        if (TextUtils.isEmpty(jsonReturn)) {
            return null;
        }
        ArrayList<Videos> movieVideos = new ArrayList<>();
        try {
            JSONObject baseJSONResponse = new JSONObject(jsonReturn);
            JSONArray videosList = baseJSONResponse.getJSONArray(VIDEO_LIST);
            if (TextUtils.isEmpty(String.valueOf(videosList))) {
                return null;
            }
            if (videosList.length() == 0) {
                Log.e(TAG, "extractVideosDetails: length=0 no videos to extract");
            } else if (videosList.length() > 10) {
                for (int c = 0; c < 10; c++) {
                    JSONObject currentVideo = videosList.getJSONObject(c);
                    videoID = currentVideo.getString(VIDEO_ID);
                    videoName = currentVideo.getString(VIDEO_NAME);
                    videoKey = currentVideo.getString(VIDEO_KEY);
                    videoSize = currentVideo.getString(VIDEO_SIZE);
                    videoType = currentVideo.getString(VIDEO_TYPE);
                    Videos video = new Videos();
                    video.setVideoID(videoID);
                    video.setVideoName(videoName);
                    video.setVideoKey(videoKey);
                    video.setVideoSize(videoSize);
                    video.setVideoType(videoType);
                    movieVideos.add(video);
                }
            } else {
                for (int c = 0; c < videosList.length(); c++) {
                    JSONObject currentVideo = videosList.getJSONObject(c);
                    videoID = currentVideo.getString(VIDEO_ID);
                    videoName = currentVideo.getString(VIDEO_NAME);
                    videoKey = currentVideo.getString(VIDEO_KEY);
                    videoSize = currentVideo.getString(VIDEO_SIZE);
                    videoType = currentVideo.getString(VIDEO_TYPE);
                    Videos video = new Videos();
                    video.setVideoID(videoID);
                    video.setVideoName(videoName);
                    video.setVideoKey(videoKey);
                    video.setVideoSize(videoSize);
                    video.setVideoType(videoType);
                    movieVideos.add(video);
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "extractVideoDetails: problem getting videos", je);
        }
        return movieVideos;
    }

    //Get Single Movie Review Details
    public static String requestHttpsMovieReviews (URL url) throws IOException {
        String jsonResponse = "";
        //Check for NULL
        if (url == null) {
            return jsonResponse;
        }

        HttpsURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(12000);
            urlConnection.setConnectTimeout(20000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            //If successful
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "requestHttpsMovieReviews: Error code: "
                        + urlConnection.getResponseCode() );
            }
        } catch (IOException ie) {
            Log.e(TAG, "requestHttpsMovieReviews: Could not retrieve JSON", ie);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    //Extract Single Movie Review Details
    public static ArrayList<Review> extractReviewDetails(String jsonReturn) {
        String reviewID;
        String reviewContent;
        String reviewAuthor;
        String reviewLink;

        if (TextUtils.isEmpty(jsonReturn)) {
            return null;
        }
        ArrayList<Review> movieReviews = new ArrayList<>();
        try {
            JSONObject baseJSONResponse = new JSONObject(jsonReturn);
            JSONArray reviewList = baseJSONResponse.getJSONArray(REVIEW_LIST);
            if (TextUtils.isEmpty(String.valueOf(reviewList))) {
                return null;
            }
             if (reviewList.length() == 0) {
                 Log.e(TAG, "extractReviewDetails: length = 0 no reviews to extract");
             } else if (reviewList.length() > 10) {
                for (int c = 0; c < 10; c++) {
                    JSONObject currentReview = reviewList.getJSONObject(c);
                    reviewID = currentReview.getString(REVIEW_ID);
                    reviewContent = currentReview.getString(REVIEW_CONTENT);
                    reviewAuthor = currentReview.getString(REVIEW_AUTHOR);
                    reviewLink = currentReview.getString(REVIEW_LINK);
                    Review review = new Review();
                    review.setReviewID(reviewID);
                    review.setReviewContent(reviewContent);
                    review.setReviewAuthor(reviewAuthor);
                    review.setReviewLink(reviewLink);
                    movieReviews.add(review);
                }
            } else {
                for (int c = 0; c < reviewList.length(); c++) {
                    JSONObject currentReview = reviewList.getJSONObject(c);
                    reviewID = currentReview.getString(REVIEW_ID);
                    reviewContent = currentReview.getString(REVIEW_CONTENT);
                    reviewAuthor = currentReview.getString(REVIEW_AUTHOR);
                    reviewLink = currentReview.getString(REVIEW_LINK);
                    Review review = new Review();
                    review.setReviewID(reviewID);
                    review.setReviewContent(reviewContent);
                    review.setReviewAuthor(reviewAuthor);
                    review.setReviewLink(reviewLink);
                    movieReviews.add(review);
                }
            }
        } catch (JSONException je) {
            Log.e(TAG, "extractVideoDetails: problem getting videos", je);
        }
        return movieReviews;
    }

    //Read All Movie Data
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}

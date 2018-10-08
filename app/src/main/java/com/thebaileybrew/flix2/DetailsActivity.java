package com.thebaileybrew.flix2;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.database.DatabaseClient;
import com.thebaileybrew.flix2.interfaces.adapters.CollapsingToolbarListener;
import com.thebaileybrew.flix2.interfaces.adapters.CreditsAdapter;
import com.thebaileybrew.flix2.interfaces.adapters.StaticProgressBar;
import com.thebaileybrew.flix2.loaders.CreditsLoader;
import com.thebaileybrew.flix2.loaders.SingleMovieLoader;
import com.thebaileybrew.flix2.models.Credit;
import com.thebaileybrew.flix2.models.Film;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.networkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class DetailsActivity extends AppCompatActivity {
    private final static String TAG = DetailsActivity.class.getSimpleName();

    private final static String MOVIE_KEY = "parcel_movie";
    private final static String TIME_FORMAT = "%02d:%02d";

    private androidx.appcompat.widget.Toolbar mToolbar;
    private ImageView posterImage;
    private ImageView poster;
    private LinearLayout movieDetailsLayout;
    private TextView movieRuntime;
    private TextView movieRelease;
    private TextView movieRating;
    private StaticProgressBar movieRatingBar;
    private TextView movieOverview;
    private Animation animScaleDown, animFadeOut;
    private Animation animScaleUp, animFadeIn;
    private Boolean posterHidden = false;
    private double currentFilmRating;
    private TextView ratingTitleHeader;
    private TextView movieTagline;
    private TextView movieGenres;
    private View scrimView;
    private static List<Credit> credits = new ArrayList<>();
    private static RecyclerView creditsRecycler;
    private TextView noCreditsText;
    private List<Film> arrayFilm = new ArrayList<>();
    private boolean landscapeMode;

    private static CreditsAdapter creditsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        landscapeMode = getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels;
        Log.e(TAG, "onCreate: widthpx: " + getResources().getDisplayMetrics().widthPixels);
        Log.e(TAG, "onCreate: heightpx: " + getResources().getDisplayMetrics().heightPixels);
        defineAnimations();
        initViews();
        //Setup the toolbar with navigation back
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimaryText), PorterDuff.Mode.SRC_ATOP);
        Intent getMovieIntent = getIntent();
        //Check for Parcelable data pass
        if (getMovieIntent != null) {
            if (getMovieIntent.hasExtra(MOVIE_KEY)) {
                Movie movie = getMovieIntent.getParcelableExtra(MOVIE_KEY);
                getSupportActionBar().setTitle(movie.getMovieTitle());
                getSupportActionBar().getThemedContext();
                populateUI(movie);
                currentFilmRating = movie.getMovieVoteAverage();
            }
        }
        //Set up the CollapsingToolbar and attach a listener to determine the current state
        //This method also starts the animation for hiding/showing StaticProgress and Poster objects
        AppBarLayout appBarLayout = findViewById(R.id.app_toolbar);
        appBarLayout.setExpanded(true);
        appBarLayout.addOnOffsetChangedListener(new CollapsingToolbarListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                Log.d(TAG, "onStateChanged: Current State: " + state.name());
                switch (state) {
                    case COLLAPSED:
                        hidePosterImage();
                        showRatingBar();
                        updateRatingBar(currentFilmRating);
                        posterHidden = true;
                        scrimView.setBackgroundResource(R.drawable.shape_scrim_collapsed);
                        break;
                    case EXPANDED:
                        showPosterImage();
                        hideRatingBar();
                        scrimView.setBackgroundResource(R.drawable.shape_scrim);
                        posterHidden = false;
                        break;
                    default:
                        scrimView.setBackgroundResource(R.drawable.shape_scrim);
                        break;
                }
            }
        });
    }

    //Method for declaring the Animation Effects that can happen to objects in any viewstate
    private void defineAnimations() {
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out_ratingbar);
        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.anim_scale_up);
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_ratingbar);
    }

    //Method for updating and animating the progress of the StaticProgressBar object
    private void updateRatingBar(double currentProgress) {
        movieRatingBar.setMax(10);
        movieRatingBar.setSuffixText(" ");
        float value = (float) currentProgress;
        ObjectAnimator animation = ObjectAnimator.ofFloat(movieRatingBar,"progress", 0, value);
        animation.setDuration(2000);
        animation.setInterpolator(new OvershootInterpolator());
        animation.start();
        //Update Text, Rating and Bar Color depending on vote_average returned via json pull
        if(currentProgress >= 7.00) {
            movieRatingBar.setTextColor(Color.parseColor("#ffffff"));
            movieRatingBar.setFinishedStrokeColor(Color.parseColor("#25cc00"));
            movieRatingBar.setUnfinishedStrokeColor(Color.parseColor("#b8ffc3"));
            movieRatingBar.setBackgroundResource(R.drawable.good_rounded_edge);
            if (currentProgress >= 8.00) {
                movieRatingBar.setBottomText("GREAT");
            } else if (currentProgress >= 9.00) {
                movieRatingBar.setBottomText("BEST");
            } else {
                movieRatingBar.setBottomText("GOOD");
            }
        } else if (currentProgress >= 4.25) {
            movieRatingBar.setTextColor(Color.parseColor("#ffffff"));
            movieRatingBar.setFinishedStrokeColor(Color.parseColor("#f5c400"));
            movieRatingBar.setUnfinishedStrokeColor(Color.parseColor("#ffe7ab"));
            movieRatingBar.setBackgroundResource(R.drawable.mid_rounded_edge);
            if (currentProgress >= 6.00) {
                movieRatingBar.setBottomText("AVERAGE");
            } else if (currentProgress >=4.75) {
                movieRatingBar.setBottomText("OKAY");
            } else {
                movieRatingBar.setBottomText("MEH..");
            }
        } else {
            movieRatingBar.setTextColor(Color.parseColor("#ffffff"));
            movieRatingBar.setFinishedStrokeColor(Color.parseColor("#dc0202"));
            movieRatingBar.setUnfinishedStrokeColor(Color.parseColor("#ffa1a1"));
            movieRatingBar.setBackgroundResource(R.drawable.bad_rounded_edge);
            if (currentProgress >= 3.5) {
                movieRatingBar.setBottomText("MEH");
            } else if (currentProgress >= 2.00) {
                movieRatingBar.setBottomText("AVOID");
            } else if (currentProgress == 0.00) {
                movieRatingBar.setBottomText("NO SCORE");
            } else {
                movieRatingBar.setBottomText("HARD PASS");
            }
        }

    }

    //Method to show the poster image overlapping the collapsing toolbar
    private void showPosterImage() {
        poster.startAnimation(animScaleUp);
        poster.setVisibility(VISIBLE);
    }
    //Method to hide the StaticProgressBar (behind the poster image) when poster is revealed
    private void hideRatingBar() {
        movieRatingBar.startAnimation(animFadeOut);
        movieRatingBar.setVisibility(INVISIBLE);
        ratingTitleHeader.startAnimation(animFadeIn);
        ratingTitleHeader.setVisibility(VISIBLE);
        movieRating.startAnimation(animFadeIn);
        movieRating.setVisibility(VISIBLE);
        if (landscapeMode) {
            hideStats();
            hideCredits();
        }
    }

    //Method to hide the poster image overlapping the collapsing toolbar
    private void hidePosterImage() {
        poster.startAnimation(animScaleDown);
        poster.setVisibility(INVISIBLE);
    }
    //Method to show the StaticProgressBar (behind the poster) when the poster is hidden
    private void showRatingBar() {
        movieRatingBar.startAnimation(animFadeIn);
        movieRatingBar.setVisibility(VISIBLE);
        ratingTitleHeader.startAnimation(animFadeOut);
        ratingTitleHeader.setVisibility(INVISIBLE);
        movieRating.startAnimation(animFadeOut);
        movieRating.setVisibility(INVISIBLE);
        if (landscapeMode) {
            showStats();
            showCredits();
        }
    }

    //Method to display the Credits Recycler (only in Landscape)
    private void showCredits() {
        creditsRecycler.setAnimation(animFadeIn);
        creditsRecycler.setVisibility(VISIBLE);
    }

    //Method to hide the Credits Recycler (only in Landscape)
    private void hideCredits() {
        creditsRecycler.setAnimation(animFadeOut);
        creditsRecycler.setVisibility(INVISIBLE);
    }

    //Method to display the stats layout (only in Landscape)
    private void showStats() {
        movieDetailsLayout.setAnimation(animFadeIn);
        movieDetailsLayout.setVisibility(VISIBLE);
    }

    //Method to hide the stats layout (only in Landscape)
    private void hideStats() {
        movieDetailsLayout.setAnimation(animFadeOut);
        movieDetailsLayout.setVisibility(INVISIBLE);
    }

    //Populate the UI with the details pulled from Async task and Parcelable
    private void populateUI(final Movie movie) {
        //Set up the Credit Recycler
        creditsRecycler = findViewById(R.id.credits_recycler);
        if (networkUtils.checkNetwork(DetailsActivity.this)) {
            //Load all data from credits json & details json
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                    LinearLayoutManager.HORIZONTAL,false);
            Log.e(TAG, "populateUI: " + String.valueOf(movie.getMovieID()));
            getCredits(String.valueOf(movie.getMovieID()));
            creditsRecycler.setLayoutManager(linearLayoutManager);
            CreditsLoader creditsLoader = new CreditsLoader();
            creditsLoader.execute(String.valueOf(movie.getMovieID()));
            //Set up the details for single film details
            SingleMovieLoader singleMovieLoader = new SingleMovieLoader();
            singleMovieLoader.execute(String.valueOf(movie.getMovieID()));
            loadExtraDetails(String.valueOf(movie.getMovieID()));

        } else {
            //Load only data from Intent and add network message
            noCreditsText.setText(R.string.check_network_credits_display);
            creditsRecycler.setVisibility(INVISIBLE);
            movieRuntime.setText(R.string.unknown_time);
        }
        //Load the imagery into the Toolbar and the Poster image
        Picasso.get().load(UrlUtils.buildPosterPathUrl(movie.getMoviePosterPath())).into(poster);
        Picasso.get().load(UrlUtils.buildBackdropUrl(movie.getMovieBackdrop(), movie.getMoviePosterPath())).into(posterImage);
        movieOverview.setText(movie.getMovieOverview());

        String fullRating = String.valueOf(trimRating((float)movie.getMovieVoteAverage()));
        movieRating.setText(fullRating);
        movieRelease.setText(formatDate(movie.getMovieReleaseDate()));

    }

    private static void loadExtraDetails(final String movieID) {
        class GetDetails extends AsyncTask<Void, Void, List<Film>> {
            @Override
            protected List<Film> doInBackground(Void... voids) {
                Log.e(TAG, "doInBackground: loading details");
                return DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .filmDao().loadSingleFilm(movieID);
            }

            @Override
            protected void onPostExecute(List<Film> films) {
                super.onPostExecute(films);
                Log.e(TAG, "onPostExecute: details loaded" + films.size());
            }
        }
        GetDetails gd = new GetDetails();
        gd.execute();
    }

    private static void getCredits(final String movieID) {
        class GetCredits extends AsyncTask<Void, Void, List<Credit>> {
            @Override
            protected List<Credit> doInBackground(Void... voids) {
                Log.e(TAG, "doInBackground: loading credits");
                credits = DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .creditDao().loadSingleFilmCredit(movieID);
                return credits;
            }

            @Override
            protected void onPostExecute(List<Credit> credits) {
                super.onPostExecute(credits);
                creditsAdapter = new CreditsAdapter(FlixApplication.getContext(), credits,
                        creditsRecycler);
                creditsRecycler.setAdapter(creditsAdapter);
                Log.e(TAG, "onPostExecute: credits loaded");
            }
        }
        GetCredits gc = new GetCredits();
        gc.execute();
    }

    //Trim the rating value to two digits
    private String trimRating(float fullRating) {
        Log.e(TAG, "trimRating: " + fullRating );
        String tempString = String.valueOf(fullRating);
        String filteredString = tempString.substring(0,3);
        double tempDouble = Double.parseDouble(filteredString);
        return String.format(Locale.US, "%.2f", tempDouble);
    }

    //Convert the time (minutes) into hours & minutes
    private String convertTime(int movieRuntime) {
        int hours = movieRuntime / 60;
        int minutes = movieRuntime % 60;
        return String.format(Locale.US, TIME_FORMAT, hours, minutes);
    }

    //Format the release date from 0000-00-00 to 00/00/0000
    private String formatDate(String movieReleaseDate) {
        String[] datestamps = movieReleaseDate.split("-");
        String dateYear = datestamps[0];
        String dateMonth = datestamps[1];
        String dateDay = datestamps[2];
        return dateMonth + getString(R.string.linebreak) + dateDay + getString(R.string.linebreak) + dateYear;
    }

    //Declare and bind the views in this layout
    private void initViews() {
        mToolbar = findViewById(R.id.toolbar);
        scrimView = findViewById(R.id.scrim_view);
        noCreditsText = findViewById(R.id.no_credits_view);
        creditsRecycler = findViewById(R.id.credits_recycler);
        poster = findViewById(R.id.poster);
        posterImage = findViewById(R.id.poster_imageview);
        movieDetailsLayout = findViewById(R.id.linear_layout_headers_details);
        movieRuntime = findViewById(R.id.movie_runtime);
        movieRelease = findViewById(R.id.movie_release);
        movieRating = findViewById(R.id.movie_rating);
        movieRatingBar = findViewById(R.id.progress);
        movieOverview = findViewById(R.id.synopsis_text);
        movieOverview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_20sp));
        movieTagline = findViewById(R.id.movie_tagline);
        movieGenres = findViewById(R.id.movie_genres);
        ratingTitleHeader = findViewById(R.id.rating_title);

    }

}

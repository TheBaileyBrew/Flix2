package com.thebaileybrew.flix2;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;
import com.thebaileybrew.flix2.database.MovieRepository;
import com.thebaileybrew.flix2.interfaces.adapters.CollapsingToolbarListener;
import com.thebaileybrew.flix2.interfaces.adapters.DetailFragmentAdapter;
import com.thebaileybrew.flix2.utils.objects.StaticProgressBar;
import com.thebaileybrew.flix2.loaders.MovieRuntimeLoader;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.UrlUtils;
import com.thebaileybrew.flix2.utils.networkUtils;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.viewpager.widget.ViewPager;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class DetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = DetailsActivity.class.getSimpleName();

    private final static String MOVIE_KEY = "parcel_movie";
    private final static String TIME_FORMAT = "%02d:%02d";

    private androidx.appcompat.widget.Toolbar mToolbar;
    private ImageView posterImage;
    private AppCompatImageView poster;
    private LinearLayout movieDetailsLayout;
    private TextView movieRuntime;
    private TextView movieRelease;
    private StaticProgressBar movieRatingBar;
    private Animation animScaleDown, animFadeOut;
    private Animation animScaleUp, animFadeIn;
    private Boolean posterHidden = false;
    private double currentFilmRating;
    private View scrimView;
    private boolean landscapeMode;
    private boolean isFavorite;
    private boolean firstload = true;
    private FloatingActionButton movieFavorite;
    private static Movie movie;
    private ViewPager viewPager;
    MovieRepository movieRepo;
    Movie thisMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        movieRepo = new MovieRepository(getApplication());

        landscapeMode = getResources().getDisplayMetrics().widthPixels > getResources().getDisplayMetrics().heightPixels;
        Log.e(TAG, "onCreate: widthpx: " + getResources().getDisplayMetrics().widthPixels);
        Log.e(TAG, "onCreate: heightpx: " + getResources().getDisplayMetrics().heightPixels);
        defineAnimations();
        initViews();

        //Setup the toolbar with navigation back
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorPrimaryText), PorterDuff.Mode.SRC_ATOP);
        viewPager = findViewById(R.id.pager_viewpager);
        DetailFragmentAdapter fragmentAdapter = new DetailFragmentAdapter(this, getSupportFragmentManager());
        TabLayout tabLayout = findViewById(R.id.pager_tabs);
        tabLayout.setupWithViewPager(viewPager, true);
        viewPager.setAdapter(fragmentAdapter);
        Intent getMovieIntent = getIntent();
        //Check for Parcelable data pass
        if (getMovieIntent != null) {
            if (getMovieIntent.hasExtra(MOVIE_KEY)) {
                movie = getMovieIntent.getParcelableExtra(MOVIE_KEY);
                if (checkForDatabaseRecord(movie.getMovieID())) {
                    movie = thisMovie;
                }
                getSupportActionBar().setTitle(movie.getMovieTitle());
                getSupportActionBar().getThemedContext();
                populateUI(movie);
                currentFilmRating = movie.getMovieVoteAverage();
                setFAB();
                Log.e(TAG, "onCreate: movie is favorite" + movie.getMovieFavorite() );

            }
        }
        //Set up the CollapsingToolbar and attach a listener to determine the current state
        //This method also starts the animation for hiding/showing StaticProgress and Poster objects
        AppBarLayout appBarLayout = findViewById(R.id.app_toolbar);
        appBarLayout.setExpanded(true);
        appBarLayout.addOnOffsetChangedListener(new CollapsingToolbarListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case EXPANDED:
                        if (poster.getVisibility() != View.VISIBLE) {
                            poster.startAnimation(animFadeIn);
                            poster.setVisibility(VISIBLE);
                            hideRatingBar();
                        }
                        scrimView.setBackgroundResource(R.drawable.shape_scrim);
                        break;
                    case COLLAPSED:
                        if (poster.getVisibility() == View.VISIBLE) {
                            poster.startAnimation(animFadeOut);
                            poster.setVisibility(View.INVISIBLE);
                            showRatingBar();
                            updateRatingBar(currentFilmRating);
                        }
                        scrimView.setBackgroundResource(R.drawable.shape_scrim_collapsed);
                        break;
                    case IDLE:
                        scrimView.setBackgroundResource(R.drawable.shape_scrim_collapsed);
                        break;
                }
            }
        });

        viewPager.getAdapter().notifyDataSetChanged();
    }

    private void setFAB() {
        int fav = movie.getMovieFavorite();
        switch (fav) {
            case 0:
            default:
                movieFavorite.setImageResource(R.drawable.ic_star);
                break;
            case 1:
                movieFavorite.setImageResource(R.drawable.ic_star_border);
                break;
        }
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


    //Method to hide the StaticProgressBar (behind the poster image) when poster is revealed
    private void hideRatingBar() {
        movieRatingBar.startAnimation(animFadeOut);
        movieRatingBar.setVisibility(INVISIBLE);
        if (landscapeMode) {
            hideStats();
        }
    }

    //Method to show the StaticProgressBar (behind the poster) when the poster is hidden
    private void showRatingBar() {
        movieRatingBar.startAnimation(animFadeIn);
        movieRatingBar.setVisibility(VISIBLE);
        if (landscapeMode) {
            showStats();
        }
    }

    //TODO: LOOK AT FOR LANDSCAPE LAYOUT

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
        movieFavorite.setOnClickListener(this);
        //Check for Network Connectivity
        if (networkUtils.checkNetwork(DetailsActivity.this)) {
            //GET MOVIE RUNTIME FROM API QUERY
            MovieRuntimeLoader movieRuntimeLoader = new MovieRuntimeLoader(movieRuntime);
            movieRuntimeLoader.execute(String.valueOf(movie.getMovieID()));
        } else {
            //LOAD ONLY WHEN NO INTERNET
            movieRuntime.setText(R.string.unknown_time);
        }
        //Load the imagery into the Toolbar and the Poster image
        Picasso.get().load(UrlUtils.buildPosterPathUrl(movie.getMoviePosterPath())).into(poster);
        Picasso.get().load(UrlUtils.buildBackdropUrl(movie.getMovieBackdrop(), movie.getMoviePosterPath())).into(posterImage);
        //Load the rating and release date from db
        String fullRating = String.valueOf(trimRating((float)movie.getMovieVoteAverage()));
        movieRelease.setText(formatDate(movie.getMovieReleaseDate()));

        int currentStar = movie.getMovieFavorite();
        switch (currentStar) {
            case 1: //MOVIE IS FAVORITE
                movieFavorite.setImageResource(R.drawable.ic_star_border);
                isFavorite = true;
                break;
            case 0: //MOVIE IS NOT FAVORITE nor INTERESTED
            default:
                movieFavorite.setImageResource(R.drawable.ic_star);
                isFavorite = false;
                break;
        }
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
        poster = findViewById(R.id.poster);
        posterImage = findViewById(R.id.poster_imageview);
        movieDetailsLayout = findViewById(R.id.linear_layout_headers_details);
        movieRuntime = findViewById(R.id.movie_runtime);
        movieRelease = findViewById(R.id.movie_release);
        movieRatingBar = findViewById(R.id.progress);
        movieFavorite = findViewById(R.id.floating_favorite_button);

    }

    @Override
    public void onClick(View v) {
        int updateValue;
        switch (v.getId()) {
            case R.id.floating_favorite_button:
                //TODO update db entry
                if (isFavorite) {
                    //MARK AS NOT FAVORITE
                    isFavorite = false; //0
                    updateValue = 0;
                    movie.setMovieFavorite(updateValue);
                    movieFavorite.setImageDrawable(getDrawable(R.drawable.ic_star));
                    updateMovieDatabase(movie);
                    showToastMessageFavoriteModification(updateValue);
                } else {
                    //MARK AS NEW FAVORITE
                    isFavorite = true; //1
                    updateValue = 1;
                    movie.setMovieFavorite(updateValue);
                    movieFavorite.setImageDrawable(getDrawable(R.drawable.ic_star_border));
                    updateMovieDatabase(movie);
                    showToastMessageFavoriteModification(updateValue);
                }
                break;
            default:
                break;
        }
    }

    private boolean checkForDatabaseRecord(int movieID) {
        thisMovie = movieRepo.getSingleFilm(movieID);
        if (thisMovie == null) {
            return false;
        } else {
            return true;
        }
    }

    private void updateMovieDatabase(Movie movie) {
        int favValue = movie.getMovieFavorite();
        switch (favValue) {
            case 0:
                movieRepo.removeFavorite(movie);
                break;
            case 1:
                movieRepo.insertFavorite(movie);
                break;
        }
    }

    private void showToastMessageFavoriteModification(int selection) {
        Toast toast = Toast.makeText(FlixApplication.getContext(), movie.getMovieTitle() + " removed from Favorites", Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.custom_toast_drawable);
        view.setAlpha((float) 0.65);
        TextView text = view.findViewById(android.R.id.message);
        switch (selection) {
            case 0:
                String removeFav = movie.getMovieTitle() + "\n" + " was removed from Favorites";
                text.setText(removeFav);
                text.setTextSize(22);
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                text.setTextColor(getColor(R.color.colorPrimaryText));
                toast.show();
                break;
            case 1:
                String addFav = movie.getMovieTitle() + "\n" + " was added to Favorites";
                text.setText(addFav);
                text.setTextSize(22);
                text.setGravity(Gravity.CENTER_HORIZONTAL);
                text.setTextColor(getColor(R.color.colorPrimaryText));
                toast.show();
        }
    }

    public static Movie getSelected() {
        return movie;
    }
}

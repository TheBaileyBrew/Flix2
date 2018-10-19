package com.thebaileybrew.flix2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.thebaileybrew.flix2.database.DatabaseClient;
import com.thebaileybrew.flix2.database.MovieRepository;
import com.thebaileybrew.flix2.interfaces.MoviePreferences;
import com.thebaileybrew.flix2.interfaces.adapters.MovieAdapter;
import com.thebaileybrew.flix2.loaders.MovieLoader;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.database.viewmodels.MainViewModel;
import com.thebaileybrew.flix2.utils.displayMetricsUtils;
import com.thebaileybrew.flix2.utils.networkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.view.View.VISIBLE;

public class MovieActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterClickHandler {
    private final static String TAG = MovieActivity.class.getSimpleName();



    private final static String MOVIE_KEY = "parcel_movie";
    private final static String RECYCLER_POSITION = "recycler_position";
    private final static String RECYCLER_STATE = "recycler_state";

    private MainViewModel movieViewModel;
    private MovieAdapter mAdapter;
    private SharedPreferences sharedPrefs;


    private String queryResult = "";
    private String sorting, language, filterYear;

    private RecyclerView mRecyclerView;
    private ConstraintLayout noNetworkLayout;
    private GridLayoutManager gridLayoutManager;
    private SwipeRefreshLayout swipeRefresh;

    private LinearLayout searchLayout;
    private TextInputEditText searchEntry;
    private boolean searchVisible = false;

    private Animation animFadeIn, animFadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initViews();
        setupListeners();
        defineAnimations();

        //Define Grid size/scale factor
        int columnIndex = displayMetricsUtils.calculateGridColumn(this);
        gridLayoutManager = new GridLayoutManager(this, columnIndex);

        //Check for network
        if (networkUtils.checkNetwork(this)) {
            //Load Movies
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            populateUI();
        } else {
            //Show no connection layout
            mRecyclerView.setVisibility(View.INVISIBLE);
            noNetworkLayout.setVisibility(VISIBLE);
            getRandomNoNetworkView();
            swipeRefresh.setRefreshing(false);
        }
    }

    //Starts listeners for (ViewModel, Swipe to Refresh, Search Entry)
    private void setupListeners() {
        //Set the ViewModel Listener
        movieViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        movieViewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                mAdapter.setMovieCollection(movies);
            }
        });
        //Set the Swipe To Refresh Listener
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkUtils.checkNetwork(MovieActivity.this)) {
                    //Load Movies
                    noNetworkLayout.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(VISIBLE);
                    Log.e(TAG, "onRefresh: load Prefs 167");
                    populateUI();
                } else {
                    //Show no connection layout
                    noNetworkLayout.setVisibility(VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    getRandomNoNetworkView();
                }
            }
        });

        //Set the Search Text Listener
        searchEntry.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (event == null || !event.isShiftPressed()) {
                        queryResult = v.getText().toString();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchEntry.getWindowToken(), 0);
                        }
                        getSharedPreferences();
                        hideSearchMenu();
                        swipeRefresh.setRefreshing(false);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateUI();
    }

    //Method to show the hidden layout for searching the movie database
    private void showSearchMenu() {
        searchLayout.setVisibility(VISIBLE);
        searchVisible = true;
    }
    private void hideSearchMenu() {
        searchLayout.setVisibility(View.INVISIBLE);
        searchVisible = false;
    }

    /*
    * Called when there is no network connection - but checks to make sure that favorites/watchlist
    * aren't selected. Since these movies are stored in the database they should be available
    * regardless of internet connection.
    */
    private void getRandomNoNetworkView() {
        getSharedPreferences();
        if (!sorting.equals(getString(R.string.preference_sort_favorite))
                && !sorting.equals(getString(R.string.preference_sort_watchlist))) {
            TextView noNetworkTextMessageOne = findViewById(R.id.internet_out_message);
            ImageView noNetworkImage = findViewById(R.id.internet_out_image);
            Random randomNetworkGen = new Random();
            int i = randomNetworkGen.nextInt((5 - 1) + 1);
            switch (i) {
                case 1:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_one));
                    noNetworkImage.setImageResource(R.drawable.voldemort);
                    break;
                case 2:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_two));
                    noNetworkImage.setImageResource(R.drawable.wonka);
                    break;
                case 3:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_three));
                    noNetworkImage.setImageResource(R.drawable.lotr);
                    break;
                case 4:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_four));
                    noNetworkImage.setImageResource(R.drawable.taken);
                    break;
                default:
                    noNetworkTextMessageOne.setText(getString(R.string.internet_message_default));
                    noNetworkImage.setImageResource(R.drawable.thanos);
                    break;

            }
            swipeRefresh.setRefreshing(false);
        } else {
            populateUI();
        }
    }

    //Initialize the views in the activity
    private void initViews() {
        mRecyclerView = findViewById(R.id.movie_recycler);
        noNetworkLayout = findViewById(R.id.no_connection_constraint_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        searchLayout = findViewById(R.id.search_layout);
        TextInputLayout searchEntryLayout = findViewById(R.id.search_layout_entry);
        searchEntry = findViewById(R.id.search_entry);
    }

    //Collect SharedPrefs from Activity
    private void getSharedPreferences() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(FlixApplication.getContext());
        //Get the sorting method from shared prefs
        String sortingKey = getString(R.string.preference_sort_key);
        String sortingDefault = getString(R.string.preference_sort_popular);
        sorting = sharedPrefs.getString(sortingKey, sortingDefault);
        //Get the langauge default from shared prefs
        String languageKey = getString(R.string.preference_sort_language_key);
        String languageDefault = getString(R.string.preference_sort_language_all);
        language = sharedPrefs.getString(languageKey, languageDefault);
        //Get filter year from shared prefs
        String filterYearKey = getString(R.string.preference_year_key);
        String filterYearDefault = getString(R.string.preference_year_default);
        filterYear = sharedPrefs.getString(filterYearKey, filterYearDefault);
    }

    //Update the UI views based on preferences
    private void populateUI() {
        getSharedPreferences();
        if (sorting.equals(getString(R.string.preference_sort_favorite))) {
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            loadFavoritesWatchlist(1);
        } else if (sorting.equals(getString(R.string.preference_sort_watchlist))) {
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            loadFavoritesWatchlist(2);
        } else {
            buildPreferencesRecycler(sorting, language, filterYear);
        }
    }

    private void buildPreferencesRecycler(String sorting, String language, String filterYear) {
        List<Movie> movies = new ArrayList<>();
        MovieLoader movieLoader = new MovieLoader();
        movieLoader.execute(sorting, language, filterYear, queryResult);
        try {
            movies = movieLoader.get();
        } catch (ExecutionException e) {
            Log.e(TAG, "buildFavoritesRecycler: execution", e);;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mAdapter = new MovieAdapter(FlixApplication.getContext(), movies, this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        swipeRefresh.setRefreshing(false);
    }

    private void loadFavoritesWatchlist(final int favVal) {
        List<Movie> movies = new ArrayList<>();
        class getWatchlist extends AsyncTask<Void, Void, List<Movie>> {

            @Override
            protected List<Movie> doInBackground(Void... voids) {
                return DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .movieDao().loadFavorites(favVal);
            }

            @Override
            protected void onPostExecute(List<Movie> movies) {
                super.onPostExecute(movies);
            }
        }
        getWatchlist gwl = new getWatchlist();

        try {
            movies = gwl.execute().get();
        } catch (ExecutionException e) {
            Log.e(TAG, "loadWatchList: ", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buildRecyclerWithoutPrefs(movies);
    }

    //Loads the recycler view with data from the database without looking at other shared preferences
    private void buildRecyclerWithoutPrefs(List<Movie> databaseMovies) {
        mAdapter = new MovieAdapter(this, databaseMovies, this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        swipeRefresh.setRefreshing(false);
    }

    //Create Menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_prefs:
                Intent openSettings = new Intent(this, MoviePreferences.class);
                startActivity(openSettings);
                return true;
            case R.id.app_bar_search:
                if (searchVisible) {
                    hideSearchMenu();
                } else {
                    showSearchMenu();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Opens the selected movie in DetailsActivty
    @Override
    public void onClick(View view, Movie movie) {
        Log.e(TAG, "onClick: view clicked: " + view.getId() );
        Intent openDisplayDetails = new Intent(MovieActivity.this, DetailsActivity.class);
        //Put Parcel Extra
        openDisplayDetails.putExtra(MOVIE_KEY, movie.getMovieID());
        startActivity(openDisplayDetails);
    }

    //Displays the Dialog to add film directly to favorite or watchlist (or remove from a list)
    @Override
    public void onLongClick(View view, Movie movie, ImageView hiddenStar) {
        Log.e(TAG, "onLongClick: view clicked: " + view.getId());
        int currentList = movie.getMovieFavorite();
        ImageView hidden = view.findViewById(R.id.hidden_star);
        showSelectionDialog(currentList, movie, hidden);

    }

    //Declares the Animations used in this activity
    private void defineAnimations() {
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out_scale);
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_scale);
    }

    //Creates and customizes the long-click dialog to add/remove/change favorites and watchlist items
    private void showSelectionDialog(int value, final Movie movie, final ImageView hidden) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Which List Should This Be Added To?");
        dialogBuilder.setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                movieViewModel.update(movie, 0);
                hidden.setImageResource(R.drawable.ic_star);
                hidden.setVisibility(VISIBLE);
                hidden.startAnimation(animFadeIn);

                hidden.startAnimation(animFadeOut);
                hidden.setVisibility(View.INVISIBLE);
            }
        });
        dialogBuilder.setNeutralButton("FAVORITES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                movieViewModel.update(movie, 1);
                hidden.setImageResource(R.drawable.ic_star_border);
                hidden.setVisibility(VISIBLE);
                hidden.startAnimation(animFadeIn);
                hidden.startAnimation(animFadeOut);
                hidden.setVisibility(View.INVISIBLE);
            }
        });
        dialogBuilder.setPositiveButton("INTERESTED", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                movieViewModel.update(movie,2);
                hidden.setImageResource(R.drawable.ic_star_interested);
                hidden.setVisibility(VISIBLE);
                hidden.startAnimation(animFadeIn);
                hidden.startAnimation(animFadeOut);
                hidden.setVisibility(View.INVISIBLE);
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorInterested));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorFavorite));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        switch(value) {
            case 0: //NOT ON A LIST SO CANNOT REMOVE WHAT DOESNT EXIST
            default:
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setClickable(false);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText("Not On A List Yet");
                alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorWhiteWash));
                break;
            case 1: //IS A CURRENT FAVORITE
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setClickable(false);
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Already A Favorite");
                alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(getResources().getColor(R.color.colorWhiteWash));
                break;
            case 2: //IS A CURRENT INTEREST
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Already On Watchlist");
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorWhiteWash));
                break;
        }
    }


}

package com.thebaileybrew.flix2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.thebaileybrew.flix2.database.AppDatabase;
import com.thebaileybrew.flix2.database.DatabaseClient;
import com.thebaileybrew.flix2.interfaces.MoviePreferences;
import com.thebaileybrew.flix2.interfaces.adapters.MovieAdapter;
import com.thebaileybrew.flix2.loaders.MovieLoader;
import com.thebaileybrew.flix2.models.Movie;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.view.View.VISIBLE;

public class MovieActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterClickHandler {
    private final static String TAG = MovieActivity.class.getSimpleName();

    private final static String SAVE_STATE = "save_state";
    private final static String RECYCLER_STATE = "recycler_state";
    private final static String MOVIE_KEY = "parcel_movie";
    private final static String SEARCHING = "searching";

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    private Parcelable savedRecyclerState;
    private String queryResult = "";
    private String sorting, language, filterYear;

    private RecyclerView mRecyclerView;
    private List<Movie> movies = new ArrayList<>();
    private ConstraintLayout noNetworkLayout;
    private GridLayoutManager gridLayoutManager;
    private SwipeRefreshLayout swipeRefresh;

    private LinearLayout searchLayout;
    private TextInputEditText searchEntry;
    private boolean searchVisible = false;

    private Animation animScaleDown, animFadeOut;
    private Animation animScaleUp, animFadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initViews();
        defineAnimations();

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
                        loadMoviesFromPrefs();
                        hideSearchMenu();
                        swipeRefresh.setRefreshing(false);
                        return true;
                    }
                }
                return false;
            }
        });

        if (savedInstanceState == null || !savedInstanceState.containsKey(SAVE_STATE)) {
            movies = new ArrayList<>();
        } else {
            savedRecyclerState = savedInstanceState.getParcelable(RECYCLER_STATE);
            movies = savedInstanceState.getParcelableArrayList(SAVE_STATE);
        }

        //Determines the correct number of columns to display depending on the screen orientation
        int columnCount = displayMetricsUtils.calculateGridColumn(this);
        gridLayoutManager = new GridLayoutManager(this, columnCount);
        setSwipeRefreshListener();

        //Check for network
        if (networkUtils.checkNetwork(this)) {
            //Load Movies
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            loadMoviesFromPrefs();
        } else {
            //Show no connection layout
            mRecyclerView.setVisibility(View.INVISIBLE);
            noNetworkLayout.setVisibility(VISIBLE);
            swipeRefresh.setRefreshing(false);
            loadMoviesFromPrefs();
        }

    }
    //Sets up the listener for the SwipeRefreshLayout
    private void setSwipeRefreshListener() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkUtils.checkNetwork(MovieActivity.this)) {
                    //Load Movies
                    noNetworkLayout.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(VISIBLE);
                    loadMoviesFromPrefs();
                } else {
                    //Show no connection layout
                    noNetworkLayout.setVisibility(VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                    getRandomNoNetworkView();

                }
            }
        });
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
            if (sorting.equals(getString(R.string.preference_sort_favorite))) {
                loadFavorites();
            } else {
                loadWatchList();
            }
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

    //Async to load movies from json/api query based on Shared Preferences
    private void loadMoviesFromPrefs() {
        getSharedPreferences();
        Log.e(TAG, "loadMoviesFromPrefs: sort value: " + sorting);
        Log.e(TAG, "loadMoviesFromPrefs: lang value: " + language);
        Log.e(TAG, "loadMoviesFromPrefs: year value: " + filterYear);
        Log.e(TAG, "loadMoviesFromPrefs: srch value: " + queryResult);
        //Determine which view the recycler should load - also retains the recycler on refresh
        if (sorting.equals(getString(R.string.preference_sort_favorite))) {
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            loadFavorites();
        } else if (sorting.equals(getString(R.string.preference_sort_watchlist))) {
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
            loadWatchList();
        } else {
            buildRecycler(sorting, language, filterYear);
        }
    }

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

    //Async to load only WATCHLIST / INTERESTED movies from Database
    private void loadWatchList() {
        class getWatchlist extends AsyncTask<Void, Void, List<Movie>> {

            @Override
            protected List<Movie> doInBackground(Void... voids) {
                return DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .movieDao().loadAllMovies(2);
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

    //Async to load only FAVORITE movies from Database
    private void loadFavorites() {
        class getFavorites extends AsyncTask<Void, Void, List<Movie>> {

            @Override
            protected List<Movie> doInBackground(Void... voids) {
                return DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .movieDao().loadAllMovies(1);
            }

            @Override
            protected void onPostExecute(List<Movie> movies) {
                super.onPostExecute(movies);
            }
        }
        getFavorites gf = new getFavorites();
        try {
            movies = gf.execute().get();
        } catch (ExecutionException e) {
            Log.e(TAG, "loadWatchList: ", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buildRecyclerWithoutPrefs(movies);
    }

    //Loads the recyclerview with data from json pull with shared preferences determining return
    private void buildRecycler(String sorting, String language, String filterYear) {
        MovieAdapter adapter = new MovieAdapter(this, movies,this);
        MovieLoader movieLoader = new MovieLoader(adapter);
        movieLoader.execute(sorting, language, filterYear, queryResult);
        //Run Async to get new movies for/from DB
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerState);
        swipeRefresh.setRefreshing(false);
    }

    //Loads the recycler view with data from the database without looking at other shared preferences
    private void buildRecyclerWithoutPrefs(List<Movie> databaseMovies) {
        MovieAdapter mAdapter = new MovieAdapter(this, databaseMovies, this);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerState);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVE_STATE, (ArrayList)movies);
        //Declare the Recycler State
        outState.putParcelable(RECYCLER_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume(){
        super.onResume();
        getSharedPreferences();
        if (sharedPrefs.contains(SEARCHING)) {
            queryResult = sharedPrefs.getString(SEARCHING, queryResult);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    private void defineAnimations() {
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        animFadeOut = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out_scale);
        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.anim_scale_up);
        animFadeIn = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in_scale);
    }

    private void showSelectionDialog(int value, final Movie movie, final ImageView hidden) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Which List Should This Be Added To?");
        dialogBuilder.setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeFromAllLists(movie);
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
                addToFavorites(movie);
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
                addToInterested(movie);
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

    private void addToInterested(Movie movie) {
        updateDatabase(2, movie);
    }

    private void addToFavorites(Movie movie) {
        updateDatabase(1, movie);
    }

    private void removeFromAllLists(Movie movie) {
        updateDatabase(0, movie);
    }

    private void updateDatabase(final int update, final Movie movie) {
        class UpdateMovieRecord extends AsyncTask<Void, Void, Movie> {
            @Override
            protected Movie doInBackground(Void... voids) {
                movie.setMovieFavorite(update);
                DatabaseClient.getInstance(FlixApplication.getContext()).getAppDatabase()
                        .movieDao().updateMovie(movie);
                return null;
            }

            @Override
            protected void onPostExecute(Movie movie) {
                super.onPostExecute(movie);
            }
        }
        UpdateMovieRecord umr = new UpdateMovieRecord();
        umr.execute();
    }

}

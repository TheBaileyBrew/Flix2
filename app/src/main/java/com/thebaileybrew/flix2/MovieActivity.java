package com.thebaileybrew.flix2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.thebaileybrew.flix2.database.FavoritesViewModel;
import com.thebaileybrew.flix2.interfaces.MoviePreferences;
import com.thebaileybrew.flix2.interfaces.adapters.MovieAdapter;
import com.thebaileybrew.flix2.loaders.MovieLoader;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.displayMetricsUtils;
import com.thebaileybrew.flix2.utils.networkUtils;
import com.thebaileybrew.flix2.utils.objects.RestoringRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.view.View.VISIBLE;

public class MovieActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterClickHandler {
    private final static String TAG = MovieActivity.class.getSimpleName();

    private final static String MOVIE_KEY = "parcel_movie";
    private MovieAdapter mAdapter;

    private String queryResult = "";
    private String sorting, language, filterYear;

    private RestoringRecyclerView mRecyclerView;
    private ConstraintLayout noNetworkLayout;
    private GridLayoutManager gridLayoutManager;
    private SwipeRefreshLayout swipeRefresh;

    private LinearLayout searchLayout;
    private TextInputEditText searchEntry;
    private boolean searchVisible = false;
    private List<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initViews();
        setupListeners();

        //Define Grid size/scale factor
        int columnIndex = displayMetricsUtils.calculateGridColumn(this);
        gridLayoutManager = new GridLayoutManager(FlixApplication.getContext(), columnIndex);
        populateUI();

        if (savedInstanceState == null) {
            movies = new ArrayList<>();
            Log.e(TAG, "onCreate: new arraylist");
        }

        //Check for network
        if (networkUtils.checkNetwork(this)) {
            //Load Movies
            noNetworkLayout.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(VISIBLE);
        } else {
            //Show no connection layout
            mRecyclerView.setVisibility(View.INVISIBLE);
            noNetworkLayout.setVisibility(VISIBLE);
            getRandomNoNetworkView();
            swipeRefresh.setRefreshing(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateUI();
    }

    //Starts listeners for (ViewModel, Swipe to Refresh, Search Entry)
    private void setupListeners() {
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
                        queryResult = v.getText().toString().trim();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(searchEntry.getWindowToken(), 0);
                        }
                        getSharedPreferences();

                        hideSearchMenu();
                        v.setText("");
                        swipeRefresh.setRefreshing(false);
                        return true;
                    }
                }
                return false;
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
        if (!sorting.equals(getString(R.string.preference_sort_favorite))) {
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
            Log.e(TAG, "populate UI: 200");
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
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(FlixApplication.getContext());
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
        if (TextUtils.isEmpty(queryResult)) {
            Log.e(TAG, "populateUI: query empty");
            if (sorting.equals(getString(R.string.preference_sort_favorite))) {
                noNetworkLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(VISIBLE);
                buildRecyclerWithoutPrefs();
            } else {
                buildPreferencesRecycler();
            }
        } else {
            Log.e(TAG, "populateUI: query has data");
            buildRecyclerForSearch();
        }

    }

    private void buildPreferencesRecycler() {
        mAdapter = new MovieAdapter(FlixApplication.getContext(), movies, this);
        MovieLoader movieLoader = new MovieLoader(mAdapter);
        movieLoader.execute(sorting, language, filterYear, queryResult);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);

    }

    private void setupViewModel() {
        FavoritesViewModel viewModel = ViewModelProviders.of(this).get(FavoritesViewModel.class);
        viewModel.getFavorites().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> favMovies) {
                if(favMovies.size()>0) {
                    mAdapter.updateMovieList(favMovies);
                }
            }
        });
    }



    //Loads the recycler view with data from the database without looking at other shared preferences
    private void buildRecyclerWithoutPrefs() {
        mAdapter = new MovieAdapter(this, movies, this);
        setupViewModel();
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        swipeRefresh.setRefreshing(false);
    }

    //Loads the recycler view with data from the database without looking at other shared preferences
    private void buildRecyclerForSearch() {
        mAdapter = new MovieAdapter(FlixApplication.getContext(), movies, this);
        MovieLoader movieLoader = new MovieLoader(mAdapter);
        movieLoader.execute(sorting, language, filterYear, queryResult);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
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
        openDisplayDetails.putExtra(MOVIE_KEY, movie);
        startActivity(openDisplayDetails);
    }


    public void setQueryResult() {
        queryResult = "";
    }


}

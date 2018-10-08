package com.thebaileybrew.flix2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.thebaileybrew.flix2.interfaces.adapters.MovieAdapter;
import com.thebaileybrew.flix2.loaders.MovieLoader;
import com.thebaileybrew.flix2.models.Movie;
import com.thebaileybrew.flix2.utils.displayMetricsUtils;
import com.thebaileybrew.flix2.utils.networkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.view.View.VISIBLE;

public class MovieActivity extends AppCompatActivity {
    private final static String TAG = MovieActivity.class.getSimpleName();

    private final static String SAVE_STATE = "save_state";
    private final static String RECYCLER_STATE = "recycler_state";
    private final static String MOVIE_KEY = "parcel_movie";

    private Parcelable savedRecyclerState;
    private String queryResult = "";

    private RecyclerView mRecyclerView;
    private MovieAdapter adapter;
    private ArrayList<Movie> movies = new ArrayList<>();
    private ConstraintLayout noNetworkLayout;
    private GridLayoutManager gridLayoutManager;
    private SwipeRefreshLayout swipeRefresh;

    private LinearLayout searchLayout;
    private TextInputEditText searchEntry;
    private boolean searchVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        initViews();

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
                        swipeRefresh.setRefreshing(true);
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
        }

        int columnCount = displayMetricsUtils.calculateGridColumn(this);
        gridLayoutManager = new GridLayoutManager(this, columnCount);
        setSwipeRefreshListener();

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
        }

    }

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

    private void showSearchMenu() {
        searchLayout.setVisibility(VISIBLE);
        searchVisible = true;
    }

    private void hideSearchMenu() {
        searchLayout.setVisibility(View.INVISIBLE);
        searchVisible = false;
    }

    private void getRandomNoNetworkView() {
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
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.movie_recycler);
        noNetworkLayout = findViewById(R.id.no_connection_constraint_layout);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        searchLayout = findViewById(R.id.search_layout);
        TextInputLayout searchEntryLayout = findViewById(R.id.search_layout_entry);
        searchEntry = findViewById(R.id.search_entry);
    }

    private void loadMoviesFromPrefs() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String sortingKey = getString(R.string.preference_sort_key);
        String sortingDefault = getString(R.string.preference_sort_popular);
        String sorting = sharedPrefs.getString(sortingKey, sortingDefault);

        String languageKey = getString(R.string.preference_sort_language_key);
        String languageDefault = getString(R.string.preference_sort_language_all);
        String language = sharedPrefs.getString(languageKey, languageDefault);

        String filterYearKey = getString(R.string.preference_year_key);
        String filterYearDefault = getString(R.string.preference_year_default);
        String filterYear = sharedPrefs.getString(filterYearKey, filterYearDefault);

        MovieLoader movieLoader = new MovieLoader();
        movieLoader.execute(sorting, language, filterYear, queryResult);
        getMovies();

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setHasFixedSize(true);


        mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerState);
        swipeRefresh.setRefreshing(false);




    }

    private void getMovies() {
        class GetMovies extends AsyncTask<Void, Void, List<Movie>> implements MovieAdapter.MovieAdapterClickHandler {

            @Override
            protected List<Movie> doInBackground(Void... voids) {
                Log.e(TAG, "doInBackground: loading movies");
                return DatabaseClient.getInstance(MovieActivity.this).getAppDatabase()
                        .movieDao().loadAllMovies();

            }

            @Override
            protected void onPostExecute(List<Movie> movies) {
                super.onPostExecute(movies);
                adapter = new MovieAdapter(MovieActivity.this, movies,this);
                mRecyclerView.setAdapter(adapter);

            }

            @Override
            public void onClick(View view, Movie movie) {
                Intent openDisplayDetails = new Intent(MovieActivity.this, DetailsActivity.class);
                //Put Parcel Extra
                openDisplayDetails.putExtra(MOVIE_KEY, movie);

                startActivity(openDisplayDetails);
            }
        }

        GetMovies gm = new GetMovies();
        gm.execute();
    }
}

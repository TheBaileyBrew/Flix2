package com.thebaileybrew.flix2.database;

import com.thebaileybrew.flix2.models.Movie;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MovieDao {
    @Query("SELECT * FROM allmovies WHERE favorite = :favValue ORDER BY movie_id")
    List<Movie> loadAllMovies(int favValue);

    @Query("SELECT * FROM allmovies WHERE movie_id = :movieID")
    Movie loadSingleMovies(int movieID);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMovie(Movie movie);

    @Insert
    void insertAllMovies(Movie... movie);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(Movie movie);

    @Delete
    void deleteMovie(Movie movie);
}

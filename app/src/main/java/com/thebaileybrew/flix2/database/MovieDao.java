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
    @Query("SELECT * FROM themovies ORDER BY movie_id")
    List<Movie> loadAllMovies();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMovie(Movie movie);

    @Insert
    void insertAllMovies(Movie... movie);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(Movie movie);

    @Delete
    void deleteMovie(Movie movie);
}

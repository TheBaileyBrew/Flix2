package com.thebaileybrew.flix2.database;

import com.thebaileybrew.flix2.models.Film;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface FilmDao {

    @Query("SELECT * FROM filmdetails ORDER BY movie_id")
    List<Film> loadAllFilms();

    @Query("SELECT * FROM filmdetails WHERE movie_id = :movieID")
    List<Film> loadSingleFilm(String movieID);

    @Insert (onConflict = OnConflictStrategy.IGNORE)
    void insertFilm(Film film);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFilm(Film film);

    @Delete
    void deleteFilm(Film film);
}


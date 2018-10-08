package com.thebaileybrew.flix2.database;

import com.thebaileybrew.flix2.models.Credit;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CreditDao {

    @Query("SELECT * FROM moviecredits ORDER BY movie_id")
    List<Credit> loadAllCredits();

    @Query("SELECT * FROM moviecredits WHERE movie_id = :movieID")
    List<Credit> loadSingleFilmCredit(String movieID);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCredit(Credit credit);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCredit(Credit credit);

    @Delete
    void deleteCredit(Credit credit);
}

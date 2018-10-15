package com.thebaileybrew.flix2.database;


import android.content.Context;
import android.util.Log;

import com.thebaileybrew.flix2.models.Credit;
import com.thebaileybrew.flix2.models.Film;
import com.thebaileybrew.flix2.models.Movie;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = Movie.class, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{

    public abstract MovieDao movieDao();
}

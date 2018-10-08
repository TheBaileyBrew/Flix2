package com.thebaileybrew.flix2.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

public class DatabaseClient {
    private static final String TAG = AppDatabase.class.getSimpleName();

    private Context mContext;
    private static DatabaseClient mInstance;

    private AppDatabase appDatabase;
    private static final String DATABASE_NAME = "forthemovies";

    private DatabaseClient(Context mContext) {
        this.mContext = mContext;

        appDatabase = Room.databaseBuilder(mContext, AppDatabase.class, DATABASE_NAME).build();
    }

    public static synchronized DatabaseClient getInstance(Context mContext) {
        if (mInstance == null) {
            mInstance = new DatabaseClient(mContext);
        }
        return mInstance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }
}
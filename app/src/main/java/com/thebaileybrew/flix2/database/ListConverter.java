package com.thebaileybrew.flix2.database;

import com.thebaileybrew.flix2.models.Movie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import androidx.room.TypeConverter;

public class ListConverter {
    @TypeConverter
    public static ArrayList<Movie> toArrayList(List<Movie> movies) {
        return movies == null ? null : new ArrayList<Movie>();
    }

    @TypeConverter
    public static String toList(ArrayList<Movie> movies) {
        return movies.toString();
    }
}

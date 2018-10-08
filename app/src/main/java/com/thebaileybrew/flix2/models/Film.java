package com.thebaileybrew.flix2.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "filmdetails", indices = {@Index("movie_id")},
        foreignKeys = @ForeignKey(entity = Movie.class, parentColumns = "movie_id", childColumns = "movie_id",
                onDelete = CASCADE))
public class Film {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int filmID;

    @ColumnInfo(name = "movie_id")
    private String movieID;

    @ColumnInfo(name = "movie_tag_line")
    private String movieTagLine;

    @ColumnInfo(name = "movie_runtime")
    private int movieRuntime;

    @ColumnInfo(name = "movie_genre")
    private String movieGenre;


    public int getFilmID() {
        return filmID;
    }

    public void setFilmID(int filmID) {
        this.filmID = filmID;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getMovieTagLine() {
        return movieTagLine;
    }

    public void setMovieTagLine(String movieTagLine) {
        this.movieTagLine = movieTagLine;
    }

    public int getMovieRuntime() {
        return movieRuntime;
    }

    public void setMovieRuntime(int movieRuntime) {
        this.movieRuntime = movieRuntime;
    }

    public String getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(String movieGenre) {
        this.movieGenre = movieGenre;
    }
}


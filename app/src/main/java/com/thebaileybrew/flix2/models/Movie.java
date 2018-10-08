package com.thebaileybrew.flix2.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "themovies")
public class Movie implements Parcelable{
    //This class model is for the basic details of the film

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "movie_id")
    private int movieID;

    @ColumnInfo(name = "movie_vote_count")
    private int movieVoteCount;

    @ColumnInfo(name = "movie_vote_average")
    private double movieVoteAverage;

    @ColumnInfo(name = "movie_title")
    private String movieTitle;

    @ColumnInfo(name = "movie_popularity")
    private double moviePopularity;

    @ColumnInfo(name = "movie_language")
    private String movieLanguage;

    @ColumnInfo(name = "movie_poster_path")
    private String moviePosterPath;

    @ColumnInfo(name = "movie_backdrop")
    private String movieBackdrop;

    @ColumnInfo(name = "movie_overview")
    private String movieOverview;

    @ColumnInfo(name = "movie_release_date")
    private String movieReleaseDate;

    public Movie() {}

    private Movie(Parcel in) {
        movieID = in.readInt();
        movieVoteCount = in.readInt();
        movieVoteAverage = in.readDouble();
        movieTitle = in.readString();
        moviePopularity = in.readLong();
        movieLanguage = in.readString();
        moviePosterPath = in.readString();
        movieBackdrop = in.readString();
        movieOverview = in.readString();
        movieReleaseDate = in.readString();
    }

    public int getMovieID() {
        return movieID;
    }

    public void setMovieID(int movieID) {
        this.movieID = movieID;
    }

    public int getMovieVoteCount() {
        return movieVoteCount;
    }

    public void setMovieVoteCount(int movieVoteCount) {
        this.movieVoteCount = movieVoteCount;
    }

    public double getMovieVoteAverage() {
        return movieVoteAverage;
    }

    public void setMovieVoteAverage(double movieVoteAverage) {
        this.movieVoteAverage = movieVoteAverage;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public double getMoviePopularity() {
        return moviePopularity;
    }

    public void setMoviePopularity(double moviePopularity) {
        this.moviePopularity = moviePopularity;
    }

    public String getMovieLanguage() {
        return movieLanguage;
    }

    public void setMovieLanguage(String movieLanguage) {
        this.movieLanguage = movieLanguage;
    }

    public String getMoviePosterPath() {
        return moviePosterPath;
    }

    public void setMoviePosterPath(String moviePosterPath) {
        this.moviePosterPath = moviePosterPath;
    }

    public String getMovieBackdrop() {
        return movieBackdrop;
    }

    public void setMovieBackdrop(String movieBackdrop) {
        this.movieBackdrop = movieBackdrop;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    //Parcelable implementation

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(movieID);
        parcel.writeInt(movieVoteCount);
        parcel.writeDouble(movieVoteAverage);
        parcel.writeString(movieTitle);
        parcel.writeDouble(moviePopularity);
        parcel.writeString(movieLanguage);
        parcel.writeString(moviePosterPath);
        parcel.writeString(movieBackdrop);
        parcel.writeString(movieOverview);
        parcel.writeString(movieReleaseDate);
    }
}

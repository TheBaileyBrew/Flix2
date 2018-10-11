package com.thebaileybrew.flix2.models;


public class Film {

    private String movieTagLine;
    private int movieRuntime;
    private String movieGenre;

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


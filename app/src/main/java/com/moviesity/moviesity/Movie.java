package com.moviesity.moviesity;

public class Movie extends Object {
    private String poster_path;
    private String original_title;
    private String plot_synopsis;   // called overview in the api
    private String user_rating;     // called vote_average in the api
    private String release_date;

    public Movie(String poster_path, String original_title, String plot_synopsis,
                 String user_rating, String release_date) {
        this.poster_path = poster_path;
        this.original_title = original_title;
        this.plot_synopsis = plot_synopsis;
        this.user_rating = user_rating;
        this.release_date = release_date;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public String getPlot_synopsis() {
        return plot_synopsis;
    }

    public String getUser_rating() {
        return user_rating;
    }

    public String getRelease_date() {
        return release_date;
    }
}

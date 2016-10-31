package com.moviesity.moviesity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.net.MalformedURLException;
import java.net.URL;

public class Movie extends Object implements Parcelable {
    public static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w300";
    private static String LOG_TAG = Movie.class.getSimpleName();

    private String posterPath;
    private String originalTitle;
    private String plotSynopsis;   // called overview in the api
    private String userRating;     // called vote_average in the api
    private String releaseDate;

    public Movie(String posterPath, String originalTitle, String plotSynopsis,
                 String userRating, String releaseDate) {
        this.posterPath = posterPath;
        this.originalTitle = originalTitle;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public URL getPosterFullUrl() throws MalformedURLException {
        Uri.Builder uri = new Uri.Builder();
        uri.encodedPath(IMAGE_BASE_URL).appendEncodedPath(this.getPosterPath()).build();
        return new URL(uri.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.posterPath);
        dest.writeString(this.originalTitle);
        dest.writeString(this.plotSynopsis);
        dest.writeString(this.userRating);
        dest.writeString(this.releaseDate);
    }

    protected Movie(Parcel in) {
        this.posterPath = in.readString();
        this.originalTitle = in.readString();
        this.plotSynopsis = in.readString();
        this.userRating = in.readString();
        this.releaseDate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}

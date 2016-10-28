package com.moviesity.moviesity;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DiscoveryFragment extends Fragment {

    public DiscoveryFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_discovery, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new FetchMovieTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discovery, container, false);
    }

    public class FetchMovieTask extends AsyncTask<Void, Void, String[]> {

        private String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(Void... voids) {

            // Both put outside try/catch block so that they can be closed in finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            // try to get the movie data from API
            try {
                // Example API Request: https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=c52dcf21b2d2216733871b9ab0aa00e4
                final String MOVIE_DISCOVER_BASE_URL =
                        "https://api.themoviedb.org/3/discover/movie";
                final String SORT_BY_PARAM = "sort_by";
                final String SORT_BY_VALUE = "popularity.desc";     // Rating: vote_average.desc
                final String API_KEY_PARAM = "api_key";

                Uri.Builder uri = new Uri.Builder();
                uri.encodedPath(MOVIE_DISCOVER_BASE_URL)
                        .appendQueryParameter(SORT_BY_PARAM, SORT_BY_VALUE)
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.THEMOVIEDB_ORG_API_KEY)
                        .build();

                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) return null;
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) return null;
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "movieJsonStr: " + movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) urlConnection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream: " + e.getMessage(), e);
                    }
                }
            }
            return null;
        }
    }
}

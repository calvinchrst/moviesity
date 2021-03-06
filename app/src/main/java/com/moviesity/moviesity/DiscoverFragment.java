package com.moviesity.moviesity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private DiscoverAdapter mDiscoverAdapter;
    private ArrayList<Movie> movieArrayList;

    public DiscoverFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        movieArrayList = new ArrayList<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_discover, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);

        // Setup gridView adapter
        mDiscoverAdapter = new DiscoverAdapter(getActivity(), R.layout.grid_item_discover,
                R.id.grid_item_discover_image_view, new ArrayList<Movie>());
        GridView discoverGridView = (GridView) rootView.findViewById(R.id.grid_view_discover);
        discoverGridView.setAdapter(mDiscoverAdapter);

        discoverGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("parcelable_extra", movieArrayList.get(i));
                startActivity(detailActivityIntent);
            }
        });

        return rootView;
    }

    private void updateMovie() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortValue = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_key_popular));
        new FetchMovieTask().execute(sortValue);
    }

    public class FetchMovieTask extends AsyncTask<String, String, ArrayList<Movie>> {

        private String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private boolean isOnline() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... strings) {

            // Check Connection
            if (! isOnline()) {
                publishProgress("Network is not connected");
                return null;
            }

            // Both put outside try/catch block so that they can be closed in finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            // Example API Request: https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=c52dcf21b2d2216733871b9ab0aa00e4
            final String MOVIE_DISCOVER_BASE_URL =
                    "https://api.themoviedb.org/3/discover/movie";
            final String SORT_BY_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";
            final String SORT_BY_VALUE;
            if (strings[0].equals(getString(R.string.pref_sort_key_popular))) {
                SORT_BY_VALUE = "popularity.desc";
            } else if (strings[0].equals(getString(R.string.pref_sort_key_rating))) {
                SORT_BY_VALUE = "vote_average.desc";
            } else {
                Log.e(LOG_TAG, "Unknow sorting_value " + strings[0]);
                return null;
            }

            // try to get the movie data from API
            try {
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
                //Log.v(LOG_TAG, "movieJsonStr: " + movieJsonStr);
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

            // code will only get here if no error caught
            // extract JSON
            try {
                movieArrayList = getMovieDataFromJson(movieJsonStr);
                return movieArrayList;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Exception: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Snackbar snackbar = Snackbar.make(
                    getActivity().findViewById(R.id.main_activity_coordinator_layout),
                    values[0],
                    Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {
            if (movieList != null) {
                mDiscoverAdapter.clear();
                mDiscoverAdapter.addAll(movieList);
            }
        }

        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {
            ArrayList<Movie> movieList = new ArrayList<Movie>();

            // names of JSON ojects that need to be extracted
            final String results = "results";
            final String poster_path = "poster_path";
            final String original_title = "original_title";
            final String plot_synopsis = "overview";
            final String user_rating = "vote_average";
            final String release_date = "release_date";

            JSONObject rootJSON = new JSONObject(movieJsonStr);
            JSONArray resultsArray = rootJSON.getJSONArray(results);
            int num_of_results = resultsArray.length();
            JSONObject movieJson;
            for (int i=0; i < num_of_results; i++) {
                movieJson = resultsArray.getJSONObject(i);
                String poster_path_value = movieJson.getString(poster_path);
                String original_title_value = movieJson.getString(original_title);
                String plot_synopsis_value = movieJson.getString(plot_synopsis);
                String user_rating_value = movieJson.getString(user_rating);
                String release_date_value = movieJson.getString(release_date);
                Movie aMovie = new Movie(poster_path_value, original_title_value,
                        plot_synopsis_value, user_rating_value, release_date_value);
                movieList.add(aMovie);
            }
            return movieList;
        }
    }

    // TODO: Set Image Size to fill the screen (Currently there's blank space on the poster display)

    private class DiscoverAdapter extends ArrayAdapter<Movie> {

        private String LOG_TAG = DiscoverAdapter.class.getSimpleName();

        // Example Movie Request http://image.tmdb.org/t/p/w185//IfB9hy4JH1eH6HEfIgIGORXi5h.jpg

        public DiscoverAdapter(Context context, int resource, int imageViewResourceId, List<Movie> movieList) {
            super(context, resource, imageViewResourceId, movieList);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) imageView = new ImageView(this.getContext());
            else imageView = (ImageView) convertView;

            try {
                URL url = getItem(position).getPosterFullUrl();
                Picasso.with(getActivity())
                        .load(url.toString())
                        .into(imageView);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return imageView;
        }
    }

}

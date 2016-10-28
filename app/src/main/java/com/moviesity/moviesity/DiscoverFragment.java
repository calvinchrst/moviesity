package com.moviesity.moviesity;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class DiscoverFragment extends Fragment {

    private DiscoverAdapter mDiscoverAdapter;

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
                R.id.grid_item_discover_image_view, new ArrayList<String>());
        GridView discoverGridView = (GridView) rootView.findViewById(R.id.grid_view_discover);
        discoverGridView.setAdapter(mDiscoverAdapter);

        return rootView;
    }

    private void updateMovie() {
        new FetchMovieTask().execute();
    }

    public class FetchMovieTask extends AsyncTask<Void, Void, ArrayList<Movie>> {

        private String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected ArrayList<Movie> doInBackground(Void... voids) {

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

            // code will only get here if no error caught
            // extract JSON
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSON Exception: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movieList) {
            if (movieList != null) {
                mDiscoverAdapter.clear();

                ArrayList<String> poster_path_list = new ArrayList<>();
                // update Adapter with poster path only
                for (Movie aMovie : movieList) {
                    poster_path_list.add(aMovie.getPoster_path());
                }
                mDiscoverAdapter.addAll(poster_path_list);
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

    private class DiscoverAdapter extends ArrayAdapter<String> {

        private String LOG_TAG = DiscoverAdapter.class.getSimpleName();

        private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w300";
        // Example Movie Request http://image.tmdb.org/t/p/w185//IfB9hy4JH1eH6HEfIgIGORXi5h.jpg

        public DiscoverAdapter(Context context, int resource, int imageViewResourceId, List<String> strings) {
            super(context, resource, imageViewResourceId, strings);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) imageView = new ImageView(this.getContext());
            else imageView = (ImageView) convertView;

            // build url to fetch image
            Uri.Builder uri = new Uri.Builder();
            uri.encodedPath(IMAGE_BASE_URL).appendEncodedPath(getItem(position)).build();
            try {
                URL url = new URL(uri.toString());
                Picasso.with(getActivity())
                        .load(url.toString())
                        .into(imageView);               // TODO: Set Image Size to fill the screen
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return imageView;
        }
    }

}

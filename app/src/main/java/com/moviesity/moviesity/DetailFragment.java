package com.moviesity.moviesity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static String LOG_TAG = DetailFragment.class.getSimpleName();

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Update View based on intent data (movie)
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("parcelable_extra")) {
            Movie aMovie = intent.getParcelableExtra("parcelable_extra");
            String releaseDateText = getString(R.string.release_date_label) + ": " +
                    aMovie.getReleaseDate();
            String ratingText = getString(R.string.rating_label) + ": " +
                    aMovie.getUserRating();
            ((TextView) rootView.findViewById(R.id.titleTextView)).
                    setText(aMovie.getOriginalTitle());
            ((TextView) rootView.findViewById(R.id.ratingTextView)).
                    setText(ratingText);
            ((TextView) rootView.findViewById(R.id.releaseDateTextView)).
                    setText(releaseDateText);
            ((TextView) rootView.findViewById(R.id.synopsisTextView)).
                    setText(aMovie.getPlotSynopsis());
            ImageView posterImageView = (ImageView) rootView.findViewById(R.id.posterImageView);

            try {
                URL posterFullUrl = aMovie.getPosterFullUrl();
                Picasso.with(getActivity()).load(posterFullUrl.toString()).into(posterImageView);
            } catch (MalformedURLException m) {
                Log.e(LOG_TAG, m.getMessage(), m);
            }
        }

        return rootView;
    }
}

package com.example.shashankkalra.popularmovies.viewsfragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.shashankkalra.popularmovies.R;
import com.example.shashankkalra.popularmovies.activities.ReviewActivity;
import com.example.shashankkalra.popularmovies.adapters.TrailerAdapter;
import com.example.shashankkalra.popularmovies.entities.MovieVO;
import com.example.shashankkalra.popularmovies.entities.TrailerVO;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MovieDetailFragment extends Fragment {

    @Bind(R.id.poster)ImageView poster;
    @Bind(R.id.name) TextView name;
    @Bind(R.id.trailer) TextView trailer;
    @Bind(R.id.overview)TextView overview;
    @Bind(R.id.release)TextView releaseDate;
    @Bind(R.id.rating)TextView ratingText;
    @Bind(R.id.ratingBar) RatingBar ratingBar;
    @Bind(R.id.trailerList) NonScrollableListView trailerListView;
    @Bind(R.id.movieId) TextView movieId;
    @Bind(R.id.fav) Switch fav;

    private static final String LOG_TAG = "Movie_Detail";
    List<TrailerVO> trailersList;
    TrailerAdapter trailerAdapter;

    MovieVO clickedMovie=null;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(clickedMovie!=null) {
            new PopulateTrailers().execute(clickedMovie.getId());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_movie_detail,container,false);
        ButterKnife.bind(this, view);


        trailersList = new ArrayList<>();

        Bundle arguments=getArguments();
        clickedMovie=arguments.getParcelable("movie");
        if(clickedMovie!=null) {
            name.setText(clickedMovie.getName());
            overview.setText(clickedMovie.getOverview());
            movieId.setText(clickedMovie.getId());
            releaseDate.setText("Release Date : " + clickedMovie.getReleaseDate());
            ratingText.setText("Rating :" + Double.toString(clickedMovie.getRating()));
            Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w780/" + clickedMovie.getImageUrl()).resize(700,780).into(poster);

            ratingBar.setRating(((float) clickedMovie.getRating() / 2));
            ratingBar.setIsIndicator(true);
            ratingBar.setStepSize(0.01f);
            ratingBar.setContentDescription("Rating : " + clickedMovie.getRating());

            preferences = getActivity().getSharedPreferences("favs", Context.MODE_APPEND);
            Set<String> favs = preferences.getStringSet("favs",null);
            if(favs != null && favs.contains(clickedMovie.getName()))
                fav.setChecked(true);

            fav.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {

                    preferencesEditor = preferences.edit();
                    if (isChecked) {
                        Set<String> favs = preferences.getStringSet("favs",null);
                        if(favs == null)
                            favs = new HashSet<String>();
                        if(!favs.contains(name.getText().toString()))
                            favs.add(name.getText().toString());
                        preferencesEditor.putStringSet("favs",favs);
                    } else {
                        Set<String> favs = preferences.getStringSet("favs",null);
                        if(favs != null && favs.contains(name.getText().toString()))
                            favs.remove(name.getText().toString());
                        preferencesEditor.putStringSet("favs", favs);
                    }

                    preferencesEditor.commit();

                }
            });

            trailerAdapter = new TrailerAdapter(getActivity(), R.layout.trailer_item, new ArrayList<TrailerVO>());
            trailerListView.setAdapter(trailerAdapter);
            PopulateTrailers trailers = new PopulateTrailers();
            trailers.execute(clickedMovie.getId());
            setListViewHeightBasedOnChildren(trailerListView);
        }

        //TextView name = (TextView) view.findViewById(R.id.name);
        //name.setText(getIntent().getExtras().getString("name"));
        //TextView overview = (TextView) findViewById(R.id.overview);
        //overview.setText(getIntent().getExtras().getString("overview"));
        //TextView releaseDate = (TextView) findViewById(R.id.release);
        //releaseDate.setText("Release Date : " + getIntent().getExtras().getString("releaseDate"));

        //TextView rating = (TextView) findViewById(R.id.rating);
        //rating.setText("Rating : " + getIntent().getExtras().getDouble("rating"));

        //RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
//        ratingBar.setRating(((float) getIntent().getExtras().getDouble("rating") / 2));
//        ratingBar.setIsIndicator(true);
//        ratingBar.setStepSize(0.01f);
//        ratingBar.setContentDescription("Rating : " + getIntent().getExtras().getDouble("rating"));

//        ImageView poster = (ImageView) findViewById(R.id.poster);
//        String imageURL = "http://image.tmdb.org/t/p/w780/" + getIntent().getExtras().getString("url");
//        Picasso.with(this).load(imageURL).resize(700, 780).into(poster);

//        TextView movieId = (TextView) findViewById(R.id.movieId);
//        movieId.setText(getIntent().getExtras().getString("id"));

//        Switch favOrNot = (Switch) findViewById(R.id.fav);

    return view;

    }

    public class PopulateTrailers extends AsyncTask<String,Void,List<TrailerVO>> {
        HttpURLConnection urlConnection;
        BufferedReader bufferedReader;
        String movies_detail=null;
        @Override
        protected List<TrailerVO> doInBackground(String... params) {
            final String TRAILER_URL="http://api.themoviedb.org/3/movie/"+params[0]+"/videos?api_key="+getString(R.string.api_key);
            //Please enter your own API KEY in place of <YOUR_API_KEY> in below line before building the code
            try{
                URL url=new URL(TRAILER_URL);
                urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream=urlConnection.getInputStream();
                if(inputStream==null){
                    return  null;
                }
                bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer buffer=new StringBuffer();
                String line;
                while((line=bufferedReader.readLine())!=null){
                    buffer.append(line);
                }
                if(buffer.length()==0){
                    return null;
                }
                movies_detail=buffer.toString();
            }catch(Exception e){
                Log.e(LOG_TAG, e.getMessage());
            }finally {
                if(urlConnection!=null){
                    urlConnection.disconnect();
                }
                try{
                    if(bufferedReader!=null){
                        bufferedReader.close();
                    }
                }catch (Exception e){
                    Log.e(LOG_TAG,"Unable to close reader");
                }
            }
            return getTrailerData(movies_detail);
        }

        private List<TrailerVO> getTrailerData(String movies_detail) {
            final String RESULTS = "results";
            final String KEY = "key";
            final String NAME = "name";
            final String SITE = "site";
            List<TrailerVO> trailers=new ArrayList<>();
            try {
                JSONObject movieJson = new JSONObject(movies_detail);
                JSONArray trailerJSONArray=movieJson.getJSONArray(RESULTS);
                for(int i=0;i<trailerJSONArray.length();i++){
                    JSONObject trailerObject=trailerJSONArray.getJSONObject(i);
                    TrailerVO temp_trailerVO =new TrailerVO();
                    temp_trailerVO.setName(trailerObject.getString(NAME));
                    temp_trailerVO.setKey(trailerObject.getString(KEY));
                    if("YouTube".equalsIgnoreCase(trailerObject.getString(SITE)))
                        trailers.add(temp_trailerVO);
                }
            }catch (Exception e){
                Log.e(LOG_TAG,e.getMessage());
            }
            return trailers;
        }
        @Override
        protected void onPostExecute(List<TrailerVO> trailers) {
            if(trailers!=null) {
                trailersList.clear();
                trailerAdapter.clear();
                for(TrailerVO trailer: trailers) {
                    trailersList.add(trailer);
                }
                trailerAdapter.addAll(trailers);
                trailerAdapter.notifyDataSetChanged();
            }
        }

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void loadReviews(View view){
        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        String movieName = name.getText().toString();
        String movieID = movieId.getText().toString();
        intent.putExtra(getString(R.string.movie_name_key), movieName);
        intent.putExtra(getString(R.string.movie_id_key), movieID);
        startActivity(intent);
    }
}

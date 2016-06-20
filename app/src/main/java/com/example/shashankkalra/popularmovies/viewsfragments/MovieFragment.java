package com.example.shashankkalra.popularmovies.viewsfragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.shashankkalra.popularmovies.R;
import com.example.shashankkalra.popularmovies.activities.MainActivity;
import com.example.shashankkalra.popularmovies.adapters.MovieAdapter;
import com.example.shashankkalra.popularmovies.entities.MovieVO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MovieFragment extends Fragment {
    String LOG_TAG="sk";
    MovieAdapter madapter;
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
        fetchMovieData();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getActivity().getSystemService(getContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchMovieData(){
        if(isNetworkAvailable()) {
            preferences = getActivity().getSharedPreferences("favs",Context.MODE_APPEND);
            String sortPreference = preferences.getString("sort_by","popular");
            String favs = String.valueOf(preferences.getBoolean("onlyfavs",false));
            PopulateMovie populateMovie = new PopulateMovie();
            populateMovie.execute(sortPreference,favs);
        }else {
            Toast.makeText(getContext(),"Unable to retrieve movie details. Check your internet connection.",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.sorting_options,menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        preferences = getActivity().getSharedPreferences("favs",Context.MODE_APPEND);
        preferencesEditor = preferences.edit();

        if(item.getItemId()==R.id.sort_by_popularity){
            preferencesEditor.putString("sort_by","popular");
            preferencesEditor.putBoolean("onlyfavs",false);
        }
        if(item.getItemId()==R.id.sort_by_rating){
            preferencesEditor.putString("sort_by","top_rated");
            preferencesEditor.putBoolean("onlyfavs",false);
        }
        if(item.getItemId()==R.id.favourites){
            preferencesEditor.putBoolean("onlyfavs",true);
        }

        preferencesEditor.commit();

        fetchMovieData();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        madapter = new MovieAdapter(getActivity(), R.layout.list_item, new ArrayList<MovieVO>());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.grids);
        gridView.setAdapter(madapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieVO movie = (MovieVO) parent.getItemAtPosition(position);
                if (movie != null) {
                    ((MainActivity) getActivity()).onItemSelected(movie);
                }

            }
        });
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MovieVO movie = (MovieVO) parent.getItemAtPosition(position);
//                Intent i = new Intent(getContext(), MovieDetailActivity.class);
//                i.putExtra("name", movie.getName());
//                i.putExtra("url",movie.getImageUrl());
//                i.putExtra("rating", movie.getRating());
//                i.putExtra("releaseDate", movie.getReleaseDate());
//                i.putExtra("overview", movie.getOverview());
//                i.putExtra("id",movie.getId());
//                startActivity(i);
//            }
//        });
        fetchMovieData();
        return  rootView;
    }

    public class PopulateMovie extends AsyncTask<String,Void,List<MovieVO>> {
        HttpURLConnection urlConnection;
        BufferedReader bufferedReader;
        String movies_detail=null;
        @Override
        protected List<MovieVO> doInBackground(String... params) {
            final String BASE_URL="http://api.themoviedb.org/3/movie/";

            //Please enter your own API KEY in place of <YOUR_API_KEY> in strings.xml
            final String API_KEY="?api_key="+getString(R.string.api_key);
            String path=BASE_URL+params[0]+API_KEY;

            try{
                URL url=new URL(path);
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
            return getMovieData(movies_detail,params[1]);
        }
        private List<MovieVO> getMovieData(String movies_detail,String favourite) {
            final String RESULTS = "results";
            final String TITLE = "original_title";
            final String OVER_VIEW = "overview";
            final String POSTER_PATH = "poster_path";
            final String RELEASE_DATE = "release_date";
            final String RATINGS = "vote_average";
            final String ID = "id";
            List<MovieVO> movies=new ArrayList<>();
            try {
                JSONObject movieJson = new JSONObject(movies_detail);
                JSONArray movieArray=movieJson.getJSONArray(RESULTS);

                SharedPreferences preferences = getContext().getSharedPreferences("favs", Context.MODE_PRIVATE);
                Set<String> favs = preferences.getStringSet("favs", null);

                for(int i=0;i<movieArray.length();i++){
                    JSONObject movieObject=movieArray.getJSONObject(i);
                    MovieVO temp_movieVO =new MovieVO(movieObject.getString((ID)));
                    temp_movieVO.setName(movieObject.getString(TITLE));
                    temp_movieVO.setImageUrl(movieObject.getString(POSTER_PATH));
                    temp_movieVO.setOverview(movieObject.getString(OVER_VIEW));
                    temp_movieVO.setRating(movieObject.getDouble(RATINGS));
                    temp_movieVO.setReleaseDate(movieObject.getString(RELEASE_DATE));
                    if(favourite == null || favourite.equalsIgnoreCase("false"))
                        movies.add(temp_movieVO);
                    if(favourite != null && favourite.equalsIgnoreCase("true"))
                        if(favs != null && favs.contains(temp_movieVO.getName()))
                            movies.add(temp_movieVO);
                }
            }catch (Exception e){
                Log.e(LOG_TAG,e.getMessage());
            }
            return movies;
        }
        @Override
        protected void onPostExecute(List<MovieVO> all_movies) {
            if(all_movies!=null) {

                madapter.clear();
                madapter.addAll(all_movies);
                madapter.notifyDataSetChanged();
            }
        }

    }
}

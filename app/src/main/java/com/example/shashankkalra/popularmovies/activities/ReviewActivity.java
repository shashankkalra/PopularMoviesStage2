package com.example.shashankkalra.popularmovies.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.example.shashankkalra.popularmovies.R;
import com.example.shashankkalra.popularmovies.adapters.ReviewAdapter;
import com.example.shashankkalra.popularmovies.entities.ReviewVO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    ReviewAdapter reviewsAdapter;
    private static final String LOG_TAG = "ReviewActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reviews);

        reviewsAdapter = new ReviewAdapter(this, R.layout.review_item, new ArrayList<ReviewVO>());
        ListView listOfReviews = (ListView) this.findViewById(R.id.reviewsListView);
        listOfReviews.setAdapter(reviewsAdapter);
        PopulateReviews reviews = new PopulateReviews();
        reviews.execute(getIntent().getExtras().getString("id"));
    }

    public class PopulateReviews extends AsyncTask<String,Void,List<ReviewVO>> {
        HttpURLConnection urlConnection;
        BufferedReader bufferedReader;
        String movies_detail=null;
        @Override
        protected List<ReviewVO> doInBackground(String... params) {
            final String REVIEWS_URL="http://api.themoviedb.org/3/movie/"+params[0]+"/reviews?api_key="+getString(R.string.api_key);
            //Please enter your own API KEY in place of <YOUR_API_KEY> in below line before building the code
            try{
                URL url=new URL(REVIEWS_URL);
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
            return getReviewData(movies_detail);
        }

        private List<ReviewVO> getReviewData(String movies_detail) {
            final String RESULTS = "results";
            final String AUTHOR = "author";
            final String CONTENT = "content";
            final String URL = "url";
            List<ReviewVO> reviews=new ArrayList<>();
            try {
                JSONObject movieJson = new JSONObject(movies_detail);
                JSONArray trailerJSONArray=movieJson.getJSONArray(RESULTS);
                for(int i=0;i<trailerJSONArray.length();i++){
                    JSONObject reviewObject=trailerJSONArray.getJSONObject(i);
                    ReviewVO temp_reviewVO =new ReviewVO();
                    temp_reviewVO.setContent(reviewObject.getString(CONTENT));
                    temp_reviewVO.setAuthor(reviewObject.getString(AUTHOR));
                    reviews.add(temp_reviewVO);
                }
            }catch (Exception e){
                Log.e(LOG_TAG,e.getMessage());
            }
            return reviews;
        }
        @Override
        protected void onPostExecute(List<ReviewVO> reviews) {
            if(reviews!=null) {
                reviewsAdapter.clear();
                reviewsAdapter.addAll(reviews);
                reviewsAdapter.notifyDataSetChanged();
            }
        }

    }

}

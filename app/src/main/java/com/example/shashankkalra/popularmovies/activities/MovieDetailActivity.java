package com.example.shashankkalra.popularmovies.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.shashankkalra.popularmovies.R;
import com.example.shashankkalra.popularmovies.viewsfragments.MovieDetailFragment;

public class MovieDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if(savedInstanceState==null){
            MovieDetailFragment fragment=new MovieDetailFragment();
            Bundle temp=new Bundle();
            temp.putParcelable("movie",getIntent().getParcelableExtra("movie"));
            fragment.setArguments(temp);
            getSupportFragmentManager().beginTransaction().
                    add(R.id.movie_detail_container,fragment).commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void loadReviews(View view){
        Intent intent = new Intent(this, ReviewActivity.class);
        TextView name = (TextView) findViewById(R.id.name);
        TextView movieId = (TextView) findViewById(R.id.movieId);
        String movieName = name.getText().toString();
        String movieID = movieId.getText().toString();
        intent.putExtra(getString(R.string.movie_name_key), movieName);
        intent.putExtra(getString(R.string.movie_id_key), movieID);
        startActivity(intent);
    }

    public void addRemoveFromFavourites(View view){
        SharedPreferences favs = getSharedPreferences("favs", Context.MODE_APPEND);
    }
}

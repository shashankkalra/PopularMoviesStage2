package com.example.shashankkalra.popularmovies.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.shashankkalra.popularmovies.R;
import com.example.shashankkalra.popularmovies.entities.MovieVO;
import com.example.shashankkalra.popularmovies.viewsfragments.MovieDetailFragment;

public class MainActivity extends AppCompatActivity {

    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_layout, new MovieFragment())
                    .commit();
        }*/

        //            if (savedInstanceState == null) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.movie_detail_container, new MovieDetailFragment())
//                        .commit();
//            }
        mTwoPane = findViewById(R.id.movie_detail_container) != null;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onItemSelected(MovieVO movie) {
        if(!mTwoPane){
            Intent intent=new Intent(this,MovieDetailActivity.class);
            intent.putExtra("movie",movie);
            startActivity(intent);
            }else{
            Bundle arguments = new Bundle();
            arguments.putParcelable("movie", movie);

                             MovieDetailFragment fragment = new MovieDetailFragment();
                  fragment.setArguments(arguments);

                             getSupportFragmentManager().beginTransaction()
                                     .replace(R.id.movie_detail_container, fragment)
                                    .commit();
                  }
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
}

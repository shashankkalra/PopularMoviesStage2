package com.example.shashankkalra.popularmovies.entities;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * MovieVO VO
 *
 * @author shashankkalra
 */
public class MovieVO implements Parcelable {

    String name, overview, imageUrl, releaseDate;
    final String BASE_URL = "http://image.tmdb.org/t/p/w342";
    double rating;

    public MovieVO(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    String id;
    String fullURL;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFullUrl(){
        return fullURL;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        fullURL = BASE_URL + imageUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public MovieVO(Cursor cursor){
        name=cursor.getString(1);
        id=cursor.getString(0);
        overview=cursor.getString(2);
        releaseDate=cursor.getString(4);
        imageUrl=cursor.getString(3);
        rating=Double.parseDouble(cursor.getString(5));
    }

    public MovieVO(Parcel in){
        String[] data=new String[6];
        in.readStringArray(data);
        id=data[0];
        name=data[1];
        overview=data[2];
        imageUrl=data[3];
        releaseDate=data[4];
        rating=Double.parseDouble(data[5]);
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                id,name,overview,imageUrl,releaseDate,String.valueOf(rating)
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MovieVO createFromParcel(Parcel in) {
            return new MovieVO(in);
        }

        public MovieVO[] newArray(int size) {
            return new MovieVO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}

package com.example.shashankkalra.popularmovies.entities;

/**
 * Created by shashankkalra on 03/04/16.
 */
public class TrailerVO {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String name;
    String key;

    public String getURL(){
        return "https://www.youtube.com/watch?v=" + getKey();
    }
}

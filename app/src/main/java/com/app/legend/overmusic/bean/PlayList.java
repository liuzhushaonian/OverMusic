package com.app.legend.overmusic.bean;

import android.util.Log;

import java.io.Serializable;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class PlayList implements Serializable{

    private String name;
    private String songs;
    private int id;
    private int length=0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSongs() {
        return songs;
    }

    public void setSongs(String songs) {
        this.songs = songs;
        cacuLength();
    }

    private void cacuLength(){
        if (songs.isEmpty()){
            return;
        }
        String[] strings=songs.split(";");

        this.length=strings.length;

    }

    public int getLength() {
        return length;
    }
}

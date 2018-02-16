package com.app.legend.overmusic.bean;

import java.io.Serializable;

/**
 *
 * Created by legend on 2018/2/6.
 */

public class Artist implements Serializable{

    private long id;
    private String name;
    private String album;

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

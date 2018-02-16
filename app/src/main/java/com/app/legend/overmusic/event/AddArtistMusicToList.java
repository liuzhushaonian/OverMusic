package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Artist;

/**
 *
 * Created by legend on 2018/2/13.
 */

public class AddArtistMusicToList {
    private Artist artist;

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public AddArtistMusicToList(Artist artist) {

        this.artist = artist;
    }
}

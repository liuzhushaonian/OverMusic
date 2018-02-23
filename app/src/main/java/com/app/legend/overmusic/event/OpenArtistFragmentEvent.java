package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Artist;

/**
 *
 * Created by legend on 2018/2/21.
 */

public class OpenArtistFragmentEvent {
    private Artist artist;

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public OpenArtistFragmentEvent(Artist artist) {

        this.artist = artist;
    }
}

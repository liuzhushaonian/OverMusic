package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Album;

/**
 *
 * Created by legend on 2018/2/15.
 */

public class SearchAlbumEvent {
    private Album album;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public SearchAlbumEvent(Album album) {

        this.album = album;
    }


}

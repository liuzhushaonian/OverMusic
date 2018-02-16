package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Album;

/**
 *
 * Created by legend on 2018/2/13.
 */

public class AddAlbumMusicToListEvent {
    private Album album;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public AddAlbumMusicToListEvent(Album album) {

        this.album = album;
    }
}

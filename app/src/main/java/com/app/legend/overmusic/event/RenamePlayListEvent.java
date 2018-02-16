package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.PlayList;

/**
 *
 * Created by legend on 2018/2/14.
 */

public class RenamePlayListEvent {

    private PlayList playList;

    public PlayList getPlayList() {
        return playList;
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    public RenamePlayListEvent(PlayList playList) {

        this.playList = playList;
    }
}

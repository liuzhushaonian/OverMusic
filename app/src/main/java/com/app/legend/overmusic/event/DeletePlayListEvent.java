package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.PlayList;

/**
 *
 * Created by legend on 2018/2/14.
 */

public class DeletePlayListEvent {
    private PlayList playList;

    public PlayList getPlayList() {
        return playList;
    }

    public void setPlayList(PlayList playList) {
        this.playList = playList;
    }

    public DeletePlayListEvent(PlayList playList) {

        this.playList = playList;
    }
}

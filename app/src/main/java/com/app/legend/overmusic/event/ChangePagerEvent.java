package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/27.
 */

public class ChangePagerEvent {
    private int position;
    private List<Music> musicList;

    public List<Music> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
    }

    public int getPosition() {

        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ChangePagerEvent(int position, List<Music> musicList) {

        this.position = position;
        this.musicList = musicList;
    }
}

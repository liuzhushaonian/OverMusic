package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

/**
 *
 * Created by legend on 2018/2/17.
 */

public class PlayingMusicChangeEvent {

    private Music music;

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public PlayingMusicChangeEvent(Music music) {

        this.music = music;
    }
}

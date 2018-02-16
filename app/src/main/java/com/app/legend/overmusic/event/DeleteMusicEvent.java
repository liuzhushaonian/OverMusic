package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

/**
 *
 * Created by legend on 2018/2/13.
 */

public class DeleteMusicEvent {
    private Music music;
    private int position;

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public DeleteMusicEvent(Music music, int position) {

        this.music = music;
        this.position = position;
    }
}

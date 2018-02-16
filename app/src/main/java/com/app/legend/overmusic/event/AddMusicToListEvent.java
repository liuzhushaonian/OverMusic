package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class AddMusicToListEvent {

    private Music music;

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public AddMusicToListEvent(Music music) {

        this.music = music;
    }
}

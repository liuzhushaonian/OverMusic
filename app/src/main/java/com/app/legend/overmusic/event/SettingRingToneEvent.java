package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

/**
 *
 * Created by legend on 2018/2/16.
 */

public class SettingRingToneEvent {
    private Music music;

    public Music getMusic() {

        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public SettingRingToneEvent(Music music) {

        this.music = music;
    }
}

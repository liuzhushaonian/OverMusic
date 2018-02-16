package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

/**
 *
 * Created by legend on 2018/2/2.
 */

public class ListStatusEvent {

    private Music pre_music;

    private Music current_music;

    public ListStatusEvent(Music pre_music, Music current_music) {
        this.pre_music = pre_music;
        this.current_music = current_music;
    }

    public Music getPre_music() {
        return pre_music;
    }

    public void setPre_music(Music pre_music) {
        this.pre_music = pre_music;
    }

    public Music getCurrent_music() {
        return current_music;
    }

    public void setCurrent_music(Music current_music) {
        this.current_music = current_music;
    }
}

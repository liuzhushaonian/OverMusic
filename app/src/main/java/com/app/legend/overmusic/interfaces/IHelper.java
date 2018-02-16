package com.app.legend.overmusic.interfaces;

import com.app.legend.overmusic.bean.Music;

/**
 *
 * Created by legend on 2018/1/31.
 */

public interface IHelper {

    void playMusic(Music music);

    void pauseMusic();

    void stop();
    void startMusic();

    boolean isPlaying();
}

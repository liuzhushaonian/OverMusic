package com.app.legend.overmusic.interfaces;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.utils.PlayStatus;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/16.
 */

public interface IPlayingPresenter {

    void setCurrentPager(int position);
    void setData(List<Integer> positionList);
    void setStatus(PlayStatus status);
    void setPlayingStatus(int playingStatus);

    void setMusic(Music music);

    void setPlayProgress(int position,long progress);

    void openBottomSheetMenu();

}

package com.app.legend.overmusic.interfaces;

import android.app.Activity;

import com.app.legend.overmusic.bean.Lrc;
import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/3/15.
 */

public interface IPlayingAlbumFragmentPresenter {

    void setLrcList(List<Lrc> lrcList);

    void setTLrcList(List<Lrc> lrcList);
}

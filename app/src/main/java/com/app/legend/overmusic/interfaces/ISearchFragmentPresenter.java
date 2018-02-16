package com.app.legend.overmusic.interfaces;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 * 我也不是很懂年三十为什么还要写代码，嘛，昨天情人节都写了，也不差今天就是了
 * Created by legend on 2018/2/15.
 */

public interface ISearchFragmentPresenter {

    void setMusicData(List<Music> musicList);
    void setArtistData(List<Artist> artistList);
    void setAlbumData(List<Album> albumList);
    void showInfo();
    void hideInfo();
}

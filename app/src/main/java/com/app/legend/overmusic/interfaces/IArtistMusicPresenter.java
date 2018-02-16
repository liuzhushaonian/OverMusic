package com.app.legend.overmusic.interfaces;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/12.
 */

public interface IArtistMusicPresenter {

    void setAlbumData(List<Album> albums);
    void setMusicData(List<Music> musicList);
}

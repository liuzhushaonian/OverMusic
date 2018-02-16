package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IAlbumMusicPresenter;
import com.app.legend.overmusic.utils.Mp3Util;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/7.
 */

public class AlbumInfoPresenter {
    private IAlbumMusicPresenter albumMusicFragment;

    public AlbumInfoPresenter(IAlbumMusicPresenter albumMusicFragment) {
        this.albumMusicFragment = albumMusicFragment;
    }

    public void getData(Album album){
        Observable
                .create((ObservableOnSubscribe< List<Music>>) e->{
                    List<Music> list= Mp3Util.newInstance().getAlbumMusic(album);
                    if (list!=null){
                        e.onNext(list);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);

    }

    private void setData(List<Music> list){
        albumMusicFragment.setData(list);
    }
}

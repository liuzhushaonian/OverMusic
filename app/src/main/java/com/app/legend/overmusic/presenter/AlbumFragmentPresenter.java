package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.interfaces.IAlbumPresenter;
import com.app.legend.overmusic.utils.Mp3Util;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/6.
 */

public class AlbumFragmentPresenter {

    private IAlbumPresenter albumFragment;

    public AlbumFragmentPresenter(IAlbumPresenter albumFragment) {
        this.albumFragment = albumFragment;
    }

    public void getData(){

        Observable
                .create((ObservableOnSubscribe<List<Album>>) e -> {
                    List<Album> albumList= Mp3Util.newInstance().getAlbumList();
                    if (albumList!=null){
                        e.onNext(albumList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);
    }

    private void setData(List<Album> albums){
        albumFragment.setDataList(albums);
    }

}

package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.interfaces.IPlayListInfoPresenter;
import com.app.legend.overmusic.utils.Mp3Util;


import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class PlayListInfoPresenter {

    private IPlayListInfoPresenter fragment;

    public PlayListInfoPresenter(IPlayListInfoPresenter fragment) {
        this.fragment = fragment;
    }

    public void getData(PlayList playList){

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {
                    List<Music> musicList= Mp3Util.newInstance().getPlayListMusic(playList);

                    e.onNext(musicList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);
    }

    private void setData(List<Music> musicList){
        fragment.setData(musicList);
    }
}

package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.fragment.MusicFragment;
import com.app.legend.overmusic.interfaces.IMusicFragmentPresenter;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.OverApplication;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/1/30.
 */

public class MusicFragmentPresenter {

    private IMusicFragmentPresenter fragment;

    public MusicFragmentPresenter(MusicFragment fragmentPresenter) {
        this.fragment = fragmentPresenter;
    }

    public void getData(){
        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {

                   List<Music> allMusicList= Mp3Util.newInstance().getAllMusic(OverApplication.getContext());

                    if (allMusicList!=null){
                        e.onNext(allMusicList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(music -> fragment.setListData(music));
    }
}

package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.interfaces.IArtistPresenter;
import com.app.legend.overmusic.utils.Mp3Util;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/6.
 */

public class ArtistFragmentPresenter {
    private IArtistPresenter artistFragment;

    public ArtistFragmentPresenter(IArtistPresenter artistFragment) {
        this.artistFragment = artistFragment;
    }

    public void getData(){
        Observable
                .create((ObservableOnSubscribe<List<Artist>>) e -> {
                    List<Artist> artistList = Mp3Util.newInstance().getArtistList();

                    if (artistList != null) {
                        e.onNext(artistList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);

    }

    private void setData(List<Artist> artists){
        artistFragment.setData(artists);
    }
}

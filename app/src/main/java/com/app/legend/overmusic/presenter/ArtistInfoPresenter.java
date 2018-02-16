package com.app.legend.overmusic.presenter;

import android.util.Log;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IArtistMusicPresenter;
import com.app.legend.overmusic.utils.Mp3Util;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class ArtistInfoPresenter {

    private IArtistMusicPresenter fragment;

    public ArtistInfoPresenter(IArtistMusicPresenter fragment) {
        this.fragment = fragment;
    }

    public void getAlbumData(Artist artist){
        Observable
                .create((ObservableOnSubscribe<List<Album>>) e -> {
                    List<Album> albumList= Mp3Util.newInstance().getArtistAlbum(artist);
                    e.onNext(albumList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setAlbumData);

    }

    public void getMusicData(Artist artist){
        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {
                    List<Music> musicList=Mp3Util.newInstance().getArtistMusic(artist);
                    e.onNext(musicList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setMusicData);
    }

    private void setAlbumData(List<Album> albumData){
        fragment.setAlbumData(albumData);
    }

    private void setMusicData(List<Music> musicData){
        fragment.setMusicData(musicData);
    }
}

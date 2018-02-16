package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.ISearchFragmentPresenter;
import com.app.legend.overmusic.utils.Mp3Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/15.
 */

public class SearchFragmentPresenter {
    private ISearchFragmentPresenter fragment;

    public SearchFragmentPresenter(ISearchFragmentPresenter fragment) {
        this.fragment = fragment;
    }

    public void getData(String string){
        getAllData(string);
    }

//    private void getMusicData(String string){
//        Observable
//                .create((ObservableOnSubscribe<List<Music>>) e -> {
//                    List<Music> musicList= Mp3Util.newInstance().getSearchMusic(string);
//                    e.onNext(musicList);
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::setMusicData);
//    }
//
//    private void getArtistData(String string){
//        Observable
//                .create((ObservableOnSubscribe<List<Artist>>) e -> {
//                    List<Artist> artistList=Mp3Util.newInstance().getSearchArtist(string);
//                    e.onNext(artistList);
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::setArtistData);
//    }
//
//    private void getAlbumData(String string){
//
//        Observable
//                .create((ObservableOnSubscribe<List<Album>>) e -> {
//                    List<Album> albumList=Mp3Util.newInstance().getSearchAlbum(string);
//                    e.onNext(albumList);
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::setAlbumData);
//
//    }

    private void getAllData(String string){
        Observable
                .create((ObservableOnSubscribe<Map>) e -> {
                    List<Music> musicList=Mp3Util.newInstance().getSearchMusic(string);
                    List<Artist> artistList=Mp3Util.newInstance().getSearchArtist(string);
                    List<Album> albumList=Mp3Util.newInstance().getSearchAlbum(string);

                    Map map=new HashMap();
                    map.put("music",musicList);
                    map.put("artist",artistList);
                    map.put("album",albumList);

                    e.onNext(map);
                })
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(map -> {
                    List<Music> musicList= (List<Music>) map.get("music");
                    List<Artist> artistList= (List<Artist>) map.get("artist");
                    List<Album> albumList= (List<Album>) map.get("album");
                    if (musicList.isEmpty()&&artistList.isEmpty()&&albumList.isEmpty()){
                        showInfo();
                    }else {
                        hideInfo();
                    }


                    setMusicData(musicList);
                    setArtistData(artistList);
                    setAlbumData(albumList);
                });
    }



    private void setMusicData(List<Music> musicList){
        fragment.setMusicData(musicList);
    }

    private void setArtistData(List<Artist> artistList){
        fragment.setArtistData(artistList);
    }


    private void setAlbumData(List<Album> albumList){
        fragment.setAlbumData(albumList);
    }

    private void showInfo(){
        fragment.showInfo();
    }

    private void hideInfo(){
        fragment.hideInfo();
    }


}

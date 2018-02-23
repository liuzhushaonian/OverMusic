package com.app.legend.overmusic.presenter;



import android.util.Log;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.BigPagerChangeEvent;
import com.app.legend.overmusic.event.GetProgressEvent;
import com.app.legend.overmusic.event.OpenAlbumFragmentEvent;
import com.app.legend.overmusic.event.OpenArtistFragmentEvent;
import com.app.legend.overmusic.event.PagerNotifyChangeEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.event.PlayingMusicChangeEvent;
import com.app.legend.overmusic.event.SearchAlbumEvent;
import com.app.legend.overmusic.event.SearchArtistEvent;
import com.app.legend.overmusic.interfaces.IPlayingPresenter;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 *
 * Created by legend on 2018/2/16.
 */

public class PlayingPresenter{
    private IPlayingPresenter activity;
    private Disposable pagerChange,status_dis,playing_dis,getProgress_dis,artist_dis,album_dis,pager_dis;

    public PlayingPresenter(IPlayingPresenter activity) {
        this.activity = activity;
        register();
    }

    private void register(){

        //传递正在播放的歌曲（从列表点击才需传递）
        pagerChange= RxBus.getDefault().tObservable(BigPagerChangeEvent.class).subscribe(bigPagerChangeEvent -> {

            int position=bigPagerChangeEvent.getPosition();
            List<Integer> integerList=bigPagerChangeEvent.getIntegerList();
            setData(integerList);
            setCurrent(position);
//            runToCurrentMusic(music,position);

        });

        status_dis=RxBus.getDefault().tObservable(PlayEvent.class).subscribe(playEvent -> {

            setStatus(playEvent.getStatus());

        });

        playing_dis=RxBus.getDefault().tObservable(PlayingMusicChangeEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(playingMusicChangeEvent -> {

            setMusic(playingMusicChangeEvent.getMusic());
        });

        getProgress_dis=RxBus.getDefault().tObservable(PlayPositionEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(playPositionEvent -> {
            setProgress(playPositionEvent.getPosition(),playPositionEvent.getProgress());

        });

        artist_dis=RxBus.getDefault().tObservable(OpenArtistFragmentEvent.class).subscribe(openArtistFragmentEvent -> {
            startActivityForArtist(openArtistFragmentEvent.getArtist());
        });

        album_dis=RxBus.getDefault().tObservable(OpenAlbumFragmentEvent.class).subscribe(openAlbumFragmentEvent -> {
            startActivityForAlbum(openAlbumFragmentEvent.getAlbum());
        });

        pager_dis=RxBus.getDefault().tObservable(PagerNotifyChangeEvent.class).subscribe(pagerNotifyChangeEvent -> {

            activity.changeViewPager();
        });

    }

    private void unregister(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    public void dis(){
        unregister(pagerChange);
        unregister(status_dis);
        unregister(playing_dis);
        unregister(artist_dis);
        unregister(album_dis);
        unregister(getProgress_dis);
    }

    private void setData(List<Integer> integers){
        if (this.activity!=null){

            activity.setData(integers);
        }
    }

    private void setCurrent(int position){
        if (this.activity!=null){
            activity.setCurrentPager(position);
        }
    }

    private void setStatus(int status){
        if (activity!=null){
            activity.setPlayingStatus(status);
        }
    }

    /**
     * 从playhelper获取正在播放的列表及其信息
     */
    public void getData(){

        List<Integer> integerList= PlayHelper.create().getCurrentList();
        int position=PlayHelper.create().getPosition();

        setData(integerList);

        setCurrent(position);
    }

    private void setMusic(Music music){
        if (activity!=null){
            activity.setMusic(music);
        }
    }

    private void setProgress(int position,long progress){

//        Music music=PlayHelper.create().getCurrent_music();
//        long time=music.getTime();
//
//        int progress= (int) ((position/(time/500)));


        activity.setPlayProgress(position,progress);
    }

    public void getProgress(){
        RxBus.getDefault().post(new GetProgressEvent(1));
    }

    private void startActivityForArtist(Artist artist){
        activity.startActivityForArtist(artist);
    }

    private void startActivityForAlbum(Album album){
        activity.startActivityForAlbum(album);
    }

}

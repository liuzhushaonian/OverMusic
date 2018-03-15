package com.app.legend.overmusic.presenter;



import android.graphics.Bitmap;
import android.util.Log;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.ChangePagerEvent;
import com.app.legend.overmusic.event.GetProgressEvent;
import com.app.legend.overmusic.event.OpenAlbumFragmentEvent;
import com.app.legend.overmusic.event.OpenArtistFragmentEvent;
import com.app.legend.overmusic.event.PagerNotifyChangeEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.event.PlayingMusicChangeEvent;
import com.app.legend.overmusic.event.StatusChangeEvent;
import com.app.legend.overmusic.interfaces.IPlayingPresenter;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.ImageUtil;
import com.app.legend.overmusic.utils.InternetUtil;
import com.app.legend.overmusic.utils.JsonUtil;
import com.app.legend.overmusic.utils.LyricManager;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/16.
 *
 */

public class PlayingPresenter{
    private IPlayingPresenter activity;
    private Disposable pagerChange,status_dis,playing_dis,getProgress_dis,artist_dis,album_dis,pager_dis,scroll_dis;

    public PlayingPresenter(IPlayingPresenter activity) {
        this.activity = activity;
        register();
    }

    private void register(){

        //传递正在播放的歌曲（从列表点击才需传递）
        pagerChange= RxBus.getDefault().tObservable(ChangePagerEvent.class).subscribe(bigPagerChangeEvent -> {

            int position=bigPagerChangeEvent.getPosition();
            List<Music> musicList=bigPagerChangeEvent.getMusicList();
            setData(musicList);
            setCurrent(position);
//            runToCurrentMusic(music,position);

        });

        status_dis=RxBus.getDefault().tObservable(PlayEvent.class).observeOn(AndroidSchedulers.mainThread()).subscribe(playEvent -> {

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

        scroll_dis=RxBus.getDefault().tObservable(StatusChangeEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(statusChangeEvent -> {
                    if (this.activity!=null){
                        activity.setScroll(statusChangeEvent.isScroll());
                    }
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
        unregister(scroll_dis);
        unregister(pager_dis);
    }

    private void setData(List<Music> musicList){
        if (this.activity!=null){
            activity.setData(musicList);
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

        List<Music> musicList=PlayHelper.create().getCurrentMusicList();
        int position=PlayHelper.create().getPosition();
        setData(musicList);
        setCurrent(position);

    }

    private void setMusic(Music music){
        if (activity!=null){
            activity.setMusic(music);
//            getMusicData(music);
        }
    }

    private void setProgress(int position,long progress){

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

    public void getBitmap(Music music){

        Observable
                .create((ObservableOnSubscribe<List<Bitmap>>) e -> {
                    Bitmap bitmap= ImageLoader.getImageLoader(OverApplication.getContext()).getBitmap(music.getAlbumId());

                    List<Bitmap> bitmapList=new ArrayList<>();
                    if (bitmap!=null){
                        bitmap= ImageUtil.getBitmap(OverApplication.getContext(),bitmap,25);

                        bitmapList.add(bitmap);
                    }else {
                        bitmapList.add(null);
                    }

                    e.onNext(bitmapList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmaps -> {
                    if (activity!=null){

                        activity.setBlurBitmap(bitmaps.get(0));

                    }
                });
    }


    private void getMusicData(Music music){

        Observable
                .create((ObservableOnSubscribe<Long>) e -> {
                    String json=InternetUtil.getUtil().getJson("type=search&s="+music.getSongName());

//                    Log.d("jj--->>",json);
                    long id=JsonUtil.getJsonUtil().getSongId(json);

                    String doing="type=lyric&id="+id;

                    String j=InternetUtil.getUtil().getJson(doing);

//                    Log.d("j---->>",j);

                   List<String> lrcList= JsonUtil.getJsonUtil().getLrcList(j);

                   String lrc=lrcList.get(0);

                    LyricManager.getManager().parseLrc(lrc);


                    e.onNext(id);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(eLong -> {

                });
    }
}

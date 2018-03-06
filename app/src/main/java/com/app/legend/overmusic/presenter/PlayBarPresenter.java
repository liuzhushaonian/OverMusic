package com.app.legend.overmusic.presenter;


import android.util.Log;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AutoPagerEvent;
import com.app.legend.overmusic.event.ChangePagerEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.event.StatusChangeEvent;
import com.app.legend.overmusic.interfaces.IPlayBarPresenter;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 *
 * Created by legend on 2018/2/3.
 */

public class PlayBarPresenter {

    private IPlayBarPresenter playBarFragment;
    private Disposable pagerChange,playStatus,autoPager,play_position,status_dis;

    public PlayBarPresenter(IPlayBarPresenter playBarFragment) {
        this.playBarFragment = playBarFragment;
        register();
    }


    //注册rxbus
    private void register(){

        //传递正在播放的歌曲（从列表点击才需传递）
        pagerChange=RxBus.getDefault().tObservable(ChangePagerEvent.class).subscribe(changePagerEvent -> {

            if (notNull()) {
                int position = changePagerEvent.getPosition();
                List<Music> musicList = changePagerEvent.getMusicList();
//                playBarFragment.setMusicData(position,musicList);
                setData(position,musicList);

                Log.d("musiclist---->>>",musicList.size()+"");
            }

        });

        playStatus=RxBus.getDefault().tObservable(PlayEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(playEvent -> {
           int status=playEvent.getStatus();
           if (notNull()) {
               playBarFragment.setStatus(status);
           }
        });

        autoPager=RxBus.getDefault().tObservable(AutoPagerEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(autoPagerEvent -> {

            runToCurrentMusic(autoPagerEvent.getPosition());
        });

        play_position=RxBus.getDefault().tObservable(PlayPositionEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(playPositionEvent -> {
            changePosition(playPositionEvent.getPosition());

        });

        status_dis=RxBus.getDefault().tObservable(StatusChangeEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(statusChangeEvent -> {
            if (notNull()){
                playBarFragment.setScroll(statusChangeEvent.isScroll());
            }
        });


    }

    public void unregister(){

        if (pagerChange!=null&&!pagerChange.isDisposed()){
            pagerChange.dispose();
        }

        if (playStatus!=null&&!playStatus.isDisposed()){
            playStatus.isDisposed();
        }

        if (autoPager!=null&&!autoPager.isDisposed()){
            autoPager.isDisposed();
        }

        if (this.playBarFragment!=null){
            this.playBarFragment=null;
        }
    }

    private void runToCurrentMusic(int position){
        if (notNull()) {
            playBarFragment.setCurrentPager(position);
        }

    }

    /**
     * 恢复时自动获取数据
     */
    public void getData(){
        if (notNull()){
            List<Music> musicList= PlayHelper.create().getCurrentMusicList();
            int position=PlayHelper.create().getPosition();
            playBarFragment.setMusicData(position,musicList);

        }

    }



    private void changePosition(int progress){

      if (notNull()) {
            playBarFragment.setProgress(progress);
        }

    }

    private boolean notNull(){
        return this.playBarFragment!=null;
    }

    private void setData(int position,List<Music> musicList){
        if (notNull()){
            playBarFragment.setMusicData(position,musicList);
        }
    }

}

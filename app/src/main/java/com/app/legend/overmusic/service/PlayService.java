package com.app.legend.overmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.GetProgressEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.event.SeekEvent;
import com.app.legend.overmusic.interfaces.IHelper;
import com.app.legend.overmusic.interfaces.OnChangeSeekLinstener;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static java.lang.Thread.sleep;

public class PlayService extends Service implements IHelper{


    MediaPlayer mediaPlayer;
    private Disposable seek_dis,get_dis;
    private Timer timer;

    private static final int CPU_COUNT=Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE=CPU_COUNT+1;
    private static final int MAXNUM_POOL_SIZE=CPU_COUNT*2+1;
    private static final long KEEP_ALIVE=10L;

    private static final ThreadFactory mThreadFactory=new ThreadFactory() {
        private final AtomicInteger count=new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable,"PlayService#"+count.getAndIncrement());
        }
    };

    private static final Executor threadPool=new ThreadPoolExecutor(
            CORE_POOL_SIZE,MAXNUM_POOL_SIZE,KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),mThreadFactory);





    public class PlayBind extends Binder {
        public PlayService getPlayService(){
            return PlayService.this;
        }
    }

    public PlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer=new MediaPlayer();
        timer=new Timer();
        mediaPlayer.setOnPreparedListener(mp -> {
            start();
            startTimer(PlayHelper.create().getCurrent_music());
        });

        PlayHelper.newInstance(PlayService.this);

        mediaPlayer.setOnCompletionListener(mp -> PlayHelper.create().autoNext());

        register();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.reset();
            mediaPlayer.release();
        }

        unregister(seek_dis);
        unregister(get_dis);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        return new PlayBind();
    }


    //播放
    private void start(){
        if (mediaPlayer==null){
            return;
        }
        mediaPlayer.start();
    }

    //暂停
    private void pause(){
        if (mediaPlayer==null){
            return;
        }
        mediaPlayer.pause();
    }

    private void play(Music music){

        if (mediaPlayer!=null){
            cancalTimer();

            mediaPlayer.stop();
            mediaPlayer.reset();

//            Runnable runnable= () -> {
                try {
//                    cancalTimer();

//                    mediaPlayer.setDataSource(OverApplication.getContext(), Uri.parse(music.getUrl()));
                    mediaPlayer.setDataSource(music.getUrl());
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

//            };

//            threadPool.execute(runnable);
        }
    }

    @Override
    public void playMusic(Music music) {
        play(music);
    }

    @Override
    public void pauseMusic() {
        pause();
    }

    @Override
    public void stop() {
        if (mediaPlayer!=null){
            mediaPlayer.stop();
        }
    }

    @Override
    public void startMusic() {
        start();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    private void setCurrentPosition(int position,long progress){

        RxBus.getDefault().post(new PlayPositionEvent(position,progress));
    }

    private void register(){

        seek_dis=RxBus.getDefault().tObservable(SeekEvent.class).subscribe(seekEvent -> {
            setProgress(seekEvent.getPosition());
        });

        /**
         * 打开PlayingActivity页面时获取当前进度
         */
        get_dis=RxBus.getDefault().tObservable(GetProgressEvent.class).subscribe(getProgressEvent -> {
            if (mediaPlayer!=null&&!mediaPlayer.isPlaying()){

                Music music=PlayHelper.create().getCurrent_music();

                int progress = (int) ((mediaPlayer.getCurrentPosition() / (music.getTime() / 500)));
                setCurrentPosition(progress,mediaPlayer.getCurrentPosition());
            }

        });
    }

    private void unregister(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    private void setProgress(int position){
        if (mediaPlayer!=null){
            mediaPlayer.seekTo(position);
        }
    }

    private void startTimer(Music music){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    timer=new Timer();

                    TimerTask timerTask=new TimerTask() {
                        @Override
                        public void run() {
                            long time = music.getTime();

                            int progress = (int) ((mediaPlayer.getCurrentPosition() / (time / 500)));

                            setCurrentPosition(progress, mediaPlayer.getCurrentPosition());
                        }
                    };

                    timer.schedule(timerTask,0,1000);
                    Log.d("thread11---->>",Thread.currentThread().getName()+"");

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer ->{});

    }

    private void cancalTimer(){
        if (timer!=null) {
            timer.cancel();
        }
    }


}

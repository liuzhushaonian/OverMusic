package com.app.legend.overmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.MainActivity;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.broadcast.PlayingBroadcast;
import com.app.legend.overmusic.event.GetProgressEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.event.SeekEvent;
import com.app.legend.overmusic.interfaces.IHelper;
import com.app.legend.overmusic.interfaces.OnChangeSeekLinstener;
import com.app.legend.overmusic.utils.ColorUtil;
import com.app.legend.overmusic.utils.ImageLoader;
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

public class PlayService extends Service implements IHelper,AudioManager.OnAudioFocusChangeListener{


    MediaPlayer mediaPlayer;
    private Disposable seek_dis,get_dis;
    private Timer timer;
    private static final String CHANNEL_ID="OVER_MUSIC";

    private PlayingBroadcast playingBroadcast;
    private Notification notification;
    private NotificationChannel notificationChannel;

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN://已获得音频焦点
                if (mediaPlayer!=null){
                    PlayHelper.create().start();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS://失去音频焦点
                PlayHelper.create().pause();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT://临时失去焦点
                PlayHelper.create().pause();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://临时失去焦点，但允许低音量播放
                if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(0.1f,0.1f);//把声音调低

                }
                break;
        }
    }


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

            if (this.notification==null) {
                startNotification();//打开前台通知
                changePlayStatus();//改变通知按钮
            }


            changeNotification(PlayHelper.create().getCurrent_music());//改变通知内容

        });

        PlayHelper.newInstance(PlayService.this);

        mediaPlayer.setOnCompletionListener(mp -> PlayHelper.create().autoNext());

        register();

        this.playingBroadcast=new PlayingBroadcast();

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(PlayingBroadcast.NEXT);
        intentFilter.addAction(PlayingBroadcast.PLAY);
        intentFilter.addAction(PlayingBroadcast.PAUSE);
        intentFilter.addAction(PlayingBroadcast.PREVIOUS);

        registerReceiver(this.playingBroadcast,intentFilter);


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
        if (this.playingBroadcast!=null) {
            unregisterReceiver(this.playingBroadcast);
        }

        if (this.notification!=null){
            stopNotification(true);
        }
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
        turnUpVolume();//渐渐提升音量
        changePlayStatus();

    }

    //暂停
    private void pause(){
        if (mediaPlayer==null){
            return;
        }

        mediaPlayer.pause();
        changePlayStatus();
    }

    private void play(Music music) {

        if (mediaPlayer != null) {
            cancalTimer();

            mediaPlayer.stop();
            mediaPlayer.reset();

            try {

                mediaPlayer.setDataSource(music.getUrl());
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void playMusic(Music music) {
        play(music);
    }

    @Override
    public void pauseMusic() {
//        pause();
        turnDownVolume();
    }

    @Override
    public void stop() {
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            stopNotification(false);
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

    private RemoteViews getNotificationView(int res){

        return new RemoteViews(this.getPackageName(),res);

    }

    private PendingIntent getPendingIntentForBroadcast(Intent intent){

        return PendingIntent.getBroadcast(this,0,intent,0);
    }

    private void startNotification(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence sequence="over_music_notification_channel";
            this.notificationChannel=new NotificationChannel(CHANNEL_ID,sequence,NotificationManager.IMPORTANCE_MIN);
            this.notificationChannel.setShowBadge(false);
            this.notificationChannel.setLockscreenVisibility(0);
            this.notificationChannel.setSound(null,null);

            NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder=new NotificationCompat.Builder(PlayService.this,CHANNEL_ID);

        RemoteViews remoteBigViews=getNotificationView(R.layout.big_notification);

        RemoteViews remoteSmallViews=getNotificationView(R.layout.small_notification);


        /**
         * 上一曲
         */
        Intent pre_intent=new Intent(PlayingBroadcast.PREVIOUS);
        PendingIntent pre_pendingIntent=getPendingIntentForBroadcast(pre_intent);
        remoteBigViews.setOnClickPendingIntent(R.id.notification_previous,pre_pendingIntent);
        remoteSmallViews.setOnClickPendingIntent(R.id.small_notification_previous,pre_pendingIntent);


        /**
         * 下一曲
         */
        Intent next_intent=new Intent(PlayingBroadcast.NEXT);
        PendingIntent next_pendingIntent=getPendingIntentForBroadcast(next_intent);
        remoteBigViews.setOnClickPendingIntent(R.id.notification_next,next_pendingIntent);
        remoteSmallViews.setOnClickPendingIntent(R.id.small_notification_next,next_pendingIntent);

        /**
         * 播放or暂停
         */
        Intent action_intent=new Intent(PlayingBroadcast.PLAY);
        PendingIntent action_pendingIntent=getPendingIntentForBroadcast(action_intent);
        remoteBigViews.setOnClickPendingIntent(R.id.notification_play,action_pendingIntent);
        remoteSmallViews.setOnClickPendingIntent(R.id.small_notification_play,action_pendingIntent);


        builder.setCustomContentView(remoteSmallViews);
        builder.setCustomBigContentView(remoteBigViews);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        this.notification=builder
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_album_black_24dp)
                .setContentIntent(pendingIntent)
                .setChannelId(CHANNEL_ID)
                .build();

        startForeground(110, notification);

    }

    /**
     * 改变通知
     * @param music
     */
    private void changeNotification(Music music){

        if (this.notification==null){
            return;
        }


        this.notification.bigContentView.setTextViewText(R.id.notification_song,music.getSongName());
        this.notification.contentView.setTextViewText(R.id.small_notification_song,music.getSongName());

        String info=music.getArtistName()+" | "+music.getAlbumName();

        this.notification.bigContentView.setTextViewText(R.id.notification_info,info);
        this.notification.contentView.setTextViewText(R.id.small_notification_info,info);

        int w=getResources().getDimensionPixelSize(R.dimen.notification_big_book);

        Bitmap bitmap= ImageLoader.getImageLoader(getApplicationContext()).getSizeBitmap(music.getAlbumId(),w,w);

        if (bitmap!=null) {

            int defaultValue=getResources().getColor(R.color.colorBlueGrey);

            int d=OverApplication.getContext().getSharedPreferences("over_music_shared",MODE_PRIVATE).getInt("color",defaultValue);

            int color= ColorUtil.getColor(bitmap,d);

            this.notification.bigContentView.setTextColor(R.id.notification_song,color);
            this.notification.bigContentView.setTextColor(R.id.notification_info,color);
            this.notification.bigContentView.setImageViewBitmap(R.id.notification_album_book, bitmap);


            this.notification.contentView.setImageViewBitmap(R.id.small_notification_album_book, bitmap);
            this.notification.contentView.setTextColor(R.id.small_notification_song,color);
            this.notification.contentView.setTextColor(R.id.small_notification_info,color);

        }else {

            this.notification.bigContentView.setImageViewResource(R.id.notification_album_book,R.drawable.ic_audiotrack_black_24dp);
            this.notification.contentView.setImageViewResource(R.id.small_notification_album_book, R.drawable.ic_audiotrack_black_24dp);

        }

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;



        notificationManager.notify(110,this.notification);

    }

    //停止前台通知
    //同时可以取消通知
    private void stopNotification(boolean remove){
        stopForeground(remove);
    }

    private void changePlayStatus(){

        if (this.notification==null){
            return;
        }

        if (mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            this.notification.bigContentView.setImageViewResource(R.id.notification_play, R.drawable.ic_pause_black_24dp);
            this.notification.contentView.setImageViewResource(R.id.small_notification_play,R.drawable.ic_pause_black_24dp);

            startForeground(110,this.notification);
        }else {
            this.notification.bigContentView.setImageViewResource(R.id.notification_play, R.drawable.ic_play_arrow_black_24dp);
            this.notification.contentView.setImageViewResource(R.id.small_notification_play,R.drawable.ic_play_arrow_black_24dp);
            stopNotification(false);
        }

        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager!=null) {

            notificationManager.notify(110, this.notification);
        }

    }


    /**
     * 实现渐入渐出
     */
    private void turnDownVolume(){

        new Thread(){
            @Override
            public void run() {
                super.run();

                boolean adjustVolume=false;

                float v=1.0f;

                while (!adjustVolume){

                    try {
                        sleep(100);

                        v-=0.1;

                        mediaPlayer.setVolume(v,v);

                        if (v<=0){
                            adjustVolume=true;
                            pause();
                            mediaPlayer.setVolume(1,1);//恢复正常音量
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();

    }

    private void turnUpVolume(){



        new Thread(){
            @Override
            public void run() {
                super.run();

                float v=0;

                boolean turn=false;

                mediaPlayer.setVolume(0,0);//开始播放

                while (!turn){

                    try {
                        sleep(100);
                        v+=0.1;


                        mediaPlayer.setVolume(v,v);

                        if (v>=1){
                            turn=true;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }




}

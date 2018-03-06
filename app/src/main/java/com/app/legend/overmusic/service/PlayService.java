package com.app.legend.overmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.MainActivity;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.broadcast.MediaButtonReceiver;
import com.app.legend.overmusic.broadcast.PlayingBroadcast;
import com.app.legend.overmusic.event.GetProgressEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.event.SeekEvent;
import com.app.legend.overmusic.interfaces.IHelper;
import com.app.legend.overmusic.utils.ColorUtil;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class PlayService extends Service implements IHelper,AudioManager.OnAudioFocusChangeListener{


    MediaPlayer mediaPlayer;
    private Disposable seek_dis,get_dis;
    private Timer timer;
    private static final String CHANNEL_ID="OVER_MUSIC";
    private PlayingBroadcast playingBroadcast;
    private NotificationChannel notificationChannel;
    AudioManager audioManager;

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
            startNewNotification(false);

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
        registerReceiver();
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

        stopForeground(true);
        unregisterReceiver();

        Log.d("destory---->>>","service is destory!");
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
//        changePlayStatus();
        startNewNotification(false);

    }

    //暂停
    private void pause(){
        if (mediaPlayer==null){
            return;
        }

        if (mediaPlayer.isPlaying()) {

            mediaPlayer.pause();
            stopNotification(false);

        }
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
        startNewNotification(true);
    }

    @Override
    public void stop() {
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            startNewNotification(true);
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

    private PendingIntent getPendingIntentForBroadcast(Intent intent){

        return PendingIntent.getBroadcast(this,0,intent,0);
    }


    //停止前台通知
    //同时可以取消通知
    private void stopNotification(boolean remove){
        stopForeground(remove);
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

    private void startNewNotification(boolean isPause){

        Music music=PlayHelper.create().getCurrent_music();

        NotificationCompat.Builder builder=new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence sequence="over_music_notification_channel";
            this.notificationChannel=new NotificationChannel(CHANNEL_ID,sequence,NotificationManager.IMPORTANCE_DEFAULT);
            this.notificationChannel.setShowBadge(false);
            this.notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            this.notificationChannel.setSound(null,null);


            NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Bitmap bitmap=ImageLoader.getImageLoader(getApplicationContext()).getBitmap(music.getAlbumId());

        if (bitmap!=null){
            builder.setLargeIcon(bitmap);

            int defaultValue=getResources().getColor(R.color.colorBlueGrey);
            int color=ColorUtil.getColor(bitmap,defaultValue);

            builder.setColor(color);
        }

        String info=music.getArtistName()+" | "+music.getAlbumName();

        Intent pre_intent=new Intent(PlayingBroadcast.PREVIOUS);
        PendingIntent pre_pendingIntent=getPendingIntentForBroadcast(pre_intent);

        Intent action_intent=new Intent(PlayingBroadcast.PLAY);
        PendingIntent action_pendingIntent=getPendingIntentForBroadcast(action_intent);

        Intent next_intent=new Intent(PlayingBroadcast.NEXT);
        PendingIntent next_pendingIntent=getPendingIntentForBroadcast(next_intent);

        NotificationCompat.Action pre_action=new NotificationCompat.Action(R.drawable.ic_skip_previous_black_24dp,"previous",pre_pendingIntent);
        builder.addAction(pre_action);
        if (!isPause){
            NotificationCompat.Action pause_action = new NotificationCompat.Action(R.drawable.ic_pause_black_24dp, "play", action_pendingIntent);
            builder.addAction(pause_action);
        }else {
            NotificationCompat.Action play_action = new NotificationCompat.Action(R.drawable.ic_play_arrow_black_24dp, "play", action_pendingIntent);
            builder.addAction(play_action);

        }

        NotificationCompat.Action next_action=new NotificationCompat.Action(R.drawable.ic_skip_next_black_24dp,"next",next_pendingIntent);
        builder.addAction(next_action);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

       Notification notification=builder.setContentTitle(music.getSongName())
                .setContentText(info)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2))
                .setSound(null)
                .build();

        startForeground(2,notification);

        if(mediaPlayer!=null&&!mediaPlayer.isPlaying()){
            stopForeground(false);
        }

    }


    private void registerReceiver(){

        audioManager = (AudioManager)getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(getApplicationContext().getPackageName(),
                MediaButtonReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(name);
    }

    private void unregisterReceiver(){
        if (this.audioManager!=null){
            ComponentName name = new ComponentName(getApplicationContext().getPackageName(),
                    MediaButtonReceiver.class.getName());
            audioManager.unregisterMediaButtonEventReceiver(name);
        }
    }


}

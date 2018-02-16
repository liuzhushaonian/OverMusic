package com.app.legend.overmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IHelper;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;

import java.io.IOException;
import java.util.List;

public class PlayService extends Service implements IHelper{


    MediaPlayer mediaPlayer;

    public PlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mp -> start());

        PlayHelper.newInstance(PlayService.this);

        mediaPlayer.setOnCompletionListener(mp -> PlayHelper.create().autoNext());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null){
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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

            mediaPlayer.stop();
            mediaPlayer.reset();

            new Thread(){
                @Override
                public void run() {
                    super.run();

                    try {

                        mediaPlayer.setDataSource(OverApplication.getContext(), Uri.parse(music.getUrl()));
                        mediaPlayer.prepareAsync();

                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                }
            }.start();
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
}

package com.app.legend.overmusic.utils;

import android.util.Log;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.ChangePagerEvent;
import com.app.legend.overmusic.event.ListStatusEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayingMusicChangeEvent;
import com.app.legend.overmusic.interfaces.IHelper;
import com.app.legend.overmusic.service.PlayService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 负责处理播放列表
 * <p>
 * Created by legend on 2018/1/31.
 */

public class PlayHelper {

    private static volatile PlayHelper playHelper;

    private static IHelper playService;

    private PlayStatus status = PlayStatus.NORMAL;

    private int position = -1;

    private Music current_music;

    public static final int PLAY = 0x0000100;

    public static final int PAUSE = 0x0000200;


    private List<Music> orderMusicList;
    private List<Music> randomMusicList;
    private List<Music> cacheList;//缓存

    private static final int FAST_CLICK_DELAY = 1000;
    private long lastClickTime = 0L;
    private boolean canPop=true;

    private PlayHelper() {
        this.cacheList = new ArrayList<>();
    }

    public static void newInstance(PlayService service) {
        playService = service;
        if (playHelper == null) {
            synchronized (PlayHelper.class) {
                playHelper = new PlayHelper();

            }
        }

    }

    public static PlayHelper create() {
        return playHelper;
    }


    /**
     * 提供通知按钮播放与暂停
     */
    public void playOrPause() {

        if (isPlaying()) {
            pause();
        } else {
            start();
        }
    }


    /**
     * 播放
     *
     * @param music
     */
    private void play(Music music) {
        if (music == null) {
            Log.w("playhelper-->>", "##the music is null!");
            return;
        }

//        if (System.currentTimeMillis() - lastClickTime <= FAST_CLICK_DELAY) {
//            //播放时间过快，将音乐进行缓存
//            cacheMusic(music);
//            pause();
////            return;
//        }else {
//            Log.d("info---->>>","playing!!!!");
            truelyPlayMusic(music);
//        }
//        lastClickTime = System.currentTimeMillis();
//        postToList(this.current_music, music);
//
//        this.current_music = music;
////        playService.playMusic(music);
//
//        playStatusChange(PLAY);//通知改变播放按钮
//        playingMusicChange();//通知改变播放音乐

    }

    private void truelyPlayMusic(Music music) {


        postToList(this.current_music, music);

        this.current_music = music;
//        playService.playMusic(music);

        playStatusChange(PLAY);//通知改变播放按钮
        playingMusicChange();//通知改变播放音乐

        playService.playMusic(music);
    }

    public void start() {
        playService.startMusic();
        playStatusChange(PLAY);
    }


    //仅提供播放页面的播放列表点击播放
    public void playMusicByClickPlayingList(Music music, int position) {

        play(music);
        this.position = position;
    }

    public void pause() {
        playService.pauseMusic();
        playStatusChange(PAUSE);
    }

    private void stop() {
        playService.stop();
        playStatusChange(PAUSE);

    }

    private void previous() {
        if (position - 1 < 0) {
            position = 0;
        } else {
            position--;
        }

        Music music = getPlayingMusic(position);

        play(music);

    }

    //提供ViewPager执行上一曲
    public void pagerToPrevious() {
        previous();
    }

    //提供button等控件执行上一曲操作
    public void buttonToPrevious() {
        previous();

        if (status.equals(PlayStatus.RANDOM)) {
            changePagerEvent(this.position, this.randomMusicList);
        } else {
            changePagerEvent(this.position, this.orderMusicList);
        }

    }

    //手动播放下一首
    private void next() {

        int limit = -1;

        limit = getCurrentMusicList().size();

        switch (status) {
            case CIRCULATION:
                if (position + 1 >= limit) {
                    position = 0;
                } else {
                    position++;
                }

                break;
            default:
                if (position + 1 >= limit) {

                    stop();
                    return;
                } else {
                    position++;
                }

                break;
        }

        Music music = getPlayingMusic(position);
        play(music);

    }

    //提供button等控件执行下一曲
    public void buttonToNext() {
        next();

        //改变pager
        if (status.equals(PlayStatus.RANDOM)) {

            changePagerEvent(this.position, randomMusicList);
        } else {

            changePagerEvent(this.position, orderMusicList);
        }

    }

    //提供ViewPager执行下一曲
    public void pagerToNext() {
        next();
    }


    //自动播放下一首
    public void autoNext() {
        if (status.equals(PlayStatus.SINGLE)) {

            playService.startMusic();

        } else {
            next();
        }

        //改变pager
        if (status.equals(PlayStatus.RANDOM)) {
            changePagerEvent(this.position, randomMusicList);

        } else {


            changePagerEvent(this.position, orderMusicList);
        }


    }

    /**
     * 设置播放模式
     *
     * @param status 模式
     */
    public void setStatus(PlayStatus status) {

        Log.d("position11111---->>>",this.position+"");

        if (this.status.equals(status)) {
            return;
        }

        boolean resume = false;

        if (this.status.equals(PlayStatus.RANDOM)) {
            resume = true;
        } else {
            resume = false;
        }

        this.status = status;

        if (resume) {

            resumeNormalMusicList();
            //切换到非随机
        } else if (this.status.equals(PlayStatus.RANDOM)) {

            initMusicRandomList();
        }

        Log.d("position22222---->>>",this.position+"");

    }


    public PlayStatus getStatus() {
        return status;
    }

    //当前播放列表顺序
    public int getPosition() {
        return position;
    }


    public Music getCurrent_music() {
        return current_music;
    }

    //改变播放状态
    private void playStatusChange(int status) {
        RxBus.getDefault().post(new PlayEvent(status));

    }

    //通知列表更改状态
    private void postToList(Music pre_music, Music current_music) {

        RxBus.getDefault().post(new ListStatusEvent(pre_music, current_music));

    }

    private void playingMusicChange() {
        RxBus.getDefault().post(new PlayingMusicChangeEvent(this.current_music));
    }

    public boolean isPlaying() {
        return playService.isPlaying();
    }


    /**
     *
     * ---------------------------------------------------------
     *
     */


    /**
     * 播放与更新列表，提供点击列表使用
     *
     * @param musicList list
     * @param position  位置
     */
    public void playMusicAndUpdateList(List<Music> musicList, int position) {

        if (musicList == null || position < 0) {
            return;
        }

        this.position = position;

        updateMusicList(musicList);


        Music music = getPlayingMusic(position);

        play(music);


    }


    private void updateMusicList(List<Music> musicList) {


        this.orderMusicList = new ArrayList<>();

        this.orderMusicList.addAll(musicList);

        resetMusicPosition(this.orderMusicList);//给音乐添加下标


        switch (this.status) {
            case RANDOM:
                initMusicRandomList();
                break;
            default:
                initOrderMusicList();
                break;
        }

    }


    private void initOrderMusicList() {

        changePagerEvent(this.position, this.orderMusicList);
    }

    /**
     * 实例化随机列表
     * 在随机模式下点击列表播放时需要
     */
    private void initMusicRandomList() {
        this.randomMusicList = new ArrayList<>();

        this.randomMusicList.addAll(this.orderMusicList);

        //移除当前播放音乐
        if (this.randomMusicList.contains(this.current_music)) {
            this.randomMusicList.remove(this.current_music);
        }

        Collections.shuffle(this.randomMusicList);//打乱顺序



        this.randomMusicList.add(0, this.current_music);//放入当前播放音乐

        this.position = 0;

        changePagerEvent(this.position, this.randomMusicList);

    }

    private void resumeNormalMusicList() {

        if (this.current_music != null) {
            this.position = this.current_music.getPosition();//获取音乐当前位置

            Log.d("this------>>>", this.position + "");

            changePagerEvent(this.position, this.orderMusicList);
        }
    }


    /**
     * 给列表里的music排序
     * 且每次列表有变动都需要重新排序
     */
    private void resetMusicPosition(List<Music> musicList) {
        if (musicList != null) {

            for (int i = 0; i < musicList.size(); i++) {

                musicList.get(i).setPosition(i);
            }
        }
    }

    /**
     * 根据position获取music
     *
     * @param position 位置
     * @return 返回music实例
     */
    public Music getPlayingMusic(int position) {

        Music music;

        switch (this.status) {
            case RANDOM:
                music = this.randomMusicList.get(position);
                break;
            default:

                music = this.orderMusicList.get(position);

                break;
        }

        return music;
    }

    public List<Music> getCurrentMusicList() {
        switch (this.status) {
            case RANDOM:

                return this.randomMusicList;

            default:

                return this.orderMusicList;

        }
    }


    private void changePagerEvent(int position, List<Music> musicList) {

        RxBus.getDefault().post(new ChangePagerEvent(position, musicList));
    }

    /**
     * 添加下一曲
     *
     * @param music 音乐
     */
    public void addNextMusic(Music music) {

        if (music == null) {
            Log.w("PlayHelper-->>", "the music is null!!");
            return;
        }

        Music m = newInstanceMusic(music);


        if (this.status.equals(PlayStatus.RANDOM)) {

            int p = this.current_music.getPosition();


            this.orderMusicList.add(p + 1, music);

            resetMusicPosition(this.orderMusicList);

            this.randomMusicList.add(this.position + 1, music);

            changePagerEvent(this.position, this.randomMusicList);

        } else {


            this.orderMusicList.add(this.position + 1, m);

            resetMusicPosition(this.orderMusicList);//重新设置position


            changePagerEvent(this.position, this.orderMusicList);
        }

    }

    /**
     * 删除正在播放的音乐
     *
     * @param music
     */
    public void deleteMusic(Music music, int position) {
        if (music == null) {
            Log.w("PlayHelper-->>", "the music is null!!");
            return;
        }

        if (this.status.equals(PlayStatus.RANDOM)) {

            //

        } else {


        }

    }


    private Music newInstanceMusic(Music m) {

        Music music = new Music();

        music.setId(m.getId());
        music.setPlayStatus(m.getPlayStatus());
        music.setUrl(m.getUrl());
        music.setTime(m.getTime());
        music.setSongName(m.getSongName());
        music.setArtistName(m.getArtistName());
        music.setAlbumId(m.getAlbumId());
        music.setArtistId(m.getArtistId());
        music.setAlbumName(m.getAlbumName());

        return music;
    }


    /**
     * 缓存过快进行下一曲的音乐
     *
     * @param music
     */
    private void cacheMusic(Music music) {
        if (this.cacheList != null) {
            this.cacheList.add(music);
            cancelTimer();
            startTimer();
            Log.d("music------>>>",music.getSongName());
        }
    }

    /**
     * 缓存结束，并将最后的music放置播放
     */
    private void popMusic() {
        if (this.cacheList != null && !this.cacheList.isEmpty()) {
            Music music = this.cacheList.get(this.cacheList.size() - 1);
            Log.d("music3333------>>>",music.getSongName());

            truelyPlayMusic(music);

            this.cacheList.clear();
        }
    }

    private Timer timer;
    private int open=0;

    private void startTimer(){
        timer=new Timer();

        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                open+=1;
                Log.d("open1----->>",open+"");
                if (open==15){
                    Log.d("open2----->>",open+"");
                    popMusic();
                    timer.cancel();

                }
            }
        };

        timer.schedule(timerTask,0,100);

    }

    private void cancelTimer(){
        if (timer!=null){
            open=0;
            timer.cancel();
        }
    }

}
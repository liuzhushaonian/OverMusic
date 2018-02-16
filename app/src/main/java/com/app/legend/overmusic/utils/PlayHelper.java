package com.app.legend.overmusic.utils;

import android.util.Log;
import android.widget.Toast;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.ListStatusEvent;
import com.app.legend.overmusic.event.PagerChangeEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayListChangeEvent;
import com.app.legend.overmusic.interfaces.IHelper;
import com.app.legend.overmusic.service.PlayService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 负责处理播放列表
 *
 * Created by legend on 2018/1/31.
 */

public class PlayHelper {

    private static volatile PlayHelper playHelper;

    private static IHelper playService;

    private List<Music> playingMusicList;

    private PlayStatus status=PlayStatus.NORMAL;

    private List<Integer> randomList;

    private List<Integer> normalList;

    private int position=-1,nor_position=-2;

    private Music current_music;

    public static final int PLAY=0x0000100;

    public static final int PAUSE=0x0000200;

    private PlayHelper() {

    }

    public static void newInstance(PlayService service){
        playService=service;
        if (playHelper==null){
            synchronized (PlayHelper.class) {
                playHelper = new PlayHelper();
            }
        }

    }

    public static PlayHelper create(){
        return playHelper;
    }

    private void play(Music music){
        if (music==null){
            Log.w("playhelper-->>","##the music is null!");
            return;
        }

        postToList(this.current_music,music);

        this.current_music=music;
        playService.playMusic(music);

        playStatusChange(PLAY);

//        Log.d("playing-position---->>>",position+"");

    }

    public void start(){
        playService.startMusic();
        playStatusChange(PLAY);
    }



    //外部调用播放,比如点击列表，点击专辑，或是搜索结果列表
    public void playAndUpdate(Music music,List<Music> list,int position){
        if (music==null||list==null||position>list.size()||position<0){
            return;
        }

        play(music);//通知播放

        this.position=position;

        updateList(list);//更新播放列表

//        changePager(position,music);//通知

    }

    public void pause(){
        playService.pauseMusic();
        playStatusChange(PAUSE);
    }

    private void stop(){
        playService.stop();
        playStatusChange(PAUSE);

    }

    private void previous(){
        if (position-1<0){
            position=0;
        }else {
            position--;
        }

        Music music=getNextMusic(position);

        play(music);

    }

    //提供ViewPager执行上一曲
    public void pagerToPrevious(){
        previous();
    }

    //提供button等控件执行上一曲操作
    public void buttonToPrevious(){
        previous();

        if (status.equals(PlayStatus.RANDOM)){

            changePager(position,randomList);
        }else {
            changePager(position,normalList);
        }

    }

    //手动播放下一首
    private void next(){
        switch (status){
            case CIRCULATION:
                if (position+1>=playingMusicList.size()){
                    position=0;
                }else {
                    position++;
                }

                break;
            default:
                if (position+1>=playingMusicList.size()){

                    stop();
                    return;
                }else {
                    position++;
                }
                break;
        }

        Music music=getNextMusic(position);
        play(music);

    }

    //提供button等控件执行下一曲
    public void buttonToNext(){
        next();

        //改变pager
        if (status.equals(PlayStatus.RANDOM)){

            changePager(position,randomList);
        }else {
            changePager(position,normalList);
        }

    }

    //提供ViewPager执行下一曲
    public void pagerToNext(){
        next();
    }


    //自动播放下一首
    public void autoNext(){
        if (status.equals(PlayStatus.SINGLE)){

            playService.startMusic();

        }else {
            next();
        }

        //改变pager
        if (status.equals(PlayStatus.RANDOM)){

            changePager(position,randomList);
        }else {
            changePager(position,normalList);
        }
    }

    /**
     * 设置播放模式
     * @param status 模式
     */
    public void setStatus(PlayStatus status){
        this.status=status;
//        if (status.equals(PlayStatus.SINGLE)){
//            playService.single(true);
//        }else {
//            playService.single(false);
//        }

        if (status.equals(PlayStatus.RANDOM)){
            initRandomList();
        }else {
            if (nor_position>=0){
                position=nor_position;
            }else {
                position=0;
            }

            initNormalList();
        }
    }

    /**
     * 检测播放列表是否已经存在并相等
     * 同时根据当前播放模式进行相应调整
     * 此方法仅提供列表点击播放使用
     * @param musicList 需要播放的列表
     */
    private void updateList(List<Music> musicList){


        if (playingMusicList==null){

            this.playingMusicList=musicList;

//            initNormalList();
//            changeList(playingMusicList);

            //通知
//            changePlayList(playingMusicList);
        }else if (!this.playingMusicList.equals(musicList)){

            this.playingMusicList=musicList;
//            initNormalList();
//            changeList(playingMusicList);

//            if (status.equals(PlayStatus.RANDOM)){
//                initRandomList();
//            }
            //通知
//            changePlayList(playingMusicList);
        }

        //无论列表是否发生变化，只要点击过列表进行播放，则对随机数组再次初始化
        if (status.equals(PlayStatus.RANDOM)){
            initRandomList();
        }else {
            initNormalList();
        }
    }

    /**
     * 实例化普通列表下标
     * 完成后传递给pager以改变pager状态
     */
    private void initNormalList(){

        //恢复普通模式
//        if (normalList!=null&&normalList.size()==playingMusicList.size()){
//            changePager(position,normalList);
//
//            Log.d("resume---->>","恢复！！");
//
//            return;
//        }

        //重新传入playlist后重新实例化
        canScroll(false);

        normalList=new ArrayList<>();
        for (int i=0;i<playingMusicList.size();i++){
            normalList.add(i);
        }

        changePager(position,normalList);

        canScroll(true);
//        changePlayList(normalList);
    }

//    //通知adapter播放列表改变
//    private void changePlayList(List<Integer> integers){
//
//        RxBus.getDefault().post(new PlayListEvent(integers));
//    }

//    private void changeList(List<Music> musicList){
//        RxBus.getDefault().post(new PlayListEvent(musicList));
//    }

    //初始化randomList
    //randomList负责记录播放列表下标
    //完成后传给pager以改变pager状态
    private void initRandomList(){

        canScroll(false);

        randomList=new ArrayList<>();

        for (int i=0;i<playingMusicList.size();i++){
            if (i!=position) {
                randomList.add(i);
            }
        }

        Collections.shuffle(randomList);

        //将正在播放的歌曲放入到队列第一个
//        if (randomList.contains(position)){
//            randomList.remove(position);
            randomList.add(0,position);
            nor_position=position;//备份position
            position=0;//还原position

//        Log.d("size--->>",playingMusicList.get(randomList.get(0)).getSongName()+"");

//        }

        changePager(position,randomList);

        canScroll(true);
//        changePlayList(randomList);

    }

    public Music getPagerMusic(int position){
        Music music;

        switch (status){
            case RANDOM:
                music=getCurrentMusic(getRandom(position));

                break;
            default:
                music=getCurrentMusic(position);
                break;
        }

        return music;

    }

    private int getRandom(int p){

        return randomList.get(p);
    }

    private Music getCurrentMusic(int position){

        return playingMusicList.get(position);
    }

    public List<Music> getPlayingMusicList() {
        return playingMusicList;
    }

    public PlayStatus getStatus() {
        return status;
    }

    public List<Integer> getRandomList() {
        return randomList;
    }

    //当前播放列表顺序
    public int getPosition() {
        return position;
    }

    //所有播放歌曲真正顺序
    public int getNor_position() {
        return nor_position;
    }

    public Music getCurrent_music() {
        return current_music;
    }

    private Music getNextMusic(int position){

        Music music;

        switch (status){
            case RANDOM:

                int p=randomList.get(position);
                nor_position=p;//记住当前播放歌曲的真正下标，以便恢复
                music=playingMusicList.get(p);
                break;
            default:

                music=playingMusicList.get(position);
                break;
        }
        return music;
    }

    //通知改变pager
    private void changePager(int position,List<Integer> integers){

        RxBus.getDefault().post(new PagerChangeEvent(position,integers));
    }

    //是否允许ViewPager进行滑动
    //在设置playlist时调用
    private void canScroll(boolean scroll){
        RxBus.getDefault().post(new PlayListChangeEvent(scroll));
    }

    //改变播放状态
    private void playStatusChange(int status){
        RxBus.getDefault().post(new PlayEvent(status));

    }

    //通知列表更改状态
    private void postToList(Music pre_music,Music current_music){

        RxBus.getDefault().post(new ListStatusEvent(pre_music,current_music));

    }

    public boolean isPlaying(){
        return playService.isPlaying();
    }

    /**
     * 添加下一曲
     * @param music
     */
    public void addNextMusic(Music music){

        if (this.playingMusicList==null){

            Toast.makeText(OverApplication.getContext(),"当前播放列表为空",Toast.LENGTH_SHORT).show();
            return;
        }



        if (this.status.equals(PlayStatus.RANDOM)){//仅在randomlist里添加真实下标，不改变random状态

            int position=this.nor_position;

            this.playingMusicList.add(position+1,music);

            int random_positon=this.randomList.indexOf(position);//获取歌曲真实下标
//            Log.d("ran--->>",random_positon+"");
            this.randomList.add(random_positon+1,position+1);
            changePager(random_positon,randomList);//改变pager
        }else {
            int position=this.position;

            this.playingMusicList.add(position+1,music);

            this.normalList.add(position+1,position+1);

//            Log.d("position--->>",this.position+"");


//            for (int i=position+2;i<this.normalList.size();i++){
//                int p=this.normalList.get(i);
//                p++;
//                this.normalList.remove(i);
//                this.normalList.add(i,p);
//            }

            initNormalList();
        }
    }
}

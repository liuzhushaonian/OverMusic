package com.app.legend.overmusic.utils;

import android.util.Log;
import android.widget.Toast;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.BigPagerChangeEvent;
import com.app.legend.overmusic.event.ListStatusEvent;
import com.app.legend.overmusic.event.PagerChangeEvent;
import com.app.legend.overmusic.event.PagerNotifyChangeEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayListChangeEvent;
import com.app.legend.overmusic.event.PlayingMusicChangeEvent;
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

    private int position=-1,nor_position=-2,true_position=-1;

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


    /**
     * 提供通知按钮播放与暂停
     */
    public void playOrPause(){

        if (isPlaying()){
            pause();
        }else {
            start();
        }
    }



    /**
     * 播放
     * @param music
     */
    private void play(Music music){
        if (music==null){
            Log.w("playhelper-->>","##the music is null!");
            return;
        }

        postToList(this.current_music,music);

        this.current_music=music;
        playService.playMusic(music);

        playStatusChange(PLAY);//通知改变播放按钮
        playingMusicChange();//通知改变播放音乐


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

        getMusicByPosition(position);

//        changePager(position,music);//通知

    }

    //仅提供播放页面的播放列表点击播放
    public void playMusicByClickPlayingList(Music music,int position){

        play(music);
        this.position=position;
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

        Music music=getMusicByPosition(position);

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

        int limit=-1;

        if (this.status.equals(PlayStatus.RANDOM)){
            limit=this.randomList.size();
        }else {
            limit=this.normalList.size();
        }


        switch (status){
            case CIRCULATION:
                if (position+1>=limit){
                    position=0;
                }else {
                    position++;
                }

                break;
            default:
                if (position+1>=limit){

                    stop();
                    return;
                }else {
                    position++;
                }

                break;
        }

        Music music=getMusicByPosition(position);
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

        if (this.status.equals(status)){
            return;
        }

        boolean resume=false;

        if (this.status.equals(PlayStatus.RANDOM)){
            resume=true;
        }else {
            resume=false;
        }

        this.status=status;

        if (resume){
            true_position=randomList.get(position);//恢复正常

            resumeNormalList();
            //切换到非随机
        }else if (this.status.equals(PlayStatus.RANDOM)){

            resumeRandomList();
            //切换到随机
        }

    }

    /**
     * 检测播放列表是否已经存在并相等
     * 同时根据当前播放模式进行相应调整
     * 此方法仅提供列表点击播放使用
     * @param musicList 需要播放的列表
     */
    private void updateList(List<Music> musicList){


        //直接复制一个，不要引用
        if (playingMusicList==null){

            this.playingMusicList=new ArrayList<>();

            this.playingMusicList.addAll(musicList);

        }else if (!this.playingMusicList.equals(musicList)){

            this.playingMusicList=new ArrayList<>();

            this.playingMusicList.addAll(musicList);

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

        //重新传入playlist后重新实例化
//        canScroll(false);

        normalList=new ArrayList<>();
        for (int i=0;i<playingMusicList.size();i++){
            normalList.add(i);
        }

        changePager(position,normalList);

//        canScroll(true);

        Log.d("position--->>",position+"");

    }

    /**
     * 恢复普通列表
     */
    private void resumeNormalList(){
        if (true_position>=0) {
            this.position = true_position;

        }

        changePager(this.position,normalList);

    }


    //初始化randomList
    //randomList负责记录播放列表下标
    //完成后传给pager以改变pager状态
    private void initRandomList(){

//        canScroll(false);
        randomList=new ArrayList<>();

        for (int i=0;i<playingMusicList.size();i++){
            if (i!=position) {
                randomList.add(i);
            }
        }

        Collections.shuffle(randomList);


        randomList.add(0, position);
        position = 0;//还原position

        changePager(position,randomList);

//        canScroll(true);
//        changePlayList(randomList);

    }

    /**
     * 恢复随机列表
     */
    private void resumeRandomList(){
        randomList=new ArrayList<>();

        for (int i=0;i<playingMusicList.size();i++){
            if (i!=true_position) {
                randomList.add(i);
            }
        }

        Collections.shuffle(randomList);

        this.randomList.add(0,true_position);

        this.position=0;

        Log.d("true--->>",normalList.get(true_position)+"");

        Log.d("randomlist-->>",randomList.get(0)+"");

        changePager(position,randomList);

    }


    public Music getPagerMusic(int position){
        Music music;

        int p=-1;

        switch (status){
            case RANDOM:
                p=getRandom(position);

                music=getCurrentMusic(p);

                break;
            default:
                p=this.normalList.get(position);

                music=getCurrentMusic(p);
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

    private Music getMusicByPosition(int position){

        Music music;

        switch (status){
            case RANDOM:

                int p=randomList.get(position);
                true_position=p;//记住当前播放歌曲的真正下标，以便恢复
                music=playingMusicList.get(p);
                break;
            default:
                true_position=position;
                music=playingMusicList.get(position);
                break;
        }
        return music;
    }

    //通知改变pager
    private void changePager(int position,List<Integer> integers){

        RxBus.getDefault().post(new PagerChangeEvent(position,integers));
        RxBus.getDefault().post(new BigPagerChangeEvent(position,integers));
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

    private void playingMusicChange(){
        RxBus.getDefault().post(new PlayingMusicChangeEvent(this.current_music));
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

        /**
         * 先判断当前列表是否存在music实例，若是已存在，则在相关列表添加坐标，若是不存在，则将这个music实例添加到列表，然后再获取坐标并设置到相关列表。
         * 机智如我😎
         */
        int p=-1;

        if (this.playingMusicList.contains(music)){

            //已存在实例
            p=this.playingMusicList.indexOf(music);
        }else {
            //不存在实例

            this.playingMusicList.add(music);

            p=this.playingMusicList.size()-1;

        }


        if (this.status.equals(PlayStatus.RANDOM)){//仅在randomlist里添加真实下标，不改变random状态

            this.randomList.add(position+1,p);

            changePager(position,this.randomList);
        }else {

            this.normalList.add(position+1,p);
            changePager(position,this.normalList);
        }

    }

    /**
     * 删除音乐
     * @param position
     */
    public void deleteMusicPosition(int position){

        int p=-1;

        if (this.status.equals(PlayStatus.RANDOM)){

            p=this.randomList.get(position);
        }else {
            p=this.normalList.get(position);
        }

        if (this.normalList!=null&&this.normalList.contains(p)){

            this.normalList.remove(p);

        }

        if (this.randomList!=null&&this.randomList.contains(p)){
            this.randomList.remove(p);

        }

        if (position<this.position){

            this.position--;

        }else if (position==this.position){

            stop();

            if (this.status.equals(PlayStatus.RANDOM)){
                if (this.position+1<this.randomList.size()){
                    this.position++;
                }else if (this.position-1>0){
                    this.position--;
                }else {

                    //列表为空
                    this.position=0;
                }


            }else {

                if (this.position+1<this.normalList.size()){
                    this.position++;
                }else if (this.position-1>0){
                    this.position--;
                }else {

                    //列表为空
                    this.position=0;
                }


            }



        }

        RxBus.getDefault().post(new PagerNotifyChangeEvent(10));

//        if (status.equals(PlayStatus.RANDOM)){
//            changePager(this.position,this.randomList);
//        }else {
//            changePager(this.position,this.normalList);
//        }



    }


    /**
     * 获取当前播放列表下标
     * @return
     */
    public List<Integer> getCurrentList(){
        switch (status) {
            case RANDOM:

                return randomList;
            default:
                return normalList;

        }
    }


}

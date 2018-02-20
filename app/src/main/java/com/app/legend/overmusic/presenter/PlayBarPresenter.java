package com.app.legend.overmusic.presenter;


import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AutoPagerEvent;
import com.app.legend.overmusic.event.PagerChangeEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayListChangeEvent;
import com.app.legend.overmusic.event.PlayPositionEvent;
import com.app.legend.overmusic.interfaces.IPlayBarPresenter;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 *
 * Created by legend on 2018/2/3.
 */

public class PlayBarPresenter {

    private IPlayBarPresenter playBarFragment;
    private Disposable disposable,pagerChange,playStatus,autoPager,play_position;

    public PlayBarPresenter(IPlayBarPresenter playBarFragment) {
        this.playBarFragment = playBarFragment;
        register();
    }


    //注册rxbus
    private void register(){

        //传递播放列表
        disposable= RxBus.getDefault().tObservable(PlayListChangeEvent.class).subscribe(playListChangeEvent -> {

           boolean scroll=playListChangeEvent.isScroll();

           playBarFragment.setScroll(scroll);
        });

        //传递正在播放的歌曲（从列表点击才需传递）
        pagerChange=RxBus.getDefault().tObservable(PagerChangeEvent.class).subscribe(pagerChangeEvent -> {

            int position=pagerChangeEvent.getPosition();
            List<Integer> integerList=pagerChangeEvent.getIntegerList();
            playBarFragment.setDataList(integerList);
            runToCurrentMusic(position);

//            runToCurrentMusic(music,position);

        });

        playStatus=RxBus.getDefault().tObservable(PlayEvent.class).subscribe(playEvent -> {
           int status=playEvent.getStatus();
            playBarFragment.setStatus(status);
        });

        autoPager=RxBus.getDefault().tObservable(AutoPagerEvent.class).subscribe(autoPagerEvent -> {

            runToCurrentMusic(autoPagerEvent.getPosition());
        });

        play_position=RxBus.getDefault().tObservable(PlayPositionEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(playPositionEvent -> {
            changePosition(playPositionEvent.getPosition());

        });
    }

    public void unregister(){

        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }

        if (pagerChange!=null&&!pagerChange.isDisposed()){
            pagerChange.dispose();
        }

        if (playStatus!=null&&!playStatus.isDisposed()){
            playStatus.isDisposed();
        }

        if (autoPager!=null&&!autoPager.isDisposed()){
            autoPager.isDisposed();
        }
    }

    private void runToCurrentMusic(int position){
//        PlayStatus status= PlayHelper.create().getStatus();
//        switch (status){
//            case RANDOM:
//                playBarFragment.setCurrentPager(0);
//                break;
//            default:
//
//                Log.d("presenter--position-->>",position+"");
        playBarFragment.setCurrentPager(position);
//                break;
//        }
    }

    /**
     * 恢复时自动获取数据
     */
    public void getData(){
        if (playBarFragment!=null){
            List<Integer> integerList= PlayHelper.create().getCurrentList();
            int position=PlayHelper.create().getPosition();



            playBarFragment.setDataList(integerList);

            runToCurrentMusic(position);
        }

    }



    private void changePosition(int progress){
//        Music music=PlayHelper.create().getCurrent_music();
//        float time=music.getTime()/1000;
//
//        int progress= (int) (500/time*position/1000);

        playBarFragment.setProgress(progress);

    }

}

package com.app.legend.overmusic.presenter;


import com.app.legend.overmusic.event.PagerChangeEvent;
import com.app.legend.overmusic.event.PlayEvent;
import com.app.legend.overmusic.event.PlayListChangeEvent;
import com.app.legend.overmusic.interfaces.IPlayBarPresenter;
import com.app.legend.overmusic.utils.RxBus;
import java.util.List;
import io.reactivex.disposables.Disposable;

/**
 *
 * Created by legend on 2018/2/3.
 */

public class PlayBarPresenter {

    private IPlayBarPresenter playBarFragment;
    private Disposable disposable,pagerChange,playStatus;

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

}

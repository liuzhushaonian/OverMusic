package com.app.legend.overmusic.fragment;


import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.MainActivity;
import com.app.legend.overmusic.adapter.PlayingBarAdapter;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IPlayBarPresenter;
import com.app.legend.overmusic.presenter.PlayBarPresenter;
import com.app.legend.overmusic.utils.MusicViewPager;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RoundProgressBar;

import java.util.List;


/**
 * 底部播放栏
 * A simple {@link Fragment} subclass.
 */
public class PlayBarFragment extends Fragment implements IPlayBarPresenter{

    MusicViewPager viewPager;
    PlayingBarAdapter adapter;
    PlayBarPresenter presenter;
    ImageView play_bar_button;
    int pre_position=-1;//记录当前position，也为记录上次的position，判断滑动是向左还是向右

    boolean isScroll=true;

    private RoundProgressBar progressBar;

    public PlayBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter!=null){
            presenter.unregister();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_play_bar, container, false);

        viewPager=view.findViewById(R.id.bottom_view_pager);
        scrollPager();

        play_bar_button=view.findViewById(R.id.play_bar_button);

        progressBar=view.findViewById(R.id.playing_bar_progress);

        play_bar_button.setOnClickListener(v -> {
            if (PlayHelper.create().isPlaying()){
                PlayHelper.create().pause();
            }else {
                PlayHelper.create().start();
            }

        });

        adapter=new PlayingBarAdapter(getChildFragmentManager());

        viewPager.setAdapter(adapter);
//        viewPager.setScroll(false);

        presenter=new PlayBarPresenter(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //设置当前position，记录当前position
    @Override
    public void setCurrentPager(int position) {
        isScroll=false;
        viewPager.setCurrentItem(position,false);
        pre_position=position;
//        adapter.notifyDataSetChanged();
    }

    //设置list数据，记录当前position
    @Override
    public void setDataList(List<Integer> integerList) {
//        adapter=null;
//        adapter=new PlayingBarAdapter(getFragmentManager());
//
        adapter.setPlayPositionList(integerList);
        viewPager.setAdapter(adapter);

        ((MainActivity)getActivity()).showPlayBar();

        adapter.notifyDataSetChanged();

        pre_position=viewPager.getCurrentItem();

//        if (PlayHelper.create().getStatus().equals(PlayStatus.RANDOM)){
//            viewPager.setCurrentItem(0,false);
//        }
    }

    //是否允许滑动
    @Override
    public void setScroll(boolean scroll) {
        viewPager.setScroll(scroll);
    }

    //改变按钮状态
    @Override
    public void setStatus(int status) {

        switch (status){
            case PlayHelper.PAUSE:
                VectorDrawable drawable= (VectorDrawable) getResources()
                        .getDrawable(R.drawable.ic_play_arrow_black_24dp,getActivity().getTheme());

                play_bar_button.setImageDrawable(drawable);
                break;
            case PlayHelper.PLAY:

                VectorDrawable drawable1= (VectorDrawable) getResources()
                        .getDrawable(R.drawable.ic_pause_black_24dp,getActivity().getTheme());

                play_bar_button.setImageDrawable(drawable1);

                break;
        }

    }

    @Override
    public void setProgress(int progress) {
        this.progressBar.setProgress(progress);
    }

    @Override
    public void changePager() {
        adapter.notifyDataSetChanged();
    }


    //ViewPager滑动事件
    //上一首或下一首
    private void scrollPager(){

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (isScroll) {

                    if (position > pre_position) {
                        //向左,下一曲
                        PlayHelper.create().pagerToNext();
                    } else if (position < pre_position) {
                        //向右，上一曲
                        PlayHelper.create().pagerToPrevious();
                    }
                }

                pre_position=position;

                isScroll=true;

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
//                Log.d("state---->>",state+"");
                isScroll=true;
            }
        });
    }

    private void autoChangeView(){
        if (PlayHelper.create().isPlaying()){
            play_bar_button.setImageResource(R.drawable.ic_pause_black_24dp);
        }else {
            play_bar_button.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }



}

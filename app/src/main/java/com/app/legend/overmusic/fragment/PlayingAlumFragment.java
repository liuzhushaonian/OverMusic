package com.app.legend.overmusic.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.PlayingActivity;
import com.app.legend.overmusic.adapter.LrcAdapter;
import com.app.legend.overmusic.bean.Lrc;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.SetLrcProgressEvent;
import com.app.legend.overmusic.interfaces.IPlayingAlbumFragmentPresenter;
import com.app.legend.overmusic.interfaces.LrcItemClickListener;
import com.app.legend.overmusic.presenter.PlayingAlbumFragmentPresenter;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.ImageUtil;
import com.app.legend.overmusic.utils.LrcRecyclerView;
import com.app.legend.overmusic.utils.LrcView;
import com.app.legend.overmusic.utils.LyricManager;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayingAlumFragment extends Fragment implements IPlayingAlbumFragmentPresenter{

    private ImageView imageView;
    private Music music;
    public static final String TAG = "playing_music";
    //    private LrcView lrcView;
    private LrcRecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private LrcAdapter adapter;
    private LinearSmoothScroller linearSmoothScroller;
    public boolean autoScroll = true;
    private LinearLayout center_view;
    private Lrc lrc;
    private ImageView play_button;
    private TextView time_text;
    private Button expand_button;
    private SeekBar textSizeSeekBar;
    private FrameLayout lrcView;
    private PlayingAlbumFragmentPresenter presenter;
    private TextView info;


    public PlayingAlumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_playing_alum, container, false);
        getComponent(view);
        presenter=new PlayingAlbumFragmentPresenter(this);


        setData();

        initList();
        getProgress();
        initClick();

        changeTextSize();

        return view;
    }

    private void getComponent(View view) {
        imageView = view.findViewById(R.id.play_album_book);
//        lrcView=view.findViewById(R.id.lrc_view);
        recyclerView = view.findViewById(R.id.lrc_list);
        center_view = view.findViewById(R.id.center_view);
        play_button = view.findViewById(R.id.lrc_playing_button);
        time_text = view.findViewById(R.id.lrc_time);
        expand_button=view.findViewById(R.id.lrc_expand_button);
        textSizeSeekBar=view.findViewById(R.id.text_size_seek_bar);
        lrcView=view.findViewById(R.id.lrc_view);
        info=view.findViewById(R.id.null_lrc_info);
    }

    /**
     * 实例化歌词
     * 实现滑动事件
     */
    private void initList() {

        linearLayoutManager = new LinearLayoutManager(getContext());

        linearSmoothScroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return linearLayoutManager.computeScrollVectorForPosition(targetPosition);
            }
        };

        adapter = new LrcAdapter(this);

        adapter.setTextSize(presenter.getTextSize(getActivity()));
        adapter.setColor(presenter.getLrcColor(getActivity()));

        recyclerView.setLayoutManager(linearLayoutManager);
//        linearLayoutManager.startSmoothScroll(linearSmoothScroller);
        recyclerView.setAdapter(adapter);
        recyclerView.setFragment(this);


        adapter.setListener(view -> {
            lrcView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            setScroll(false);//不允许滑动
        });

        /**
         * 滑动事件
         */
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!autoScroll){

                    if (center_view.getVisibility()==View.GONE){
                        showCenterLine(center_view);
                    }

                    if (textSizeSeekBar.getVisibility()==View.VISIBLE){

                        hideCenterLine(textSizeSeekBar);
                    }


                    getCenterItem();
                }else {

                    if (center_view.getVisibility()==View.VISIBLE){

                        hideCenterLine(center_view);
                    }

                }
            }
        });

    }


    /**
     * 获取接收到的music实例
     */
    private void setData() {

        Bundle bundle = getArguments();
        this.music = (Music) bundle.getSerializable(TAG);
        int w = getResources().getDisplayMetrics().widthPixels;
        ImageLoader.getImageLoader(getContext()).setAlbum(this.music, this.imageView, ImageLoader.BIG, w, w);

    }

    private void getLrcData(){
        presenter.getLrc(this.music);

    }

    private void getProgress() {

        RxBus.getDefault().tObservable(SetLrcProgressEvent.class)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(setLrcProgressEvent -> {

            if (adapter != null) {
                adapter.changeIndex(setLrcProgressEvent.getProgress());
            }
        });
    }

    public void setCurrentItemToCenter(int index) {

        if (!autoScroll){//如果不是自动滚动则返回
            return;
        }

        //当前可见的第一项和最后一项
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();


        int count = lastItem - firstItem;

        if (count > 0) {

            int half = count / 2;

            int top = index - half;

            if (top >= 0) {
                autoScroll = true;
                linearSmoothScroller.setTargetPosition(top);
                linearLayoutManager.startSmoothScroll(linearSmoothScroller);
            }

        }

    }

    public void getCenterItem() {


        //当前可见的第一项和最后一项
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();

        for (int i = firstItem; i <= lastItem; i++) {

            View view = linearLayoutManager.findViewByPosition(i);
            if (view != null) {
                if (ifCenter(view)) {
                    adapter.setCenter(i);
                }
            }
        }
    }


    private boolean ifCenter(View view) {

        int half_screen = getResources().getDisplayMetrics().widthPixels / 2;
        int top_center = half_screen - view.getHeight();
        int ruler = view.getTop();
        return (half_screen > ruler && ruler >= top_center);
    }

    /**
     * 在中心轴设置歌词对应的时间
     * @param lrc
     */
    public void showTime(Lrc lrc) {

        this.lrc = lrc;
        long t = lrc.getTime();
        if (t >= 0) {
            String time = Mp3Util.formatTime(lrc.getTime());

            time_text.setText(time);

        } else {
            time_text.setText("");
        }
    }


    /**
     * 点击事件
     */
    private void initClick() {

        //播放按钮点击事件
        play_button.setOnClickListener(v -> {
            if (this.lrc == null) {
                return;
            }

            if (this.lrc.getTime() >= 0) {

                ((PlayingActivity) getActivity()).postToChangeProgress((int) lrc.getTime());
                cancelTheCenter();//取消显示
                hideCenterLine(center_view);//取消中间线
            }

        });

        //扩展按钮点击事件
        expand_button.setOnClickListener(this::showDialog);

        //封面图点击事件
        imageView.setOnClickListener(v -> {
//            presenter.getLrc(music);//获取歌词

            getLrcData();

            lrcView.setVisibility(View.VISIBLE);

            imageView.setVisibility(View.GONE);
            setScroll(true);//恢复滑动
        });

    }

    public void cancelTheCenter(){

        handler.sendEmptyMessage(0);

    }

    Handler handler=new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:

                    adapter.setCenter(-1);

                    break;

            }

        }
    };


    private void showCenterLine(View view){

        if (view==null){
            return;
        }

        ObjectAnimator alpha= ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(1000);

        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationEnd(animation);

                view.setVisibility(View.VISIBLE);

            }
        });

        alpha.start();

    }

    private void hideCenterLine(View view){

        if (view==null){
            return;
        }

        ObjectAnimator alpha= ObjectAnimator.ofFloat(view,"alpha",1,0).setDuration(1000);

        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });

        alpha.start();

    }


    private void showDialog(View view){

        PopupMenu popupMenu=new PopupMenu(getContext(),view);

        popupMenu.inflate(R.menu.lrc_popup_menu);

        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){
                case R.id.lrc_size:

                    setProgress(adapter.getTextSize());

                    showCenterLine(this.textSizeSeekBar);
                    break;
                case R.id.lrc_waning:
                    showDialogInfo();
                    break;

                case R.id.get_tLrc://获取翻译歌词

                    presenter.getTLrc(getActivity(),music);
                    break;
            }


            return true;
        });

        popupMenu.show();

    }


    //改变歌词大小
    private void changeTextSize(){

        float defaultSize=getResources().getDimension(R.dimen.lrc_text_size);

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    float size=progress+defaultSize;
                    presenter.saveTextSize(getActivity(),size);
                    adapter.setTextSize(size);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                hideCenterLine(textSizeSeekBar);

            }
        });

    }

    //设置歌词字体大小进度
    private void setProgress(float progress){

        float d=getResources().getDimension(R.dimen.lrc_text_size);

        int p= (int) (progress-d);

        if (p<0){
            p=0;
        }

        this.textSizeSeekBar.setProgress(p);
    }

    /**
     * 展示歌词错误信息
     */
    private void showDialogInfo() {
        presenter.showDialog(getActivity(),this.music);
    }


    @Override
    public void setLrcList(List<Lrc> lrcList) {

        if (lrcList==null||lrcList.isEmpty()){
            //无歌词，不展示，直接填写信息
            info.setVisibility(View.VISIBLE);

            info.setOnClickListener(v -> {

                lrcView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                setScroll(true);
            });

        }else {
            info.setVisibility(View.GONE);
            adapter.setLrcList(lrcList);
        }
    }

    @Override
    public void setTLrcList(List<Lrc> lrcList) {
        adapter.setTlrcList(lrcList);
    }

    private void setScroll(boolean scroll){

        ((PlayingActivity)getActivity()).setPagerSrcoll(scroll);
    }

}

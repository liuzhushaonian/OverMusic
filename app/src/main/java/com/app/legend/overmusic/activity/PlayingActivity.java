package com.app.legend.overmusic.activity;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.MusicListAdapter;
import com.app.legend.overmusic.adapter.PlayAlbumAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AutoPagerEvent;
import com.app.legend.overmusic.event.SeekEvent;
import com.app.legend.overmusic.fragment.PlayingAlumFragment;
import com.app.legend.overmusic.interfaces.IPlayingPresenter;
import com.app.legend.overmusic.presenter.PlayingPresenter;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.MusicViewPager;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.PlayStatus;
import com.app.legend.overmusic.utils.RxBus;
import java.util.ArrayList;
import java.util.List;

public class PlayingActivity extends BaseActivity implements View.OnClickListener,IPlayingPresenter{

    private Toolbar toolbar;
    private MusicViewPager viewPager;
    private ImageView previous,next,play,playStatus,playList;
    private SeekBar progressBar;
    private PlayAlbumAdapter albumAdapter;
    private int pre_position=-1;//记录当前position，也为记录上次的position，判断滑动是向左还是向右
    private boolean isScroll=true;//记录是否为手动滑动pager，判断是否进行上一曲下一曲操作
    private PlayingPresenter presenter;
    private PlayStatus status;
    private Music music;
    private int position=0;
    private List<PlayStatus> playStatusList;
    private boolean trackSeek=false;
    private int seek_progress=0;
    private TextView start,end;
    private LinearLayout bg;
    private CardView controller_view;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        getComponent();
        initToolbar();
        reDraw();
        initPager();
        presenter=new PlayingPresenter(PlayingActivity.this);
        initPlayStatusList();
        getMusicData();//主动获取列表

        setPosition();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (presenter!=null){
            presenter.dis();
        }

    }

    @Override
    protected void setThemeColor() {
        this.toolbar.setBackgroundColor(getThemeColor());
        this.toolbar.getBackground().setAlpha(50);
        this.controller_view.setCardBackgroundColor(getThemeColor());

        this.controller_view.getBackground().setAlpha(50);

        this.progressBar.setProgressTintList(ColorStateList.valueOf(getThemeColor()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        setThemeColor();

    }

    private void getComponent(){
        toolbar=findViewById(R.id.playing_toolbar);
        viewPager=findViewById(R.id.playing_album_book);
        progressBar=findViewById(R.id.progressBar);
        previous=findViewById(R.id.previous);
        next=findViewById(R.id.next);
        play=findViewById(R.id.play);
        playStatus=findViewById(R.id.play_status);
        playList=findViewById(R.id.play_list);
        start=findViewById(R.id.start_time);
        end=findViewById(R.id.end_time);
        bg=findViewById(R.id.playing_bg);
        controller_view=findViewById(R.id.controller_view);


        addClickListener(previous);
        addClickListener(next);
        addClickListener(play);
        addClickListener(playList);
        addClickListener(playStatus);

    }

    private void setPosition(){

        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                Log.d("progress--->>",progress+"");
                if (trackSeek) {

                    long time = music.getTime();

                    seek_progress = (int) (progress * (time / 500));

                    start.setText(Mp3Util.formatTime((long) seek_progress));

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                trackSeek=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                trackSeek=false;
                postToChangeProgress(seek_progress);
                seek_progress=0;//恢复，避免影响之后的播放
            }
        });

    }

    private void reDraw(){

        LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) viewPager.getLayoutParams();
        int w=getResources().getDisplayMetrics().widthPixels;
        layoutParams.height=w;
        viewPager.setLayoutParams(layoutParams);

    }

    private void addClickListener(View v){
        v.setOnClickListener(this);
    }

    private void initToolbar(){

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        toolbar.getBackground().setAlpha(50);
    }

    private void initPager(){
        this.albumAdapter=new PlayAlbumAdapter(getSupportFragmentManager());

        this.viewPager.setAdapter(albumAdapter);

        this.viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isScroll){

                    if (position > pre_position) {

                        //向左,下一曲
                        PlayHelper.create().pagerToNext();
                    } else if (position < pre_position) {
                        //向右，上一曲

                        PlayHelper.create().pagerToPrevious();
                    }
                }

                //isScroll=true;
                pre_position=position;//记录位置

                changeSmallPager(position);//改变上一个ViewPager

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

                isScroll=true;//判断为手动滑动的瞬间直接改变状态
            }
        });
    }

    //同时改变另一个ViewPager的状态
    private void changeSmallPager(int position){
        RxBus.getDefault().post(new AutoPagerEvent(position));

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.play:

                if (PlayHelper.create().isPlaying()){
                    PlayHelper.create().pause();
                }else {
                    PlayHelper.create().start();
                }

                break;
            case R.id.previous:

                PlayHelper.create().buttonToPrevious();

                break;
            case R.id.next:

                PlayHelper.create().buttonToNext();
                break;
            case R.id.play_status:

                this.position++;
                if (position>3){
                    position=0;
                }

                changeStatusView(this.playStatusList.get(position));

                break;
            case R.id.play_list:
                openBottomSheetMenu();
                break;
        }


    }

    @Override
    public void setCurrentPager(int position) {
        isScroll=false;//因为直接这样设置当前pager也会触发滑动事件，所以需要改为false
        this.viewPager.setCurrentItem(position,false);
        this.pre_position=position;
    }

    @Override
    public void setData(List<Music> musicList) {
//        albumAdapter.setPositionList(positionList);
        albumAdapter.setMusicList(musicList);

    }

    @Override
    public void setStatus(PlayStatus status) {
        this.status=status;
        //如果是单曲循环，则无法滑动
        if (status.equals(PlayStatus.SINGLE)){
            this.viewPager.setScroll(false);
        }else {
            this.viewPager.setScroll(true);
        }
    }

    @Override
    public void setPlayingStatus(int playingStatus) {
        if (playingStatus==PlayHelper.PAUSE){
            this.play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }else {
            this.play.setImageResource(R.drawable.ic_pause_black_24dp);
        }
    }

    /**
     * 获取正在播放的音乐后，开始渲染UI
     */
    private void changeView(){
        if (this.music==null){
            return;
        }

        this.end.setText(Mp3Util.formatTime(music.getTime()));
        this.toolbar.setTitle(music.getSongName());
        presenter.getProgress();
        presenter.getBitmap(music);

    }

    /**
     * 主动获取列表
     */
    private void getMusicData(){
        autoSetPlayingStatus();

        changeView();

        presenter.getData();

    }



    /**
     * 打开后自动判断播放状态并设置为正确的状态
     *
     *
     */
    private void autoSetPlayingStatus(){

         if (PlayHelper.create().isPlaying()){
             this.play.setImageResource(R.drawable.ic_pause_black_24dp);
         }else {
             this.play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
         }

         this.music=PlayHelper.create().getCurrent_music();

         end.setText(Mp3Util.formatTime(music.getTime()));

         changeStatusView(PlayHelper.create().getStatus());


    }

    @Override
    public void setMusic(Music music){
        this.music=music;
        changeView();
    }

    @Override
    public void setPlayProgress(int position,long progress) {

        if (!trackSeek){
            progressBar.setProgress(position);
            start.setText(Mp3Util.formatTime(progress));
        }

    }

    /**
     * 打开底部菜单
     */
    @Override
    public void openBottomSheetMenu() {
        View view= LayoutInflater.from(PlayingActivity.this).inflate(R.layout.bottom_sheet_dialog_content_view,null,false);

        RecyclerView recyclerView=view.findViewById(R.id.bottom_list);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(PlayingActivity.this);

        MusicListAdapter adapter=new MusicListAdapter();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
//        adapter.setMusicPositionList(PlayHelper.create().getCurrentMusicList());
        adapter.setMusicList(PlayHelper.create().getCurrentMusicList());

        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(PlayingActivity.this);

        bottomSheetDialog.setContentView(view);

//        view.setBackgroundColor(getResources().getColor(R.color.colorBlue));
//        bottomSheetDialog.getDelegate()
//                .findViewById(android.support.design.R.id.design_bottom_sheet)
//                .setBackgroundColor(getResources().getColor(R.color.colorTransparent));

        View v=bottomSheetDialog.getDelegate()
                .findViewById(android.support.design.R.id.design_bottom_sheet);
        if (v!=null) {
            v.setBackground(getResources().getDrawable(R.drawable.shape_bottom, getTheme()));

        }

        bottomSheetDialog.show();

    }

    /**
     * 恢复view的状态
     * 由其他页面恢复至此时才需要进行
     */
    private void resumeView(){


    }


    private void initPlayStatusList(){
        playStatusList=new ArrayList<>();
        playStatusList.add(PlayStatus.NORMAL);
        playStatusList.add(PlayStatus.CIRCULATION);
        playStatusList.add(PlayStatus.SINGLE);
        playStatusList.add(PlayStatus.RANDOM);

        this.position=playStatusList.indexOf(PlayHelper.create().getStatus());
    }

    //改变显示
    private void changeStatusView(PlayStatus status){

        PlayHelper.create().setStatus(status);

        switch (status){
            case RANDOM:
                playStatus.setImageResource(R.drawable.ic_shuffle_black_24dp);

                break;
            case CIRCULATION:
                playStatus.setImageResource(R.drawable.ic_repeat_black_24dp);

                break;
            case NORMAL:
                playStatus.setImageResource(R.drawable.ic_normal_black_24dp);

                break;
            case SINGLE:
                playStatus.setImageResource(R.drawable.ic_repeat_one_black_24dp);

                break;
        }
    }


    @Override
    public void startActivityForArtist(Artist artist){

        Intent intent=new Intent(PlayingActivity.this,MainActivity.class);
        intent.putExtra("artist",artist);
        startActivity(intent);

    }

    @Override
    public void startActivityForAlbum(Album album){
        Intent intent=new Intent(PlayingActivity.this,MainActivity.class);
        intent.putExtra("album",album);
        startActivity(intent);
    }

    @Override
    public void changeViewPager() {
        this.albumAdapter.notifyDataSetChanged();
    }

    /**
     * 设置高斯模糊背景
     * @param blurBitmap
     */
    @Override
    public void setBlurBitmap(Bitmap blurBitmap) {

        if (blurBitmap!=null) {

            BitmapDrawable drawable = new BitmapDrawable(getResources(), blurBitmap);


            bg.setBackground(drawable);
        }else {

            bg.setBackground(null);
        }
    }

    @Override
    public void setScroll(boolean scroll) {
        if (this.viewPager!=null){
            this.viewPager.setScroll(scroll);
        }
    }


    public void postToChangeProgress(int seek_progress){

        RxBus.getDefault().post(new SeekEvent(seek_progress));

    }

    public void setPagerSrcoll(boolean srcoll){

        this.viewPager.setScroll(srcoll);

    }

}

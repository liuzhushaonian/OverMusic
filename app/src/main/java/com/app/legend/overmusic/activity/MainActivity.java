package com.app.legend.overmusic.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.SingleAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.event.AddAlbumMusicToListEvent;
import com.app.legend.overmusic.event.AddArtistMusicToList;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.event.AddMusicToListEvent;
import com.app.legend.overmusic.event.RenamePlayListEvent;
import com.app.legend.overmusic.fragment.AlbumFragment;
import com.app.legend.overmusic.fragment.AlbumInfoFragment;
import com.app.legend.overmusic.fragment.ArtistFragment;
import com.app.legend.overmusic.fragment.ArtistInfoFragment;
import com.app.legend.overmusic.fragment.MusicFragment;
import com.app.legend.overmusic.fragment.PlayListFragment;
import com.app.legend.overmusic.interfaces.IMainPresenter;
import com.app.legend.overmusic.presenter.MainPresenter;
import com.app.legend.overmusic.service.PlayService;
import com.app.legend.overmusic.utils.RxBus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity implements IMainPresenter{

    MainPresenter presenter;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    List<Fragment> fragmentList;
    SingleAdapter adapter;
    LinearLayout bottom_layout,linearLayout;
    FrameLayout frameLayout;
    Disposable disposable,music_dis,album_dis,artist_dis;
    PlayListFragment playListFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter=new MainPresenter(MainActivity.this);
        getComponent();

        reginst();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (disposable!=null&&!disposable.isDisposed()){
//            disposable.dispose();
//        }
//
//        if (music_dis!=null&&!music_dis.isDisposed()){
//            music_dis.dispose();
//        }
//
//        if (album_dis!=null&&!album_dis.isDisposed()){
//            album_dis.dispose();
//        }
//
//        if (artist_dis!=null&&!artist_dis.isDisposed()){
//            artist_dis.dispose();
//        }
        dispose(disposable);
        dispose(music_dis);
        dispose(album_dis);
    }


    @Override
    protected void onStart() {
        super.onStart();
        initToolbar();
        initTab();
        initViewPager();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        searchInfo(intent);
    }

    private void dispose(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    private void getComponent(){

        toolbar=findViewById(R.id.main_toolbar);
        tabLayout=findViewById(R.id.main_tabLayout);
        viewPager=findViewById(R.id.main_viewPager);
        bottom_layout=findViewById(R.id.bottom_layout);
        linearLayout=findViewById(R.id.linearLayout);
        frameLayout=findViewById(R.id.fragment_container);
    }

    private void initTab(){
        if (tabLayout.getTabCount()>0){//防止重复添加
            return;
        }
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getText(R.string.music)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getText(R.string.album)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getText(R.string.artist)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getText(R.string.list)));

        //绑定ViewPager，使之一同滑动
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
    }

    private void initToolbar(){
        toolbar.setFitsSystemWindows(true);

        toolbar.setTitle(getResources().getString(R.string.music));

        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_pager_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                openSearchActivity();
                break;
            case R.id.play_all:
                playAllMusic();
                break;
            case R.id.list_queue:

                //暂时不写
                break;
            case R.id.new_list:

                addNewList();
                break;

        }
        return true;
    }

    private void initViewPager(){

        if (fragmentList!=null){
            return;
        }
        fragmentList=new ArrayList<>();
        MusicFragment musicFragment=new MusicFragment();
        AlbumFragment albumFragment=new AlbumFragment();
        ArtistFragment artistFragment=new ArtistFragment();
        PlayListFragment playListFragment=new PlayListFragment();
        this.playListFragment=playListFragment;

        fragmentList.add(musicFragment);
        fragmentList.add(albumFragment);
        fragmentList.add(artistFragment);
        fragmentList.add(playListFragment);

        adapter=new SingleAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        adapter.setFragmentList(fragmentList);
        viewPager.setOffscreenPageLimit(3);

        //绑定TabLayout，使之一同滑动
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

    //提供Fragment显示playbar
    public void showPlayBar(){
        if (bottom_layout.getVisibility()==View.VISIBLE){
            return;
        }

        int margin=getResources().getDimensionPixelSize(R.dimen.bottom_play_bar);
        ObjectAnimator animator=ObjectAnimator.ofFloat(bottom_layout,"translationY",margin,0);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                bottom_layout.setVisibility(View.VISIBLE);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                bottom_layout.setVisibility(View.VISIBLE);
                setMargin(margin);
            }
        });

        animator.setDuration(300).start();
    }

    private void setMargin(int margin){
        RelativeLayout.LayoutParams layoutParams= (RelativeLayout.LayoutParams) linearLayout.getLayoutParams();
        layoutParams.bottomMargin=margin;
        linearLayout.setLayoutParams(layoutParams);
        DrawerLayout.LayoutParams layoutParams1= (DrawerLayout.LayoutParams) frameLayout.getLayoutParams();
        layoutParams1.bottomMargin=margin;
        frameLayout.setLayoutParams(layoutParams1);
    }

    /**
     * 添加Fragment
     * @param fragment
     */
    public void addFragment(Fragment fragment){
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void reginst(){
        disposable=RxBus.getDefault().tObservable(AddFragmentEvent.class).subscribe(addFragmentEvent ->{
            Fragment fragment=addFragmentEvent.getFragment();
            addFragment(fragment);
        });

        music_dis=RxBus.getDefault().tObservable(AddMusicToListEvent.class).subscribe(addMusicToListEvent -> {
           addPopupMenu(addMusicToListEvent.getMusic());
        });

        album_dis=RxBus.getDefault().tObservable(AddAlbumMusicToListEvent.class).subscribe(addAlbumMusicToListEvent -> {
            addPopupMenu(addAlbumMusicToListEvent.getAlbum());
        });

        artist_dis=RxBus.getDefault().tObservable(AddArtistMusicToList.class).subscribe(addArtistMusicToList -> {
            addPopupMenu(addArtistMusicToList.getArtist());
        });


    }

    public void removeFragment(Fragment fragment) {
        FragmentManager fragmentManager=getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentManager.popBackStack();//模拟栈操作，将栈顶null去掉
        fragmentTransaction.commit();
    }


    public void setToolbarMenu(Menu menu,int reMenu){

        getMenuInflater().inflate(reMenu,menu);

//        Log.d("menu--->>",menu.toString()+"");
    }

    public void addToolbar(Toolbar toolbar){
        setSupportActionBar(toolbar);
    }


    /**
     * 添加音乐至列表事件
     * @param music
     */
    private void addPopupMenu(Music music){
        presenter.setPopupMenu(this,music);
    }

    private void addPopupMenu(Album album){
        presenter.setPopupMenu(this,album);
    }

    private void addPopupMenu(Artist artist){
        presenter.setPopupMenu(this,artist);
    }

    //播放全部音乐
    private void playAllMusic(){
        presenter.playAllMusic();
    }

    //添加新列表
    private void addNewList(){
        presenter.newList(this);
    }
    /**
     * 改变playlist页面
     */
    @Override
    public void changePlayListData() {
        if (this.playListFragment!=null){
            playListFragment.getData();
        }
    }

    public void openSearchActivity(){
        Intent intent=new Intent(MainActivity.this,SearchActivity.class);
//        intent.putExtra("qq","228875654");

        startActivity(intent);

    }

    private void searchInfo(Intent intent){
        Artist artist= (Artist) intent.getSerializableExtra("artist");
        Album album= (Album) intent.getSerializableExtra("album");
        if (artist!=null){
            Log.d("info1--->>","artist");
            toArtistFragment(artist);
        }else if (album!=null){
            Log.d("info2--->>","album");
            toAlbumFragment(album);
        }
    }

    private void toArtistFragment(Artist artist){

        ArtistInfoFragment fragment=new ArtistInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(ArtistInfoFragment.TAG,artist);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }

    private void toAlbumFragment(Album album){
        AlbumInfoFragment fragment=new AlbumInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(AlbumInfoFragment.TAG,album);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }



}
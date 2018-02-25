package com.app.legend.overmusic.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.TintTypedArray;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.SingleAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AddAlbumMusicToListEvent;
import com.app.legend.overmusic.event.AddArtistMusicToList;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.event.AddMusicToListEvent;
import com.app.legend.overmusic.fragment.AlbumFragment;
import com.app.legend.overmusic.fragment.AlbumInfoFragment;
import com.app.legend.overmusic.fragment.ArtistFragment;
import com.app.legend.overmusic.fragment.ArtistInfoFragment;
import com.app.legend.overmusic.fragment.MusicFragment;
import com.app.legend.overmusic.fragment.PlayBarFragment;
import com.app.legend.overmusic.fragment.PlayListFragment;
import com.app.legend.overmusic.interfaces.IMainPresenter;
import com.app.legend.overmusic.presenter.MainPresenter;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity implements IMainPresenter{

    MainPresenter presenter;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    List<Fragment> fragmentList;
    SingleAdapter adapter;
    LinearLayout bottom_layout,linearLayout,bg_container;
    FrameLayout frameLayout;
    Disposable disposable,music_dis,album_dis,artist_dis;
    PlayListFragment playListFragment;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ImageView headerImage;
    private boolean set_bg=false;
    private int play=-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        presenter=new MainPresenter(MainActivity.this);
        getComponent();

        register();

        searchInfo(getIntent());

        leftMenuClick();

        resumeView(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dispose(disposable);
        dispose(music_dis);
        dispose(album_dis);
    }

    @Override
    protected void setThemeColor() {
        this.toolbar.setBackgroundColor(getThemeColor());
        this.bg_container.setBackgroundColor(getThemeColor());

        if (!set_bg) {
            Bitmap bitmap = getDefaultBg();
            if (bitmap != null) {
                this.headerImage.setImageBitmap(bitmap);
            } else {
                this.headerImage.setImageResource(R.drawable.bg);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        initToolbar();
        initTab();
        initViewPager();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setThemeColor();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        searchInfo(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt("play",play);
    }

    private void dispose(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    /**
     * 获取组件
     */
    private void getComponent(){

        toolbar=findViewById(R.id.main_toolbar);
        tabLayout=findViewById(R.id.main_tabLayout);
        viewPager=findViewById(R.id.main_viewPager);
        bottom_layout=findViewById(R.id.bottom_layout);
        linearLayout=findViewById(R.id.linearLayout);
        frameLayout=findViewById(R.id.fragment_container);
        navigationView=findViewById(R.id.left_menu);
        bg_container=findViewById(R.id.container);
        drawerLayout=findViewById(R.id.draw_layout);
        headerImage=navigationView.getHeaderView(0).findViewById(R.id.header_image);
    }

    /**
     * 实例化TabLayout
     */
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

    /**
     * 实例化toolbar
     */
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

    /**
     * 菜单点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                openSearchActivity();
                break;
            case R.id.play_all:
                playAllMusic();
                break;
//            case R.id.list_queue:
//
//                //暂时不写
//                break;
            case R.id.new_list:

                addNewList();
                break;

        }
        return true;
    }

    /**
     * 实例化ViewPager
     */
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

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setMargin(margin);

                play=1;
            }
        });

//        animator.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
////                bottom_layout.setVisibility(View.VISIBLE);
//
//            }
//        });

        animator.setDuration(300).start();
    }

    /**
     * 改变底部margin
     * @param margin
     */
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

    private void register(){
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

    /**
     * 重新实例化pager
     */
    @Override
    public void initPager() {
        fragmentList=null;
        initViewPager();
    }

    /**
     * 打开搜索页面
     */
    public void openSearchActivity(){
        Intent intent=new Intent(MainActivity.this,SearchActivity.class);
        startActivity(intent);
    }

    /**
     * 打开相关页面
     * @param intent
     */
    private void searchInfo(Intent intent){
        Artist artist= (Artist) intent.getSerializableExtra("artist");
        Album album= (Album) intent.getSerializableExtra("album");
        if (artist!=null){

            toArtistFragment(artist);
        }else if (album!=null){

            toAlbumFragment(album);
        }
    }

    /**
     * 打开artist页面
     * @param artist
     */
    private void toArtistFragment(Artist artist){

        ArtistInfoFragment fragment=new ArtistInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(ArtistInfoFragment.TAG,artist);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }

    /**
     * 打开album页面
     * @param album
     */
    private void toAlbumFragment(Album album){
        AlbumInfoFragment fragment=new AlbumInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(AlbumInfoFragment.TAG,album);
        fragment.setArguments(bundle);
        addFragment(fragment);
    }

    /**
     * 侧滑菜单点击事件
     */
    private void leftMenuClick(){

        navigationView.setNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.change_color:
                    startColorActivity();
                    break;
                case R.id.scan:
                    presenter.scanMusic();
                    break;
                case R.id.clean:
                    presenter.cleanCache();
                    break;
                case R.id.upgrade:
                    Toast.makeText(this,"更新什么的怎么可能会有~",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.about_me:
                    presenter.aboutDeveloper(this);
                    break;
                case R.id.about_app:
                    presenter.aboutApp(this);
                    break;
                case R.id.exit:

                    exit();
                    break;
            }

            drawerLayout.closeDrawers();

            return true;
        });



        navigationView.getHeaderView(0).setOnClickListener(v -> {

            openAlbum();//打开相册获取图片
//            Toast.makeText(this,"点击事件",Toast.LENGTH_SHORT).show();
        });


    }

    private void startColorActivity(){

        Intent intent=new Intent(MainActivity.this,ColorActivity.class);
        startActivity(intent);
    }

    private void exit(){
        finishAndRemoveTask();
        System.exit(0);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case 200:

                //剪切
                if (data==null){
                    return;
                }

                set_bg=true;
                startCropImage(data.getData(),this.headerImage.getWidth(),this.headerImage.getHeight());

                break;
            case 300:
                if (data==null){
                    return;
                }
                //获取剪切好之后的
                presenter.saveAndSetImage(data.getData(),this.headerImage);

                set_bg=false;
                break;

            default:

                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    private void resumeView(Bundle bundle){

        if (bundle!=null){
            int p=bundle.getInt("play");

            if (p>0){
                showPlayBar();
                PlayBarFragment fragment= (PlayBarFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                fragment.setCurrentPager(PlayHelper.create().getPosition());
            }
        }
    }

}
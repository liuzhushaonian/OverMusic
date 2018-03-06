package com.app.legend.overmusic.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.SearchHistoryAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.event.QueryEvent;
import com.app.legend.overmusic.event.SearchAlbumEvent;
import com.app.legend.overmusic.event.SearchArtistEvent;
import com.app.legend.overmusic.fragment.ArtistInfoFragment;
import com.app.legend.overmusic.fragment.SearchFragment;
import com.app.legend.overmusic.interfaces.ISearchPresenter;
import com.app.legend.overmusic.presenter.SearchPresenter;
import com.app.legend.overmusic.utils.RxBus;

import java.util.List;

import io.reactivex.disposables.Disposable;

public class SearchActivity extends BaseActivity implements ISearchPresenter{

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private SearchHistoryAdapter adapter;
    private SearchFragment fragment;
    private LinearLayout fragment_container;
    private SearchView searchView;
    private SearchPresenter presenter;
    private Disposable query_dis,artist_dis,album_dis;
    private TextView textView;
    private RelativeLayout bg_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getComponent();
        initList();
        presenter=new SearchPresenter(this);
        getHistoryData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initToolbar();
        searchEvent();
        register();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setThemeColor();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister(query_dis);
        unregister(artist_dis);
        unregister(album_dis);
    }

    private void getComponent() {
        recyclerView = findViewById(R.id.search_history);
        toolbar =findViewById(R.id.search_toolbar);
        fragment_container=findViewById(R.id.search_fragment_container);
        searchView=findViewById(R.id.search_view);
        bg_container=findViewById(R.id.search_bg_container);
    }

    private void initToolbar(){

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {

            finish();
        });
    }

    /**
     * 搜索事件
     */
    private void searchEvent(){


        searchView.setIconifiedByDefault(true);
        searchView.setIconified(false);
        int id = searchView.getResources().getIdentifier("search_src_text", "id", getApplicationContext().getPackageName());

        this.textView=searchView.findViewById(id);
        this.textView.setFocusable(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                query(query);
//                Log.d("query--->>",query+"");
                insertQuery(query);
                closeSoftKeybord(textView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query(newText);
                return true;
            }
        });

    }

    //关闭输入法
    private void closeSoftKeybord(View view){
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(this);
        adapter=new SearchHistoryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }




    private void getHistoryData(){
        presenter.getData();
    }


    @Override
    public void setData(List<String> list) {
        adapter.setHistoryList(list);
    }

    @Override
    public void queryDataByFragment(String data) {
        SearchFragment fragment= (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        fragment.queryData(data);
    }

    /**
     * 需要保存为历史
     * @param string
     */
    private void insertQuery(String string){
        if (string.isEmpty()){

            hideFragment();
            return;
        }

        showFragment();
        presenter.queryData(string);

    }

    //不需要保存为历史
    private void query(String s){
        if (s.isEmpty()){
            hideFragment();
            return;
        }
        showFragment();
//        presenter.queryData(s);
        queryDataByFragment(s);
    }

    private void showFragment(){
        recyclerView.setVisibility(View.GONE);
        fragment_container.setVisibility(View.VISIBLE);
    }

    private void hideFragment(){
        recyclerView.setVisibility(View.VISIBLE);
        fragment_container.setVisibility(View.GONE);

        getHistoryData();//每次显示都重新获取一次数据
    }

    private void register(){

        query_dis=RxBus.getDefault().tObservable(QueryEvent.class).subscribe(queryEvent -> {
            String data=queryEvent.getData();
            query(data);
        });
        artist_dis=RxBus.getDefault().tObservable(SearchArtistEvent.class).subscribe(searchArtistEvent -> {
           startActivityForArtist(searchArtistEvent.getArtist());
        });

        album_dis=RxBus.getDefault().tObservable(SearchAlbumEvent.class).subscribe(searchAlbumEvent -> {
           startActivityForAlbum(searchAlbumEvent.getAlbum());
        });

    }

    private void unregister(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    @Override
    protected void setThemeColor() {
        this.toolbar.setBackgroundColor(getThemeColor());
        this.bg_container.setBackgroundColor(getThemeColor());
    }

    public void startActivityForArtist(Artist artist){

        Intent intent=new Intent(SearchActivity.this,MainActivity.class);
        intent.putExtra("artist",artist);
        startActivity(intent);

    }

    public void startActivityForAlbum(Album album){
        Intent intent=new Intent(SearchActivity.this,MainActivity.class);
        intent.putExtra("album",album);
        startActivity(intent);
    }




}

package com.app.legend.overmusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.MainActivity;
import com.app.legend.overmusic.adapter.MusicAdapter;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.event.DeleteMusicEvent;
import com.app.legend.overmusic.interfaces.IPlayListInfoPresenter;
import com.app.legend.overmusic.presenter.PlayListInfoPresenter;
import com.app.legend.overmusic.utils.Database;
import com.app.legend.overmusic.utils.RxBus;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayListInfoFragment extends BaseFragment implements IPlayListInfoPresenter{

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private MusicAdapter adapter;
    private Toolbar toolbar;
    private PlayList playList;
    private PlayListInfoPresenter presenter;

    public PlayListInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_play_list_info, container, false);
        presenter=new PlayListInfoPresenter(this);
        getComponent(view);
        getPlayList();
        initList();
        initToolbar();
        getData();
        deleteMusic();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.toolbar.setBackgroundColor(getThemeColor());
//        toolbar.getBackground().setAlpha(16);
    }

    private void getComponent(View view){
        recyclerView=view.findViewById(R.id.play_list_info_list);
        toolbar=view.findViewById(R.id.play_list_info_toolbar);
    }

    private void getPlayList(){
        Bundle bundle=getArguments();
        this.playList= (PlayList) bundle.getSerializable("play_list");

    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(getContext());
        adapter=new MusicAdapter(MusicAdapter.LIST_MUSIC);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    private void initToolbar(){
        toolbar.setPadding(0,getStatusBarHeight(),0,0);
        toolbar.setTitle(playList.getName());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            ((MainActivity)getActivity()).removeFragment(this);

        });

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlue));

    }

    private void getData(){

        if (this.playList!=null){

            presenter.getData(this.playList);

        }

    }

    @Override
    public void setData(List<Music> musicList) {
        adapter.setData(musicList);
    }


    private void deleteMusic(){
        RxBus.getDefault().tObservable(DeleteMusicEvent.class).subscribe(deleteMusicEvent -> {
            Music music=deleteMusicEvent.getMusic();
            int position=deleteMusicEvent.getPosition();
            deleteMusic(music,position);
        });
    }

    //删除音乐
    private void deleteMusic(Music music,int position){
        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                Database.getDefault().deleteMusicFromList(this.playList,music,position);
                e.onNext(1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    ((MainActivity)getActivity()).changePlayListData();
                });
    }
}

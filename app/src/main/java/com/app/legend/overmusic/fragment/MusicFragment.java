package com.app.legend.overmusic.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.MusicAdapter;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IMusicFragmentPresenter;
import com.app.legend.overmusic.presenter.MusicFragmentPresenter;
import com.app.legend.overmusic.utils.ImageLoader;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends BaseFragment implements IMusicFragmentPresenter{

    RecyclerView recyclerView;
    MusicAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    List<Music> allMusicList;
    LinearLayout linearLayout;
    MusicFragmentPresenter presenter;
    TextView info;

    private static final String[] permissionStrings=
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public MusicFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_music, container, false);

        presenter = new MusicFragmentPresenter(this);
        recyclerView = view.findViewById(R.id.all_music_list);
        linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new MusicAdapter(MusicAdapter.ALL_MUSIC);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        ((DefaultItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        linearLayout = view.findViewById(R.id.music_null_info);
        info=view.findViewById(R.id.music_info);
        getData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter!=null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.unregister();
    }

    //通过presenter获取数据
    private void getAllMusic(){
        presenter.getData();
    }

    /**
     * 检查权限并获取数据
     */
    private void getData(){
//        if (getPermission){

            getAllMusic();
//        }else {
////            getPermission();
////            getData();
//            info.setText(getResources().getString(R.string.permission_info));
//            setListData(null);
//        }
    }

    @Override
    public void setListData(List<Music> musicList){

        //判断是否为空并设置提示
        if (musicList==null){
            linearLayout.setVisibility(View.VISIBLE);
        }else if (musicList.isEmpty()){
            linearLayout.setVisibility(View.VISIBLE);
        }else {
            this.allMusicList=musicList;
            adapter.setData(allMusicList);
            linearLayout.setVisibility(View.GONE);
        }

    }

}

package com.app.legend.overmusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.AlbumAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.interfaces.IAlbumPresenter;
import com.app.legend.overmusic.presenter.AlbumFragmentPresenter;
import com.app.legend.overmusic.utils.AlbumItemSpace;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends BaseFragment implements IAlbumPresenter{

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private AlbumAdapter adapter;
    private AlbumFragmentPresenter presenter;
    private LinearLayout linearLayout;
    private TextView info;


    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_album, container, false);

        recyclerView=view.findViewById(R.id.album_recycler_view);

        layoutManager=new GridLayoutManager(getContext(),2);

        recyclerView.setLayoutManager(layoutManager);

        adapter=new AlbumAdapter();

        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new AlbumItemSpace(AlbumItemSpace.PAGER));

        presenter=new AlbumFragmentPresenter(this);

        linearLayout=view.findViewById(R.id.album_null_info);

        info=view.findViewById(R.id.album_info);

        getData();

        return view;
    }

    @Override
    public void setDataList(List<Album> albumList) {

        if (albumList==null) {

            linearLayout.setVisibility(View.VISIBLE);
        }else if (albumList.isEmpty()){
            linearLayout.setVisibility(View.VISIBLE);
        }else {
            adapter.setAlbums(albumList);
            Log.d("size---->>",albumList.size()+"");
            linearLayout.setVisibility(View.GONE);
        }
    }

    private void getData(){
//        if (getPermission) {
            presenter.getData();
//        }else {
////            getPermission();
////            getData();
//            info.setText(getResources().getString(R.string.permission_info));
//            setDataList(null);
//
//        }
    }
}

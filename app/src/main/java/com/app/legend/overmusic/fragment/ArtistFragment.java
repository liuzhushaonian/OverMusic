package com.app.legend.overmusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.ArtistAdapter;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.interfaces.IArtistPresenter;
import com.app.legend.overmusic.presenter.AlbumFragmentPresenter;
import com.app.legend.overmusic.presenter.ArtistFragmentPresenter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends BaseFragment implements IArtistPresenter{

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArtistAdapter artistAdapter;
    private ArtistFragmentPresenter presenter;
    private LinearLayout linearLayout;
    private TextView info;

    public ArtistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_artist, container, false);

        recyclerView=view.findViewById(R.id.artist_recycler_list);
        layoutManager=new LinearLayoutManager(getContext());
        artistAdapter=new ArtistAdapter(ArtistAdapter.NORMAL);
        recyclerView.setAdapter(artistAdapter);
        recyclerView.setLayoutManager(layoutManager);
        linearLayout=view.findViewById(R.id.artist_null_info);
        presenter=new ArtistFragmentPresenter(this);
        info=view.findViewById(R.id.null_info);
        getData();
        return view;
    }

    private void getData(){
//        if (getPermission) {
            presenter.getData();
//        }else {
//            //暂定不写
//            info.setText(getResources().getString(R.string.permission_info));
//            setData(null);
//        }
    }

    @Override
    public void setData(List<Artist> artists) {

        if (artists==null){
            linearLayout.setVisibility(View.VISIBLE);
        }else if (artists.isEmpty()){
            linearLayout.setVisibility(View.VISIBLE);
        }else {
            linearLayout.setVisibility(View.GONE);
            setList(artists);
        }
    }

    private void setList(List<Artist> artists){
        artistAdapter.setArtists(artists);
    }
}

package com.app.legend.overmusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.utils.ImageLoader;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayingAlumFragment extends Fragment {

    private ImageView imageView;
    private Music music;
    public static final String TAG="playing_music";


    public PlayingAlumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_playing_alum, container, false);
        getComponent(view);
        setData();
        return view;
    }

    private void getComponent(View view){
        imageView=view.findViewById(R.id.play_album_book);
    }

    private void setData(){

        Bundle bundle=getArguments();
        this.music= (Music) bundle.getSerializable(TAG);

        Log.d("music---->>",music.getSongName());
        int w=getResources().getDisplayMetrics().widthPixels;
        ImageLoader.getImageLoader(getContext()).setScroll(false);
        ImageLoader.getImageLoader(getContext()).setAlbum(this.music,this.imageView,ImageLoader.BIG,w,w);

    }

}

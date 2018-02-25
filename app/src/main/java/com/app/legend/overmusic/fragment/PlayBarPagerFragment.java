package com.app.legend.overmusic.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.PlayingActivity;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.utils.ImageLoader;

import io.reactivex.Observable;

/**
 * 播放条，在主页面进行播放后，底部所显示的内容
 * A simple {@link Fragment} subclass.
 */
public class PlayBarPagerFragment extends BaseFragment {

    private Music playing_music;

    private ImageView imageView;

    private TextView song,info;

    public static final String TAG="music";
    private static final int FAST_CLICK_DELAY=1000;
    private long lastClickTime= 0L;


    public Music getPlaying_music() {
        return playing_music;
    }

    private void getInfo(){

        Bundle bundle=getArguments();

        Music music= (Music) bundle.getSerializable(TAG);

        this.playing_music=music;
    }

    public PlayBarPagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_play_bar_pager, container, false);
        getInfo();

        imageView=view.findViewById(R.id.play_bar_album);

        song=view.findViewById(R.id.song_name);

        info=view.findViewById(R.id.song_artist);

        song.setText(playing_music.getSongName());
        info.setText(playing_music.getArtistName());

        int w=getResources().getDimensionPixelSize(R.dimen.press_space);

        ImageLoader.getImageLoader(getContext()).setAlbum(playing_music,imageView,ImageLoader.SMALL,w,w);

        view.setOnClickListener(v-> {
            //打开播放页面

            if (System.currentTimeMillis()-lastClickTime<=FAST_CLICK_DELAY){
                return;
            }

            lastClickTime=System.currentTimeMillis();

            Intent intent=new Intent(getContext(), PlayingActivity.class);
            intent.putExtra("music",playing_music);

            getActivity().startActivity(intent);
        });

        return view;
    }

}

package com.app.legend.overmusic.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ViewGroup;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.fragment.PlayBarFragment;
import com.app.legend.overmusic.fragment.PlayBarPagerFragment;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.PlayStatus;

import java.util.List;

/**
 * 底部播放栏adapter
 * Created by legend on 2018/1/29.
 */

public class PlayingBarAdapter extends FragmentStatePagerAdapter{


    private List<Integer> playPositionList;
    private List<Music> musicList;

    public void setMusicList(List<Music> musicList) {

        this.musicList = musicList;

        notifyDataSetChanged();
    }

    public PlayingBarAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (musicList!=null){

            Music music=musicList.get(position);

            PlayBarPagerFragment fragment=new PlayBarPagerFragment();
            Bundle bundle=new Bundle();

            bundle.putSerializable(PlayBarPagerFragment.TAG,music);

            fragment.setArguments(bundle);

            return fragment;
        }

        return null;
    }

    @Override
    public int getCount() {
        if (musicList!=null){

            return musicList.size();
        }

        return 0;
    }


    @Override
    public int getItemPosition(Object object) {
//        return super.getItemPosition(object);
        return POSITION_NONE;
    }

}

package com.app.legend.overmusic.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.fragment.PlayingAlumFragment;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.PlayStatus;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/16.
 */

public class PlayAlbumAdapter extends FragmentStatePagerAdapter {

    private List<Integer> positionList;

    public void setPositionList(List<Integer> positionList) {

//        PlayStatus status=PlayHelper.create().getStatus();
//        if (status.equals(PlayStatus.RANDOM)) {
//
//            this.positionList = positionList;
//        }else if (this.positionList==null){
//
//            this.positionList=positionList;
//        }else if (!this.positionList.equals(positionList)){
//
//            this.positionList=positionList;
//        }
        this.positionList=positionList;

        notifyDataSetChanged();
    }

    public PlayAlbumAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (positionList!=null){
            Music music= PlayHelper.create().getPagerMusic(position);
            PlayingAlumFragment fragment=new PlayingAlumFragment();
            Bundle bundle=new Bundle();
            bundle.putSerializable(PlayingAlumFragment.TAG,music);

            fragment.setArguments(bundle);

            return fragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        if (positionList!=null){
            return positionList.size();
        }

        return 0;
    }

//    @Override
//    public boolean isViewFromObject(View view, Object object) {
//        return (view==object);
//    }
//
//    @Override
//    public int getItemPosition(Object object) {
//        return POSITION_NONE;
//    }
}

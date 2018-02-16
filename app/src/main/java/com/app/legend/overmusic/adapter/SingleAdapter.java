package com.app.legend.overmusic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

/**
 * 负责主页面的四项Fragment滑动切换
 * Created by legend on 2018/1/28.
 */

public class SingleAdapter extends FragmentStatePagerAdapter{

    List<Fragment> fragmentList;


    public SingleAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (fragmentList!=null) {
            Fragment fragment=fragmentList.get(position);

            return fragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        if (fragmentList!=null){
            return fragmentList.size();
        }

        return 0;
    }

    public void setFragmentList(List<Fragment> fragmentList){

        this.fragmentList=fragmentList;

        notifyDataSetChanged();

//        Log.d("tag-->>",fragmentList+"");
    }


}

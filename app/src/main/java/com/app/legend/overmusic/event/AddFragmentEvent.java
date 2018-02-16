package com.app.legend.overmusic.event;

import android.support.v4.app.Fragment;

/**
 *
 * Created by legend on 2018/2/7.
 */

public class AddFragmentEvent {

    private Fragment fragment;

    public AddFragmentEvent(Fragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}

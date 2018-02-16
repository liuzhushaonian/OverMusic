package com.app.legend.overmusic.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 *
 * Created by legend on 2018/2/5.
 */

public class MusicViewPager extends ViewPager {

    private boolean isScroll=true;

    public MusicViewPager(Context context) {
        super(context);
    }

    public MusicViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isScroll&&super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isScroll&&super.onInterceptTouchEvent(ev);
    }
}

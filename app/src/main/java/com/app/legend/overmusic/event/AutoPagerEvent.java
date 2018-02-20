package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/17.
 */

public class AutoPagerEvent {

    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public AutoPagerEvent(int position) {

        this.position = position;
    }
}

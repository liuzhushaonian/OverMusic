package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/21.
 */

public class PagerNotifyChangeEvent {

    private int info;

    public int getInfo() {
        return info;
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public PagerNotifyChangeEvent(int info) {

        this.info = info;
    }
}

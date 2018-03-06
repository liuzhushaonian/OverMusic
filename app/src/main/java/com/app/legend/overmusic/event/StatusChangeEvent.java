package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/3/3.
 */

public class StatusChangeEvent {

    private boolean scroll=false;

    public boolean isScroll() {
        return scroll;
    }

    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    public StatusChangeEvent(boolean scroll) {

        this.scroll = scroll;
    }
}

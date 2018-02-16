package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/5.
 */

public class PlayListChangeEvent {

    private boolean isScroll=true;

    public PlayListChangeEvent(boolean isScroll) {
        this.isScroll = isScroll;
    }

    public boolean isScroll() {
        return isScroll;
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }
}

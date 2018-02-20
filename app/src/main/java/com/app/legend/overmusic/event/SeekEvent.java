package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/17.
 */

public class SeekEvent {

    private int position;

    public int getPosition() {

        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public SeekEvent(int position) {

        this.position = position;
    }
}

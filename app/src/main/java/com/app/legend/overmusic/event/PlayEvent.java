package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/5.
 */

public class PlayEvent {
    private int status=-1;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public PlayEvent(int status) {

        this.status = status;
    }
}

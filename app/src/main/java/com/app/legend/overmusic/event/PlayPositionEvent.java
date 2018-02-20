package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/17.
 */

public class PlayPositionEvent {
    private int position;
    private long progress;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public PlayPositionEvent(int position, long progress) {

        this.position = position;
        this.progress = progress;
    }
}

package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/3/7.
 */

public class SetLrcProgressEvent {

    long progress;

    public SetLrcProgressEvent(long progress) {
        this.progress = progress;
    }

    public long getProgress() {

        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }
}

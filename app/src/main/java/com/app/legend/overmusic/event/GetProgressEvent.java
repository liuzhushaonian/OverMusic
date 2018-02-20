package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/17.
 */

public class GetProgressEvent {
    private int pregress;

    public int getPregress() {
        return pregress;
    }

    public void setPregress(int pregress) {
        this.pregress = pregress;
    }

    public GetProgressEvent(int pregress) {

        this.pregress = pregress;
    }
}

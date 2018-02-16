package com.app.legend.overmusic.event;

/**
 *
 * Created by legend on 2018/2/15.
 */

public class QueryEvent {
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public QueryEvent(String data) {

        this.data = data;
    }
}

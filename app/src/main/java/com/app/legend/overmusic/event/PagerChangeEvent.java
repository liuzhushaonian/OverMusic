package com.app.legend.overmusic.event;

import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/2.
 */

public class PagerChangeEvent {
    private int position;
    private List<Integer> integerList;

    public int getPosition() {
        return position;
    }

    public PagerChangeEvent(int position, List<Integer> integerList) {
        this.position = position;
        this.integerList = integerList;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<Integer> getIntegerList() {
        return integerList;
    }

    public void setIntegerList(List<Integer> integerList) {
        this.integerList = integerList;
    }
}

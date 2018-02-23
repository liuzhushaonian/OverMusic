package com.app.legend.overmusic.interfaces;

import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/3.
 */

public interface IPlayBarPresenter {

    void setCurrentPager(int position);

    void setDataList(List<Integer> integerList);

    void setScroll(boolean scroll);

    void setStatus(int status);

    void setProgress(int progress);

    void changePager();

}

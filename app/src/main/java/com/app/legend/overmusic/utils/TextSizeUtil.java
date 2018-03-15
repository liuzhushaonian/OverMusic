package com.app.legend.overmusic.utils;

/**
 *
 * Created by legend on 2018/3/15.
 */

public class TextSizeUtil {


    public static int px2sp(float value){

        final float fontScale = OverApplication.getContext().getResources().getDisplayMetrics().scaledDensity;

        return (int) (value/fontScale+0.5f);
    }
}

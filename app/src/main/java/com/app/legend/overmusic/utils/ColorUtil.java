package com.app.legend.overmusic.utils;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import com.app.legend.overmusic.R;

/**
 *
 * Created by legend on 2018/2/11.
 */

public class ColorUtil {


    public static int getColor(Bitmap bitmap,int defaultColor){

        Palette palette=Palette.from(bitmap).generate();

        int color=palette.getLightVibrantColor(defaultColor);

        return color;

    }
}

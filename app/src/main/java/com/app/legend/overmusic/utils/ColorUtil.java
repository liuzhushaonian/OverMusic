package com.app.legend.overmusic.utils;

import android.content.Context;
import android.content.res.Resources;
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

    public static int getAlbumColor(int albumId){

        int count=albumId%12;

        int color=0;

        Resources resources=OverApplication.getContext().getResources();

        switch (count){
            case 0:

                color=resources.getColor(R.color.colorBlueGrey);
                break;
            case 1:
                color=resources.getColor(R.color.colorBlue);
                break;
            case 2:
                color=resources.getColor(R.color.colorPurple);
                break;
            case 3:
                color=resources.getColor(R.color.colorCyan);
                break;
            case 4:
                color=resources.getColor(R.color.colorTeal);
                break;
            case 5:
                color=resources.getColor(R.color.colorOrange);
                break;
            case 6:
                color=resources.getColor(R.color.colorPink);
                break;
            case 7:
                color=resources.getColor(R.color.colorRed);
                break;
            case 8:
                color=resources.getColor(R.color.colorGrey);
                break;
            case 9:
                color=resources.getColor(R.color.colorLime);
                break;
            case 10:
                color=resources.getColor(R.color.colorDeepOrange);
                break;
            case 11:
                color=resources.getColor(R.color.colorLightBlue);
                break;
        }

        return color;
    }


}

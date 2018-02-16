package com.app.legend.overmusic.utils;

import android.app.Application;
import android.content.Context;

/**
 *
 * Created by legend on 2018/1/25.
 */

public class OverApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }


}

package com.app.legend.overmusic.utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 联网工具，获取歌词以及封面
 * Created by legend on 2018/3/6.
 */

public class InternetUtil {

    private static final String API="https://api.imjad.cn/cloudmusic/?";
    private static final String SEARCH="search";
    private static final String LRC="lyric";
    private static final String SEARCH_TYPE="search_type";
    private static final String S="s";
    private static final String UNDO="https://p1.music.126.net/2UGD8Wf8WSmulwu2EwrqeQ==/3391993372466843.jpg";

    private static volatile InternetUtil internetUtil;

    public static InternetUtil getUtil(){

        if (internetUtil==null){
            synchronized (InternetUtil.class){
                internetUtil=new InternetUtil();
            }
        }

        return internetUtil;
    }


//    public int getSongId(String song){
//
//    }


    public String getJson(String doing){
        String url=API+doing;
        Log.d("url---->>",url);
        Request.Builder builder=new Request.Builder().url(url).method("GET",null);

        Request request=builder.build();

        File sdcard= OverApplication.getContext().getExternalCacheDir();

        int cacheSize=100*1024*1024;

        OkHttpClient.Builder builder1= null;
        builder1 = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20,TimeUnit.SECONDS);

        if (sdcard!=null){
            builder1.cache(new Cache(sdcard.getAbsoluteFile(),cacheSize));
        }

        OkHttpClient okHttpClient=builder1.build();

        Call call=okHttpClient.newCall(request);

        Response response= null;
        String json="";

        try {
            response = call.execute();
            json=response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }

}

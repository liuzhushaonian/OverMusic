package com.app.legend.overmusic.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析json数据
 * Created by legend on 2018/3/6.
 */

public class JsonUtil {

    private static volatile JsonUtil jsonUtil;

    public static JsonUtil getJsonUtil(){
        if (jsonUtil==null){
            synchronized (JsonUtil.class){
                jsonUtil=new JsonUtil();
            }
        }

        return jsonUtil;
    }

    /**
     * 解析json获取歌曲id
     * @param json 传入json
     * @return 返回id
     */
    public long getSongId(String json){
        long id=-1;

        try {
            JSONObject jsonObject=new JSONObject(json);

            JSONObject jsonObject1=jsonObject.getJSONObject("result");

            JSONArray jsonArray=jsonObject1.getJSONArray("songs");


            JSONObject object= (JSONObject) jsonArray.get(0);

            id=object.getLong("id");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }


    public String getAlbumPic(String json){

        String pic="";

        JSONObject jsonObject= null;

        try {
            jsonObject = new JSONObject(json);
            JSONObject jsonObject1=jsonObject.getJSONObject("result");

            int code=jsonObject.getInt("code");

            if (code!=200){
                return pic;
            }

            JSONArray jsonArray=jsonObject1.getJSONArray("songs");

            JSONObject object= (JSONObject) jsonArray.get(0);

            JSONObject object1=object.getJSONObject("al");

            pic=object1.getString("picUrl");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pic;
    }


    /**
     * 获取歌词
     * 如果有翻译歌词，还需获取翻译后的歌词
     * @param json
     * @return
     */
    public List<String> getLrcList(String json){

        List<String> lrcList=new ArrayList<>();
        try {
            JSONObject result=new JSONObject(json);

            JSONObject lrcObj=result.getJSONObject("lrc");

            String lrc=lrcObj.getString("lyric");

            if (lrc!=null) {
                lrcList.add(lrc);
            }


            JSONObject tlrcObj=result.getJSONObject("tlyric");

            if (tlrcObj.has("lyric")) {
                String tlrc = tlrcObj.getString("lyric");


                if (!tlrc.equals("null")) {

                    lrcList.add(tlrc);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();

            return lrcList;
        }

        return lrcList;

    }

    /**
     * 获取翻译版歌词
     * @param json
     * @return
     */
    public String getTLrc(String json){

        JSONObject result= null;
        String tlrc ="";
        try {
            result = new JSONObject(json);


            JSONObject tlrcObj=result.getJSONObject("tlyric");

            if (tlrcObj.has("lyric")) {
                tlrc = tlrcObj.getString("lyric");

                if (!tlrc.equals("null")) {
                    return tlrc;
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();

            return tlrc;
        }


        return tlrc;
    }




}

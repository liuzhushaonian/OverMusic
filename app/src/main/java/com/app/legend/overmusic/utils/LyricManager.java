package com.app.legend.overmusic.utils;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.app.legend.overmusic.bean.Lrc;
import com.app.legend.overmusic.bean.Music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * 歌词管理器，负责管理，解析歌词
 * Created by legend on 2018/3/6.
 */

public class LyricManager {

    private static final String LRC_PATH = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/OverMusic/lrc/";//歌词存放路径

    private static volatile LyricManager manager;

    public static LyricManager getManager() {

        if (manager == null) {
            synchronized (LyricManager.class) {
                manager = new LyricManager();
                File file = new File(LRC_PATH);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }
        return manager;
    }

    /**
     * 保存歌词
     */
    private void saveLrc(String lrcName, String lrc) {
        String name = LRC_PATH + lrcName + ".lrc";

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(name);

            output.write(lrc.getBytes());
            //将String字符串以字节流的形式写入到输出流中
            output.flush();
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取本地歌词
     *
     * @param music
     * @return
     */
    public String getLrc(Music music) {
        String lrc = "";
        List<String> strings = getLocalLrc(music);

        if (!strings.isEmpty()) {
            lrc = strings.get(0);
        }

        if (!lrc.isEmpty()) {//所得不是空的

            return lrc;

//            Log.d("lrc-local--->>>",lrc);
        } else {//所得是空的，向网络获取歌词

            strings = getNetLrc(music);
            lrc = strings.get(0);

        }

        return lrc;

    }


    /**
     * 获取本地歌词，歌词或有两份，故获取数组
     *
     * @param music
     * @return 返回一个数组，或有歌词，或无歌词
     */

    private List<String> getLocalLrc(Music music) {

        List<String> list = new ArrayList<>();


        InputStream inputStream=null;

        String lrc_path=LRC_PATH+music.getSongName()+".lrc";

        try {
            File file=new File(lrc_path);

            inputStream=new FileInputStream(file);

            byte[] temp=new byte[inputStream.available()];

            inputStream.read(temp);

            inputStream.close();

            String lrc=new String(temp);

            list.add(lrc);

//            Log.d("local---->>>",lrc);

        } catch (Exception e1) {
            list.add("");
        }


        return list;

    }


    public String getTlrc(String name){

        String lrc="";

        InputStream inputStream=null;

        String lrc_path=LRC_PATH+name+".lrc";

        try {
            File file=new File(lrc_path);

            inputStream=new FileInputStream(file);

            byte[] temp=new byte[inputStream.available()];

            inputStream.read(temp);

            inputStream.close();

            lrc=new String(temp);

//            Log.d("local---->>>",lrc);

        } catch (Exception e) {
            e.printStackTrace();

        }


        return lrc;

    }

    /**
     * @param music
     * @return
     */
    private List<String> getNetLrc(Music music) {

        List<String> stringList = new ArrayList<>();


        String doing = "type=search&s=" + music.getSongName();

        String json = InternetUtil.getUtil().getJson(doing);

        long id = JsonUtil.getJsonUtil().getSongId(json);

        String getLrc = "type=lyric&id=" + id;

        String j = InternetUtil.getUtil().getJson(getLrc);//获取歌词json

        List<String> strings = JsonUtil.getJsonUtil().getLrcList(j);//解析json获取歌词

//        StringBuilder stringBuilder = new StringBuilder("");
        String lrc = "";

        //顺便保存歌词
        if (!strings.isEmpty()) {

            for (int i = 0; i < strings.size(); i++) {

                if (i==0){
                    lrc=strings.get(i);
                    saveLrc(music.getSongName(),lrc);
                }else if (i==1){//翻译歌词
//                    lrc = "double-" + lrc;
                    saveLrc("t"+music.getSongName(),strings.get(i));
                }

            }

//            if (strings.size() >= 2) {
//                //有多种语言的歌词
//                lrc = "double-" + lrc;
//            }

//            saveLrc(music, lrc);//保存歌词

        }

        stringList.add(lrc);

        return stringList;
    }


    /**
     * 解析歌词
     *
     * @param lrc
     */
    public List<Lrc> parseLrc(String lrc) {
        String[] strings = lrc.split("\\n");

        List<Lrc> lrcList = new ArrayList<>();

        for (int i = 0; i < strings.length; i++) {
            List<Lrc> list = parseLine(strings[i]);
            if (list != null) {
                lrcList.addAll(list);
            }
        }


        Collections.sort(lrcList, new TimeComparator());

//        for (int o=0;o<lrcList.size();o++){
//            Log.d("lrc--->>>",lrcList.get(o).getTime()+"");
//        }
        return lrcList;


    }


    private List<Lrc> parseLine(String line) {

        List<Lrc> lrcList = new ArrayList<>();

//        Log.d("line---->>",line);

        if (line.startsWith("[al")) {
//            lrc.setContent(line.substring(4,line.length()-1));
//            lrc.setTime(0);
//            lrcList.add(lrc);
            return lrcList;
        } else if (line.startsWith("[ar")) {
//            lrc.setTime(0);
//            lrc.setContent();
            return lrcList;
        } else if (line.startsWith("[au")) {
            return lrcList;
        } else if (line.startsWith("[by")) {
            return lrcList;
        } else if (line.startsWith("[offset")) {
            return lrcList;
        } else if (line.startsWith("[re")) {
            return lrcList;
        } else if (line.startsWith("[ti")) {
            return lrcList;
        } else if (line.startsWith("[ve")) {
            return lrcList;
        } else {

            // 设置正则规则
            String reg = "\\[(\\d{1,2}:\\d{1,2}\\.\\d{1,2})\\]|\\[(\\d{1,2}:\\d{1,2}\\.\\d{1,3})\\]|\\[(\\d{1,2}:\\d{1,2})\\]";
            // 编译
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(line);

            if (!matcher.find()) {
                return null;
            }


            //解析开始
            String[] strings = line.split("]");

//            Log.d("size---->>",strings.length+"");

            for (int i = 0; i < strings.length; i++) {

                String content = strings[strings.length - 1];//获取最后一项
                String c = "";

                /**
                 * 歌词的形式
                 * [00:01.20][00:03.45][04:20.33]你一直在我心中
                 * 以"]"为分割，可获得    [00:01.20、[00:03.45、[04:20.33、你一直在我心中    等字符串数组
                 * 然后以"["为开头的则是时间，没有则是歌词本身
                 * 最后只需去掉"["再转为long即可
                 */
                if (!content.startsWith("[")) {
                    //表示这是个歌词
                    c = content;
                }


                String s = strings[i];
                if (s.startsWith("[")) {
                    //时间部分
                    s = s.substring(1, s.length());
//                    Log.d("time--->>",s);

                    Lrc lrc = new Lrc();
                    lrc.setTime(timeConvert(s));
                    lrc.setContent(c);

                    lrcList.add(lrc);
                }

            }

        }
        return lrcList;

    }


    private static long timeConvert(String timeString) {
        //因为给如的字符串的时间格式为XX:XX.XX,返回的long要求是以毫秒为单位
        //将字符串 XX:XX.XX 转换为 XX:XX:XX
//        timeString=timeString.replace("[","");
//        timeString=timeString.replace("]","");
        timeString = timeString.replace('.', ':');
        //将字符串 XX:XX:XX 拆分
        String[] times = timeString.split(":");

        Log.d("time---->>>",timeString);

        int t=0;

        for (int i=0;i<times.length;i++){
            if (i==0){
                t=t+Integer.valueOf(times[i])*60*1000;
            }

            if (i==1){

                t=t+Integer.valueOf(times[i])*1000;
            }

            if (i==2){
                t=t+Integer.valueOf(times[i]);
            }

        }

        return t;


//        // mm:ss:SS
//        return Integer.valueOf(times[0]) * 60 * 1000 +//分
//                Integer.valueOf(times[1]) * 1000 +//秒
//                Integer.valueOf(times[2]);//毫秒
    }

    private static class TimeComparator implements Comparator {


        @Override
        public int compare(Object o1, Object o2) {
            Lrc l1 = (Lrc) o1;
            Lrc l2 = (Lrc) o2;
            return Long.valueOf(l1.getTime()).compareTo(l2.getTime());
        }
    }


    public void fixLrc(String songName){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {

                    String lrc_path=LRC_PATH+songName+".lrc";

                    File file=new File(lrc_path);

                    if (file.exists()) {
                        file.delete();
                    }

                    saveLrc(songName,"null");

                    e.onNext(100);

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer==100){
                        Toast.makeText(OverApplication.getContext(),"更新成功~",Toast.LENGTH_SHORT).show();
                    }
                });

    }


    /**
     * 通过搜索获取歌词
     * @param songName
     * @return
     */
    public String getLrcFromNetByUser(String songName){


        String doing = "type=search&s=" + songName;

        String json = InternetUtil.getUtil().getJson(doing);

        long id = JsonUtil.getJsonUtil().getSongId(json);

        String getLrc = "type=lyric&id=" + id;

        String j = InternetUtil.getUtil().getJson(getLrc);//获取歌词json

        List<String> strings = JsonUtil.getJsonUtil().getLrcList(j);//解析json获取歌词

        String lrc=strings.get(0);

        if (strings.size()>=2){//判断是否存在翻译歌词

            lrc="double-"+lrc;

        }

        return lrc;

    }

    public String getTLrcByUser(String name){

        String doing = "type=search&s=" + name;

        String json = InternetUtil.getUtil().getJson(doing);

        long id = JsonUtil.getJsonUtil().getSongId(json);

        String getLrc = "type=lyric&id=" + id;

        String j = InternetUtil.getUtil().getJson(getLrc);//获取歌词json


        String tlrc=JsonUtil.getJsonUtil().getTLrc(j);

        return tlrc;

    }

    public void saveLrcByUser(String songName,String lrc,int type){



        switch (type){
            case 2://翻译歌词
                songName="t"+songName;
                break;
        }

        String name = LRC_PATH + songName + ".lrc";

        FileOutputStream output = null;
        try {

            output = new FileOutputStream(name);

            output.write(lrc.getBytes());
            //将String字符串以字节流的形式写入到输出流中
            output.flush();
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

package com.app.legend.overmusic.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Lrc;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IPlayingAlbumFragmentPresenter;
import com.app.legend.overmusic.utils.Database;
import com.app.legend.overmusic.utils.LyricManager;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.TextSizeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 *
 * Created by legend on 2018/3/15.
 */

public class PlayingAlbumFragmentPresenter {

    private IPlayingAlbumFragmentPresenter fragment;

    public PlayingAlbumFragmentPresenter(IPlayingAlbumFragmentPresenter fragment) {
        this.fragment = fragment;
    }


    public void showDialog(Activity activity,Music music){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        String[] items = new String[]{"歌词牛头不对马嘴", "这首歌是纯音乐啊"};
        builder.setTitle("咦，歌词出错了吗？").setItems(items, (dialog, which) -> {

            String item = items[which];

            switch (item) {
                case "歌词牛头不对马嘴"://让用户输入歌名搜索歌词

                    showEditView(activity,music);

                    break;

                case "这首歌是纯音乐啊"://将本地歌词内容改为null，表示此后都不再显示这首歌的歌词
                    fixLrc(music.getSongName());
                    break;

            }
        });
        builder.create().show();

    }

    /**
     * 搜索歌名获取歌词
     * @param activity
     * @param music
     */
    private void showEditView(Activity activity,Music music){

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final EditText editText= new EditText(activity);
        editText.setText(music.getSongName());

        builder.setPositiveButton("搜索歌词", (dialog, which) -> {
            String list=editText.getText().toString();
            if (list.isEmpty()){
                Toast.makeText(activity,"不能为空",Toast.LENGTH_SHORT).show();
            }else {
                searchLrc(activity,music,list);
            }
        });

        builder.setView(editText).setTitle("请输入正确的歌名以搜索歌词喔~");
        builder.show();


    }

    //搜索
    private void searchLrc(Activity activity,Music music,String s){

        Observable
                .create((ObservableOnSubscribe<String>) e -> {

                    String lrc=LyricManager.getManager().getLrcFromNetByUser(s);

                    if (lrc!=null){
                        e.onNext(lrc);
                    }

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s1 -> {
                    showSearchLrc(activity,s1,music);
                });


    }

    //展示搜索出来的歌词
    private void showSearchLrc(Activity activity,String lrc,Music music){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater=LayoutInflater.from(activity);

        View view=inflater.inflate(R.layout.about_layout,null,false);

        TextView textView=view.findViewById(R.id.about_text);

        String s=lrc;

        if (lrc.startsWith("double-")){
            s=lrc.substring(7,lrc.length());
        }

        textView.setText(s);

        builder.setNegativeButton("保存",(dialog, which) -> {
            //保存
            if (lrc.startsWith("double-")) {
                saveSearchLrc(music, lrc, 1);
            }else {
                saveSearchLrc(music,lrc,0);
            }

        });

        builder.setNeutralButton("取消",(dialog, which) -> {
            //不保存
            Toast.makeText(activity,"歌词未改动",Toast.LENGTH_SHORT).show();
        });

        builder.setView(view).setTitle("歌词展示").show();

    }

    //保存歌词
    private void saveSearchLrc(Music music,String lrc,int type){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {

                    LyricManager.getManager().saveLrcByUser(music.getSongName(),lrc,type);
                    e.onNext(200);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer==200){
                        Toast.makeText(OverApplication.getContext(),"保存成功！",Toast.LENGTH_SHORT).show();
                        getLrc(music);
                    }
                });

    }


    public void getLrc(Music music){

        Observable
                .create((ObservableOnSubscribe<List<Lrc>>) e -> {
                    String lrc = LyricManager.getManager().getLrc(music);

                    List<Lrc> lrcList = new ArrayList<>();

                    if (lrc.startsWith("double-")) {//双份歌词.

                        lrc = lrc.substring(6, lrc.length());

                        lrcList = LyricManager.getManager().parseLrc(lrc);

                        setTlrc(music);//设置翻译歌词
                    } else if (lrc.startsWith("null")) {//无歌词

                        lrcList=new ArrayList<>();

                    } else {//正常歌词
                        lrcList = LyricManager.getManager().parseLrc(lrc);

                    }

                    if (lrcList != null) {

                        e.onNext(lrcList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lrcs -> {
                    if(notNull()) {
                        fragment.setLrcList(lrcs);
                    }
                });

    }


    private void setTlrc(Music music) {

        Observable
                .create((ObservableOnSubscribe<List<Lrc>>) e -> {
                    String lrc = LyricManager.getManager().getTlrc("t" + music.getSongName());

                    List<Lrc> lrcList = LyricManager.getManager().parseLrc(lrc);

                    e.onNext(lrcList);

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lrcs -> {
                    if (notNull()) {
                        fragment.setTLrcList(lrcs);
                    }
                });


    }



    private void fixLrc(String songName){
        LyricManager.getManager().fixLrc(songName);
        if (notNull()) {
            fragment.setLrcList(null);
        }
    }

    private boolean notNull(){
        return fragment!=null;
    }


    public void getTLrc(Activity activity,Music music){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater=LayoutInflater.from(activity);

        View view=inflater.inflate(R.layout.about_layout,null,false);

        TextView textView=view.findViewById(R.id.about_text);

        String waning=activity.getResources().getString(R.string.waning);

        textView.setText(waning);

        builder.setNegativeButton("确定",(dialog, which) -> {

            searchTLrc(activity,music);
        });

        builder.setView(view).setTitle("警告").show();

    }

    private void searchTLrc(Activity activity,Music music){
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    String lrc=LyricManager.getManager().getTLrcByUser(music.getSongName());

                    if (lrc!=null){
                        e.onNext(lrc);
                    }

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (!s.equals("")){
                        showTLrc(activity,music,s);
                    }else {
                        Toast.makeText(activity,"没找到任何翻译歌词",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showTLrc(Activity activity,Music music,String lrc){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater=LayoutInflater.from(activity);

        View view=inflater.inflate(R.layout.about_layout,null,false);

        TextView textView=view.findViewById(R.id.about_text);

        textView.setText(lrc);

        builder.setNegativeButton("保存",(dialog, which) -> {
            saveSearchLrc(music,lrc,2);
        });

        builder.setNeutralButton("取消",(dialog, which) -> {
            //不保存
            Toast.makeText(activity,"歌词未改动",Toast.LENGTH_SHORT).show();
        });

        builder.setView(view).setTitle("歌词展示").show();

    }


    public void saveTextSize(Activity activity,float size){

        SharedPreferences sharedPreferences=activity.getSharedPreferences("LrcInfo", MODE_PRIVATE);

        sharedPreferences.edit().putFloat("size",size).apply();

    }

    public float getTextSize(Activity activity){
        SharedPreferences sharedPreferences=activity.getSharedPreferences("LrcInfo", MODE_PRIVATE);

        float size= TextSizeUtil.px2sp(activity.getResources().getDimension(R.dimen.lrc_text_size));

        return sharedPreferences.getFloat("size",size);


    }

    public int getLrcColor(Activity activity){

        SharedPreferences sharedPreferences=activity.getSharedPreferences("over_music_shared",MODE_PRIVATE);

        int defaultValue=activity.getResources().getColor(R.color.colorBlueGrey);

        return sharedPreferences.getInt("color",defaultValue);

    }
}

package com.app.legend.overmusic.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.interfaces.IMainPresenter;
import com.app.legend.overmusic.utils.Database;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/1/28.
 */

public class MainPresenter {
    private IMainPresenter mainActivity;
    private static final String IMAGE_PATH = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/OverMusic/image_bg";

    public MainPresenter(IMainPresenter mainActivity) {
        this.mainActivity = mainActivity;
    }

    /**
     * 真添加音乐的地方
     * @param activity
     * @param music
     */
    public void setPopupMenu(Activity activity, Music music){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        Observable
                .create((ObservableOnSubscribe<String[]>) e -> {
                    String[] items=getItem();
                    e.onNext(items);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> {
                    builder.setItems(strings,(dialog, which) -> {

                        String item=strings[which];
                        switch (item){
                            case "添加新列表":
                                editPopupMenu(activity,music);
                                break;
                            default:
                                addMusicToList(strings[which],music);
                                break;
                        }
                    });

                    builder.create().show();
                });

    }

    private void editPopupMenu(Activity activity,Music music){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        final EditText editText= new EditText(activity);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String list=editText.getText().toString();
            if (list.isEmpty()){
                Toast.makeText(activity,"列表名称不能为空",Toast.LENGTH_SHORT).show();
            }else {

                int i=Database.getDefault().addList(list);
                if (i==1) {
//                    Database.getDefault().addMusicToList(list,music);
//                    mainActivity.changePlayListData();
//                    Toast.makeText(activity, "添加音乐成功~", Toast.LENGTH_SHORT).show();
                    addMusicToList(list,music);
                }else {
                    Toast.makeText(activity, "已存在相同列表，无法新建", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setView(editText).setTitle("新建列表名称");
        builder.show();

    }

    //添加到列表
    private void addMusicToList(String name,Music music){
        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Database.getDefault().addMusicToList(name,music);

                    e.onNext(1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    mainActivity.changePlayListData();
                    Toast.makeText(OverApplication.getContext(),"添加成功",Toast.LENGTH_SHORT).show();
                });

    }

    //添加album音乐到列表
    private void addAlbumMusic(String name, Album album){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    List<Music> musicList= Mp3Util.newInstance().getAlbumMusic(album);
                    for (Music music:musicList){
                        Database.getDefault().addMusicToList(name,music);
                    }

                    e.onNext(1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    mainActivity.changePlayListData();
                    Toast.makeText(OverApplication.getContext(),"添加成功",Toast.LENGTH_SHORT).show();
                });

    }

    //添加歌手的音乐到列表
    private void addArtistMusic(String name,Artist artist){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    List<Music> musicList= Mp3Util.newInstance().getArtistMusic(artist);
                    for (Music music:musicList){
                        Database.getDefault().addMusicToList(name,music);
                    }

                    e.onNext(1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    mainActivity.changePlayListData();
                    Toast.makeText(OverApplication.getContext(),"添加成功",Toast.LENGTH_SHORT).show();
                });

    }

    private String[] getItem(){
        List<PlayList> playLists= Database.getDefault().getAllPlayList();
        String[] items=new String[playLists.size()+1];
        for (int i=0;i<playLists.size();i++){
            items[i]=playLists.get(i).getName();
        }
//                    Log.d("length--->>",items.length+"");

        items[items.length-1]="添加新列表";

        return items;
    }

    public void setPopupMenu(Activity activity,Album album){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        Observable
                .create((ObservableOnSubscribe<String[]>) e -> {
                    String[] items=getItem();
                    e.onNext(items);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> {
                    builder.setItems(strings,(dialog, which) -> {

                        String item=strings[which];
                        switch (item){
                            case "添加新列表":
                                editPopupMenu(activity,album);
                                break;
                            default:
                                addAlbumMusic(item,album);
                                break;
                        }
                    });

                    builder.create().show();
                });

    }

    private void editPopupMenu(Activity activity,Album album){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        final EditText editText= new EditText(activity);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String list=editText.getText().toString();
            if (list.isEmpty()){
                Toast.makeText(activity,"列表名称不能为空",Toast.LENGTH_SHORT).show();
            }else {

                int i=Database.getDefault().addList(list);
                if (i==1) {
                    addAlbumMusic(list,album);
                }else {
                    Toast.makeText(activity, "已存在相同列表，无法新建", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setView(editText).setTitle("新建列表名称");
        builder.show();

    }

    public void setPopupMenu(Activity activity, Artist artist){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        Observable
                .create((ObservableOnSubscribe<String[]>) e -> {
                    String[] items=getItem();
                    e.onNext(items);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> {
                    builder.setItems(strings,(dialog, which) -> {

                        String item=strings[which];
                        switch (item){
                            case "添加新列表":
                                editPopupMenu(activity,artist);
                                break;
                            default:
                                addArtistMusic(item,artist);
                                break;
                        }
                    });

                    builder.create().show();
                });

    }

    private void editPopupMenu(Activity activity,Artist artist){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        final EditText editText= new EditText(activity);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String list=editText.getText().toString();
            if (list.isEmpty()){
                Toast.makeText(activity,"列表名称不能为空",Toast.LENGTH_SHORT).show();
            }else {

                int i=Database.getDefault().addList(list);
                if (i==1) {
                    addArtistMusic(list,artist);
                }else {
                    Toast.makeText(activity, "已存在相同列表，无法新建", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setView(editText).setTitle("新建列表名称");
        builder.show();

    }

    public void playAllMusic(){

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {
                    List<Music> musicList=Mp3Util.newInstance().getAllMusic(OverApplication.getContext());
                    e.onNext(musicList);

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(music -> {
                    if (music.isEmpty()){
                        return;
                    }

//                    PlayHelper.create().playAndUpdate(music.get(0),music,0);

                    PlayHelper.create().playMusicAndUpdateList(music,0);
                });
    }

    public void newList(Activity activity){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        final EditText editText= new EditText(activity);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String list=editText.getText().toString();
            if (list.isEmpty()){
                Toast.makeText(activity,"列表名称不能为空",Toast.LENGTH_SHORT).show();
            }else {
                addNewList(list);
            }
        });

        builder.setView(editText).setTitle("新建列表名称");
        builder.show();

    }

    private void addNewList(String name){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    int i=Database.getDefault().addList(name);
                    e.onNext(i);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer==1) {
//                    addArtistMusic(list,artist);
                        mainActivity.changePlayListData();
                        Toast.makeText(OverApplication.getContext(), "创建成功", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(OverApplication.getContext(), "已存在相同列表，无法新建", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void scanMusic(){
        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Mp3Util.newInstance().scanMusic();
                    e.onNext(1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Toast.makeText(OverApplication.getContext(),"扫描完成！",Toast.LENGTH_SHORT).show();
                    mainActivity.initPager();
                });
    }

    public void cleanCache(){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    ImageLoader.getImageLoader(OverApplication.getContext()).clean();
                    e.onNext(2);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Toast.makeText(OverApplication.getContext(),"清理成功",Toast.LENGTH_SHORT).show();
                });
    }

    public void aboutDeveloper(Activity activity){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        String info=activity.getResources().getString(R.string.string_about_me);
        String contain=activity.getResources().getString(R.string.string_about_me_contain);
        LayoutInflater inflater=LayoutInflater.from(activity);

        View view=inflater.inflate(R.layout.about_layout,null,false);
        TextView textView=view.findViewById(R.id.about_text);

        textView.setText(contain);

        builder.setMessage(info).setView(view).create().show();

    }

    public void aboutApp(Activity activity){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        String info=activity.getResources().getString(R.string.string_about_app_info);
        String contain=activity.getResources().getString(R.string.string_about_app_contain);
        LayoutInflater inflater=LayoutInflater.from(activity);

        View view=inflater.inflate(R.layout.about_layout,null,false);
        TextView textView=view.findViewById(R.id.about_text);

        textView.setText(contain);

        builder.setMessage(info).setView(view).create().show();
    }


    public void saveAndSetImage(Uri uri, ImageView imageView){

        Observable
                .create((ObservableOnSubscribe<Bitmap>) e -> {
                    Bitmap bitmap=getBitmap(uri);

                    if (bitmap!=null){
                        saveBitmap(bitmap);
                        e.onNext(bitmap);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imageView::setImageBitmap);
    }

    private void saveBitmap(Bitmap bitmap){
        FileOutputStream fileOutputStream=null;
        try {
            File file=new File(IMAGE_PATH);
            if (!file.exists()){
                file.mkdirs();
            }
            File file1=new File(file,"bg");
            fileOutputStream=new FileOutputStream(file1);
            bitmap.compress(Bitmap.CompressFormat.WEBP,100,fileOutputStream);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmap(Uri uri){


        Bitmap bitmap=null;

        if (uri==null){
            return null;
        }

        try {
            bitmap= BitmapFactory.decodeStream(OverApplication.getContext().getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }



}

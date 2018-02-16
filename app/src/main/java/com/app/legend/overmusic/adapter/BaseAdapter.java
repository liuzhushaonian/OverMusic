package com.app.legend.overmusic.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.RxBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 基础adapter
 * Created by legend on 2017/8/8.
 */

public class BaseAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>{

    protected int lastPosition=-1;
    protected int currentPosition=-1;
    protected PopupMenu popupMenu;
    protected static final String IMAGE_PATH = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/OverMusic";



    @Override
    public T onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(T holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }


    static abstract class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected void setPopupMenu(View view,int reMenu){
        popupMenu=new PopupMenu(OverApplication.getContext(),view,0);
        MenuInflater menuInflater=popupMenu.getMenuInflater();
        menuInflater.inflate(reMenu,popupMenu.getMenu());

        popupMenu.show();

    }
    protected void addFragment(Fragment fragment){
        RxBus.getDefault().post(new AddFragmentEvent(fragment));
    }

    protected void saveBitmap(int id){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Bitmap bitmap=ImageLoader.getImageLoader(OverApplication.getContext()).getBitmap(id);
                    if (bitmap!=null){
                        saveBitmap(bitmap);
                        e.onNext(1);
                    }else {
                        e.onNext(-1);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer>0){
                        Toast.makeText(OverApplication.getContext(),
                                OverApplication.getContext().getResources().getString(R.string.string_save_success),
                                Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(OverApplication.getContext(),
                                OverApplication.getContext().getResources().getString(R.string.string_save_fail),
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream fileOutputStream = null;

        try {
            File file = new File(IMAGE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            File file1 = new File(file, SystemClock.currentThreadTimeMillis()+".jpg");

            fileOutputStream = new FileOutputStream(file1);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

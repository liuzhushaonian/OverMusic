package com.app.legend.overmusic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.SettingRingToneEvent;
import com.app.legend.overmusic.service.PlayService;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.RxBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class BaseActivity extends AppCompatActivity {

    private static final int STORGE=0x000100;
    private static final int SETTINGS=0x000200;
    private Disposable ring_dis;
    private Music music;
    protected final String COLOR="color";//
    protected SharedPreferences.Editor editor;
    protected SharedPreferences sharedPreferences;
    protected int color;
    protected final String SHARED_NAME="over_music_shared";
    protected static final String IMAGE_PATH = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/OverMusic/image_bg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);

        Intent intent=new Intent(this, PlayService.class);
        startService(intent);
        initShared();
    }

    private void initShared(){
        sharedPreferences=getSharedPreferences(SHARED_NAME,MODE_PRIVATE);
    }

    protected int getThemeColor(){

        int defaultValue=getResources().getColor(R.color.colorBlueGrey);

        return sharedPreferences.getInt(COLOR,defaultValue);

    }

    protected void saveThemeColor(int color){
        getSharedPreferences(SHARED_NAME,MODE_PRIVATE).edit().putInt(COLOR,color).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    protected abstract void setThemeColor();

    protected void openAlbum() {
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,200);
    }

    protected void startCropImage(Uri uri,int w,int h) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", w);
        intent.putExtra("aspectY", h);
        //输出图片的宽高均为150
        intent.putExtra("outputX", w);
        intent.putExtra("outputY", h);

        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", false);

        intent.putExtra("outImage", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, 300);
    }

    /**
     * 获取背景图
     * @return
     */
    protected Bitmap getDefaultBg(){
        String path=IMAGE_PATH+"/bg";
        return BitmapFactory.decodeFile(path);

    }
}
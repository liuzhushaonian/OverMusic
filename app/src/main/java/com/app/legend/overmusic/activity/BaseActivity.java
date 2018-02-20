package com.app.legend.overmusic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.SettingRingToneEvent;
import com.app.legend.overmusic.service.PlayService;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.RxBus;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BaseActivity extends AppCompatActivity {

    private static final int STORGE=0x000100;
    private static final int SETTINGS=0x000200;
    private Disposable ring_dis;
    private Music music;


    private static final String[] permissionStrings=
            new String[]{Manifest.permission.WRITE_SETTINGS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        register();

        Intent intent=new Intent(this, PlayService.class);
        startService(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregister(ring_dis);
    }

    private void register(){
        ring_dis= RxBus.getDefault().tObservable(SettingRingToneEvent.class).subscribe(settingRingToneEvent -> {

            this.music=settingRingToneEvent.getMusic();

            questPermissionForSettingRingTone();
        });
    }

    private void unregister(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode){
            case STORGE:


                break;
            case SETTINGS:

                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                    setRing();
                }else {
                    Toast.makeText(this,"无法获取权限，设置失败",Toast.LENGTH_LONG).show();
                }

                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

                break;
        }
    }

    private void questPermissionForSettingRingTone(){
        if (ContextCompat.checkSelfPermission(BaseActivity.this, permissionStrings[0])!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(BaseActivity.this, new String[]{permissionStrings[0]},SETTINGS);
        }else {
            setRing();
        }

    }

    private void setRing(){

        if (this.music==null){
            return;
        }


        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Uri uri= Uri.parse(this.music.getUrl());
                    RingtoneManager.setActualDefaultRingtoneUri(OverApplication.getContext(),RingtoneManager.TYPE_RINGTONE,uri);
                    e.onNext(1);

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Toast.makeText(OverApplication.getContext(),"设置成功",Toast.LENGTH_SHORT).show();
                });
    }
}
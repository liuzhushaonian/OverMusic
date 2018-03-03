package com.app.legend.overmusic.activity;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.app.legend.overmusic.R;
import java.util.concurrent.TimeUnit;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseActivity {

    private TextView info,song;
    private static final String[] permissionStrings=
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean getPermission=true;
    private boolean canGo=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getComponent();

        startAnimation(info);
        startAnimation(song);

    }

    private void getComponent(){

        info=findViewById(R.id.textView);
        song=findViewById(R.id.music_name);
    }

    private void startAnimation(View view){

        int margin=getResources().getDimensionPixelSize(R.dimen.bottom_play_bar);
        ObjectAnimator animator=ObjectAnimator.ofFloat(view,"translationY",margin,0).setDuration(2000);
        ObjectAnimator alpha=ObjectAnimator.ofFloat(view,"alpha",0,1).setDuration(2000);

        AnimatorSet set=new AnimatorSet();


        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                info.setVisibility(View.VISIBLE);
                song.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                getPermission();
            }
        });

        set.playTogether(animator,alpha);
        set.start();

//        animator.setDuration(2000).start();
    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(SplashActivity.this, permissionStrings[0])!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{permissionStrings[0]},1000);

        }else {

//            startMainActivity();
            startCountDown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1000:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getPermission=false;
                    startCountDown();
//                    startMainActivity();
                }else {

                    Toast.makeText(SplashActivity.this,"无法获取权限，请赋予相关权限",Toast.LENGTH_SHORT).show();
                }

                break;

                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    break;
        }


    }


    private void startCountDown(){


        new Thread(){
            @Override
            public void run() {
                super.run();

                int i=0;

                while (!canGo){

                    try {
                        sleep(1000);
                        i+=1;

                        Message message=new Message();
                        message.what=100;
                        message.arg1=i;

                        handler.handleMessage(message);
                        if (i>=3){
                            canGo=true;
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();

    }

    private Handler handler=new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100:

                    int t=msg.arg1;

                    if (t==3){
                        canGo=true;
                        startMainActivity();
                    }

                    break;
            }

        }
    };

    private void startMainActivity(){
        finish();
        overridePendingTransition(0,R.anim.fade);
        Intent intent=new Intent(SplashActivity.this,MainActivity.class);
        startActivity(intent);
    }



}

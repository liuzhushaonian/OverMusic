package com.app.legend.overmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.app.legend.overmusic.utils.PlayHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * Created by legend on 2018/3/4.
 */

public class MediaButtonReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {

        String intentAction = intent.getAction() ;
        if(Intent.ACTION_MEDIA_BUTTON.equals(intentAction)){

            KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); //获得KeyEvent对象

                try {
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        PlayHelper.create().getMediaButtonEvent();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
//        abortBroadcast();//终止广播(不让别的程序收到此广播，免受干扰)

    }


}

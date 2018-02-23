package com.app.legend.overmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app.legend.overmusic.utils.PlayHelper;


public class PlayingBroadcast extends BroadcastReceiver {


    public final static String PLAY="play_music";
    public final static String PREVIOUS="previous_music";
    public final static String NEXT="next_music";
    public final static String PAUSE="pause_music";


    @Override
    public void onReceive(Context context, Intent intent) {
//
        String action=intent.getAction();
        if (action!=null) {
            switch (action) {
                case PLAY:
                    PlayHelper.create().playOrPause();

                    break;

                case PREVIOUS:

                    PlayHelper.create().buttonToPrevious();
                    break;
                case NEXT:

                    PlayHelper.create().buttonToNext();
                    break;
            }
        }
    }
}


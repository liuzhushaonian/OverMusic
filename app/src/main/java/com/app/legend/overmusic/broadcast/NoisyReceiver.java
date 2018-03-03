package com.app.legend.overmusic.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.app.legend.overmusic.utils.PlayHelper;
import java.util.Objects;

public class NoisyReceiver extends BroadcastReceiver {

    private static final String action="android.media.AUDIO_BECOMING_NOISY";


    @Override
    public void onReceive(Context context, Intent intent) {


        if(intent!=null&& Objects.equals(intent.getAction(), action)){
            if (PlayHelper.create().getCurrent_music()!=null) {
                PlayHelper.create().pause();
                Toast.makeText(context, "耳机已拔出", Toast.LENGTH_SHORT).show();
            }
        }

    }
}

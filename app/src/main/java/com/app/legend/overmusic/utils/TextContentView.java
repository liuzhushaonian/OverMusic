package com.app.legend.overmusic.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.app.legend.overmusic.R;

/**
 *
 * Created by legend on 2018/3/2.
 */

public class TextContentView extends FrameLayout {

    private String string="Hello World！";


    public TextContentView(Context context) {
        super(context);
    }

    public TextContentView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextContentView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private int[] getStartPosition(){

        int[] position=new int[2];

        int w= (int) (getResources().getDisplayMetrics().widthPixels*0.2);

        int h= (int) (getResources().getDisplayMetrics().heightPixels*0.5);
        position[0]=w;
        position[1]=h;

        return position;
    }

    private int[] getEndPosition(){
        int[] position=new int[2];

        int w= (int) (getResources().getDisplayMetrics().widthPixels-100);

        int h= (int) (getResources().getDisplayMetrics().heightPixels*0.4);
        position[0]=w;
        position[1]=h;

        return position;

    }
}

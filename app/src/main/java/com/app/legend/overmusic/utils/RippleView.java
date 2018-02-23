package com.app.legend.overmusic.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.app.legend.overmusic.R;

/**
 *
 * Created by legend on 2018/2/22.
 */

public class RippleView extends LinearLayout {

    private Paint paint;

    private volatile float radius=0;//半径

    private float cx=0;//横坐标

    private float cy=0;//纵坐标

    private int color;//颜色

    private boolean stop=true;

    private int limit=2000;


    public RippleView(Context context) {
        super(context);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public RippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint=new Paint();

        paint.setColor(this.color);

        paint.setStyle(Paint.Style.FILL);

        paint.setAntiAlias(true);

        canvas.drawCircle(cx,cy,radius,paint);

    }

    private void start(){

        new Thread(){
            @Override
            public void run() {
                super.run();

                while (!stop){

                    try {
                        sleep(10);

                        radius+=10;

                        handler.sendEmptyMessage(1);

                        if (radius>limit) {
                            stop = true;
                            //换肤操作

                            handler.sendEmptyMessage(2);

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
            switch (msg.what) {
                case 1:
                    invalidate();
                    break;
                case 2:

                    setBackgroundColor(color);

                    break;
            }
        }
    };

    public void startRipper(float cx,float cy,int color,int limit){

        if (!stop){

//            Log.d("stop---->>","it is not stop!!");

            return;
        }

        stop=false;
        this.radius=0;
        this.cx=cx;
        this.cy=cy;
        this.color=color;
        this.limit=limit;
        start();
    }

    /**
     * 设置最大限制，建议为手机屏幕
     * @param limit
     */
    public void setLimit(int limit) {

//        Log.d("limit--->>",limit+"");
        this.limit = limit;
    }



}

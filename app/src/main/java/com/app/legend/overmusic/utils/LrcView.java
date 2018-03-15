package com.app.legend.overmusic.utils;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Lrc;
import java.util.List;

/**
 *
 * Created by legend on 2018/3/7.
 */

public class LrcView extends AppCompatTextView {


    private TextPaint currentPaint;
    private TextPaint otherPaint;
    private int currentColor;
    private int otherColor;
    private List<Lrc> lrcList;
    private int index=-1;
    private int height;
    private float textHeight=65;
    private float textSize=getResources().getDimension(R.dimen.text_size);
    private boolean isNull=false;
    private boolean isDouble=false;
    private Rect rect;
    private int write_width;//歌词换行长度
    private int screen_width;
    int defaultValue;


    public void setNull(boolean aNull) {
        isNull = aNull;
    }

    public void setDouble(boolean aDouble) {
        isDouble = aDouble;
    }

    public List<Lrc> getLrcList() {
        return lrcList;
    }

    public void setLrcList(List<Lrc> lrcList) {
        this.lrcList = lrcList;
        for (Lrc lrc:lrcList){
            Log.d("lrc--->>",lrc.getContent());
        }
    }

    public LrcView(Context context) {
        super(context);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 实例化
     */
    private void init(){
        currentPaint=new TextPaint();
        currentPaint.setStyle(Paint.Style.STROKE);
        otherPaint=new TextPaint();
        otherPaint.setStyle(Paint.Style.STROKE);
        currentColor=getResources().getColor(R.color.colorBlue);
        otherColor=getResources().getColor(R.color.colorAmber);
        rect=new Rect(getLeft(),getTop(),getRight(),getBottom());
        screen_width=getResources().getDisplayMetrics().widthPixels;
        defaultValue=getResources().getDimensionPixelSize(R.dimen.default_space);
        write_width=screen_width-2*defaultValue;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas==null||this.index==-1){
            return;
        }

        if (isNull){

            setText("该歌曲为纯音乐，没有歌词喔~");
            return;
        }

        currentPaint.setColor(currentColor);
        otherPaint.setColor(otherColor);
        currentPaint.setTextSize(textSize);
        otherPaint.setTextSize(textSize);
        currentPaint.setAntiAlias(true);
        otherPaint.setAntiAlias(true);

        try {
            setText("");

            /**
             * 当前歌词
             */
            Lrc lrc=lrcList.get(index);

            if (!lrc.getContent().isEmpty()) {

                float w=currentPaint.measureText(lrc.getContent());

                if (w>screen_width){//开启换行

                    StaticLayout myStaticLayout = new StaticLayout(lrc.getContent(), currentPaint,
                            write_width,Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                    canvas.translate(defaultValue,height/2);
                    myStaticLayout.draw(canvas);

                }else {//不换

                    canvas.drawText(lrc.getContent(), (getWidth() - currentPaint.measureText(lrc.getContent())) / 2, height / 2, currentPaint);
                }

//                currentPaint.setTextAlign(Paint.Align.CENTER);
//
//                canvas.restore();

//                canvas.drawText(lrc.getContent()+"\\n 这是测试歌词", (getWidth() - currentPaint.measureText(lrc.getContent())) / 2, height / 2, currentPaint);
            }

            //双份歌词模式
            if (isDouble){
//                for (int i=0;i<lrcList.size();i++){
//
//                    if (i!=index) {//寻找非当前的歌词
//                        Lrc l = lrcList.get(i);
//                        if (l.getTime()==lrc.getTime()){
//                            //同为歌词
//
//                            canvas.drawText(l.getContent(),(getWidth()-currentPaint.measureText(l.getContent()))/2
//                                    ,height/2+textHeight,currentPaint);
//                        }
//                    }
//
//                }
                Lrc tlrc=null;
                if (index==0){
                    tlrc=lrcList.get(index+1);
                    if (!tlrc.getContent().isEmpty()) {
                        canvas.drawText(tlrc.getContent(), (getWidth() - currentPaint.measureText(tlrc.getContent())) / 2
                                , height / 2 + textHeight, currentPaint);
                    }
                }else if (index==lrcList.size()-1){
                    tlrc=lrcList.get(index-1);
                    if (!tlrc.getContent().isEmpty()) {
                        canvas.drawText(tlrc.getContent(), (getWidth() - currentPaint.measureText(tlrc.getContent())) / 2
                                , height / 2 + textHeight, currentPaint);
                    }
                }else {
                    tlrc=lrcList.get(index+1);

                    if (tlrc.getTime()!=lrc.getTime()){
                        tlrc=lrcList.get(index-1);
                        if (tlrc.getTime()==lrc.getTime()){
                            if (!tlrc.getContent().isEmpty()) {
                                canvas.drawText(tlrc.getContent(), (getWidth() - currentPaint.measureText(tlrc.getContent())) / 2
                                        , height / 2 + textHeight, currentPaint);
                            }
                        }
                    }else {

                        if (!tlrc.getContent().isEmpty()) {
                            canvas.drawText(tlrc.getContent(), (getWidth() - currentPaint.measureText(tlrc.getContent())) / 2
                                    , height / 2 + textHeight, currentPaint);
                        }
                    }
                }



                float tempY=height/2-textHeight;

                for (int i = index - 2; i >= 0; i--) {//上一句
                    tempY = tempY - textHeight;
                    if (!lrcList.get(i).getContent().isEmpty()) {
                        canvas.drawText(lrcList.get(i).getContent(),
                                (getWidth() - otherPaint.measureText(lrcList.get(i).getContent())) / 2, tempY, otherPaint);
                    }

                }

                tempY = height / 2+textHeight;

                for (int i = index + 2; i < lrcList.size(); i++) {//下一句
                    tempY = tempY + textHeight;
                    if (!lrcList.get(i).getContent().isEmpty()) {
                        canvas.drawText(lrcList.get(i).getContent(),
                                (getWidth() - otherPaint.measureText(lrcList.get(i).getContent())) / 2, tempY, otherPaint);
                    }

                }





            }else if (isNull){

                setText("此歌曲为纯音乐");

            }else {//普通歌词模式

                float tempY = height / 2;

                for (int i = index - 1; i >= 0; i--) {
                    tempY = tempY - textHeight;
                    canvas.drawText(lrcList.get(i).getContent(),
                            (getWidth()-otherPaint.measureText(lrcList.get(i).getContent()))/2, tempY, otherPaint);

                }

                tempY = height / 2;

                for (int i = index + 1; i < lrcList.size(); i++) {
                    tempY = tempY + textHeight;
                    canvas.drawText(lrcList.get(i).getContent(),
                            (getWidth()-otherPaint.measureText(lrcList.get(i).getContent()))/2, tempY, otherPaint);

                }
            }


        }catch (Exception e){
            setText("尚未有歌词");
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.height=h;
    }

    /**
     * 获取当前歌词位置
     * @param time
     * @return
     */
    public void setIndex(long time){

        if (lrcList==null){
            return;
        }

        if (time<PlayHelper.create().getCurrent_music().getTime()){

            for (int i=0;i<lrcList.size();i++){

                if (i<lrcList.size()-1){

                    if (time<lrcList.get(i).getTime()&&i==0){
                        index=i;
                    }

                    if ((time>lrcList.get(i).getTime())&&time<lrcList.get(i+1).getTime()){
                        index=i;

                    }
                }

                if ((i==lrcList.size()-1)&&time>lrcList.get(i).getTime()){
                    index=i;
                }
            }
        }

    }


}

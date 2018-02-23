package com.app.legend.overmusic.presenter;


import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;
import android.view.LayoutInflater;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Color;
import com.app.legend.overmusic.interfaces.IColorPresenter;
import com.app.legend.overmusic.utils.OverApplication;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/23.
 */

public class ColorPresenter {

    private IColorPresenter activity;

    public ColorPresenter(IColorPresenter activity) {
        this.activity = activity;
    }

    public void getColorData(){

        Observable
                .create((ObservableOnSubscribe<List<Color>>) e -> {
                    List<Color> colors=getColorList();
                    e.onNext(colors);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);
    }

    private void setData(List<Color> colors){
        activity.setData(colors);
    }

    private List<Color> getColorList(){

        List<Color> colors=new ArrayList<>();

        Resources resources=OverApplication.getContext().getResources();

        XmlResourceParser xmlResourceParser=resources.getXml(R.xml.colors);

        try {
            while (xmlResourceParser.getEventType()!=XmlResourceParser.END_DOCUMENT){

                if (xmlResourceParser.getEventType()== XmlResourceParser.START_TAG){

                    String name=xmlResourceParser.getName();
                    if (name.equals("color")){

                        String n=xmlResourceParser.getAttributeValue(1);
                        int id= Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                        String c=xmlResourceParser.nextText();
                        Log.d("id---->>",id+"");
                        int use=0;

                        int color_int= android.graphics.Color.parseColor(c);

                        Color color=new Color();
                        color.setColor(color_int);
                        color.setName(n);
                        color.setId(id);
                        color.setIs_use(use);

                        colors.add(color);
                    }
                }

                xmlResourceParser.next();

            }



        } catch (Exception e) {
            e.printStackTrace();
        }


        return colors;
    }
}

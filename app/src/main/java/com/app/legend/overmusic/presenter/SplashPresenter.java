package com.app.legend.overmusic.presenter;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Color;
import com.app.legend.overmusic.bean.ShowInfo;
import com.app.legend.overmusic.interfaces.ISplashPresenter;
import com.app.legend.overmusic.utils.OverApplication;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/3/5.
 */

public class SplashPresenter {

    private ISplashPresenter activity;


    public SplashPresenter(ISplashPresenter activity) {
        this.activity = activity;
    }

    public void getShowData(){
        Observable
                .create((ObservableOnSubscribe<List<ShowInfo>>) e -> {
                    List<ShowInfo> showInfos=getXmlData();
                    e.onNext(showInfos);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setShowData);
    }

    private void setShowData(List<ShowInfo> showData){
        if (this.activity!=null){
            activity.setInfo(showData);
        }
    }

    private List<ShowInfo> getXmlData(){

        Resources resources= OverApplication.getContext().getResources();

        XmlResourceParser xmlResourceParser=resources.getXml(R.xml.lyrics_show);

        List<ShowInfo> showInfos=new ArrayList<>();

        try {
            while (xmlResourceParser.getEventType()!=XmlResourceParser.END_DOCUMENT){

                if (xmlResourceParser.getEventType()== XmlResourceParser.START_TAG){

                    String name=xmlResourceParser.getName();
                    if (name.equals("item")){

                        String n=xmlResourceParser.getAttributeValue(1);
                        int id= Integer.parseInt(xmlResourceParser.getAttributeValue(0));
                        String l=xmlResourceParser.nextText();

                        ShowInfo showInfo=new ShowInfo();

                        showInfo.setId(id);
                        showInfo.setInfo(l);
                        showInfo.setSong(n);

                        showInfos.add(showInfo);
                    }
                }

                xmlResourceParser.next();

            }



        } catch (Exception e) {
            e.printStackTrace();
        }

        return showInfos;

    }
}

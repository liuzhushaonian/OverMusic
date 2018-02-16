package com.app.legend.overmusic.presenter;

import com.app.legend.overmusic.interfaces.ISearchPresenter;
import com.app.legend.overmusic.utils.Database;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/14.
 */

public class SearchPresenter {

    private ISearchPresenter activity;

    public SearchPresenter(ISearchPresenter activity) {
        this.activity = activity;
    }

    public void getData(){

        Observable
                .create((ObservableOnSubscribe<List<String>>) e -> {
                    List<String> strings= Database.getDefault().getHistory();
                    e.onNext(strings);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);
    }

    private void setData(List<String> stringList){
        activity.setData(stringList);
    }

    public void queryData(String data){
        Observable
                .create((ObservableOnSubscribe<String>) e -> {
                    Database.getDefault().addHistory(data);
                    e.onNext(data);
                })
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setQueryData);

    }

    private void setQueryData(String data){
        activity.queryDataByFragment(data);
    }

}

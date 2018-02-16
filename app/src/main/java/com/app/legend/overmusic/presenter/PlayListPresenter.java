package com.app.legend.overmusic.presenter;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.interfaces.IPlayListPresenter;
import com.app.legend.overmusic.utils.Database;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.RxBus;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class PlayListPresenter {

    private IPlayListPresenter fragment;

    public PlayListPresenter(IPlayListPresenter fragment) {
        this.fragment = fragment;
    }

    public void getData() {

        Observable
                .create((ObservableOnSubscribe<List<PlayList>>) e -> {
                    List<PlayList> playLists= Database.getDefault().getAllPlayList();
                    e.onNext(playLists);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData);
    }

    private void setData(List<PlayList> playLists){
        fragment.setData(playLists);
    }

    public void deleteList(Activity activity,PlayList playList){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        builder.setPositiveButton("确定", (dialog, which) -> {
            delete(playList);
        });

        builder.setNegativeButton("取消",(dialog, which) -> {

            builder.create().cancel();
        });

        builder.setTitle("确定删除？").setMessage("此操作无法撤销");
        builder.show();

    }

    private void delete(PlayList playList){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    int result=Database.getDefault().deleteList(playList);
                    e.onNext(result);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer==1) {
                        Toast.makeText(OverApplication.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                        getData();
                    }
                });

    }

    //修改列表名称
    public void renameList(Activity activity,PlayList playList){

        final AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        final EditText editText= new EditText(activity);
        editText.setText(playList.getName());
        editText.selectAll();

        builder.setPositiveButton("确定", (dialog, which) -> {
            String list=editText.getText().toString();
            if (list.isEmpty()){
                Toast.makeText(activity,"列表名称不能为空",Toast.LENGTH_SHORT).show();
            }else {
                rename(playList,list);
            }
        });

        builder.setView(editText).setTitle("重命名");
        builder.show();

    }

    private void rename(PlayList playList,String name){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    int result=Database.getDefault().renameList(playList,name);
                    e.onNext(result);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (integer==1){
                        Toast.makeText(OverApplication.getContext(),"修改成功",Toast.LENGTH_SHORT).show();
                        getData();
                    }else {
                        Toast.makeText(OverApplication.getContext(),"列表已存在，修改失败",Toast.LENGTH_SHORT).show();
                    }
                });
    }


}

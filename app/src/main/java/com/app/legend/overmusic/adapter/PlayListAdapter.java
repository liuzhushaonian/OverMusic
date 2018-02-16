package com.app.legend.overmusic.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.event.DeletePlayListEvent;
import com.app.legend.overmusic.event.RenamePlayListEvent;
import com.app.legend.overmusic.fragment.PlayListInfoFragment;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;

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
 * Created by legend on 2018/2/12.
 */

public class PlayListAdapter extends BaseAdapter<PlayListAdapter.ViewHolder>{

    private List<PlayList> playListList;



    public void setPlayListList(List<PlayList> playListList) {
        this.playListList = playListList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.play_list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            open(position);
        });

        viewHolder.button.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();

            setPopupMenu(v,R.menu.play_list_menu);

            clickMenu(position);
        });




        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (playListList!=null){

            PlayList playList=playListList.get(position);

//            String[] strings=playList.getSongs().split(";");

            holder.name.setText(playList.getName());

            String info=playList.getLength()+"首歌曲";

            holder.info.setText(info);

        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        if (playListList!=null){
            return playListList.size();
        }

        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        View view;
        ImageView book,button;
        TextView name,info;


        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            book=itemView.findViewById(R.id.play_list_album);
            button=itemView.findViewById(R.id.play_list_button);
            name=itemView.findViewById(R.id.play_list_name);
            info=itemView.findViewById(R.id.play_list_info);
        }
    }

    private void open(int position){
        PlayList playList=playListList.get(position);
        PlayListInfoFragment fragment=new PlayListInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable("play_list",playList);
        fragment.setArguments(bundle);
        addFragment(fragment);

    }

    private void clickMenu(int position){
        PlayList playList=playListList.get(position);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.play_list_play:
                    playAllMusic(playList);
                    break;
                case R.id.play_list_rename:
                    rename(playList);
                    break;
                case R.id.play_list_delete:
                    deleteList(playList);
                    break;


            }


            return true;
        });
    }

    private void playAllMusic(PlayList playList){

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {
                    List<Music> musicList= Mp3Util.newInstance().getPlayListMusic(playList);
                    e.onNext(musicList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(music -> {
                    PlayHelper.create().playAndUpdate(music.get(0),music,0);
                });

    }

    private void rename(PlayList playList){
        RxBus.getDefault().post(new RenamePlayListEvent(playList));

    }

    private void deleteList(PlayList playList){

        RxBus.getDefault().post(new DeletePlayListEvent(playList));
    }

//    private void addFragment(Fragment fragment){
//        RxBus.getDefault().post(new AddFragmentEvent(fragment));
//    }
}

package com.app.legend.overmusic.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Music;

import java.util.List;
import java.util.zip.Inflater;

/**
 *
 * Created by legend on 2018/2/7.
 */

public class AlbumMusicAdapter extends BaseAdapter<AlbumMusicAdapter.ViewHolder> {


    List<Music> musicList;



    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.music_list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);


    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{



        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

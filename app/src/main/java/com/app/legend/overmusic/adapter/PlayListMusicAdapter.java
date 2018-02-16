package com.app.legend.overmusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Music;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/13
 * .
 */

public class PlayListMusicAdapter extends BaseAdapter<PlayListMusicAdapter.ViewHolder> {

    private List<Music> allMusicList;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.music_list_item, parent, false);
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

    static class ViewHolder extends BaseAdapter.ViewHolder {

        ImageView album_book, state, menu;
        TextView song, info;
        View view;

        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            album_book = itemView.findViewById(R.id.music_list_album);
            state = itemView.findViewById(R.id.music_list_play_state);
            menu = itemView.findViewById(R.id.music_list_button);

            song = itemView.findViewById(R.id.music_list_song_name);
            info = itemView.findViewById(R.id.music_list_info);
        }


    }
}

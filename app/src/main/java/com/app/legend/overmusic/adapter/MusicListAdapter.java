package com.app.legend.overmusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/20.
 */

public class MusicListAdapter extends BaseAdapter<MusicListAdapter.ViewHolder> {

    private List<Integer> musicPositionList;

    public void setMusicPositionList(List<Integer> musicPositionList) {
        this.musicPositionList = musicPositionList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {


        });

        viewHolder.menu.setOnClickListener(v -> {

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (musicPositionList!=null){
            int p=musicPositionList.get(position);

            Music music= PlayHelper.create().getPagerMusic(p);

            holder.song.setText(music.getSongName());
            String info=music.getArtistName()+" | "+music.getAlbumName();

            holder.info.setText(info);

            int w=OverApplication.getContext().getResources().getDimensionPixelSize(R.dimen.press_space);

            ImageLoader.getImageLoader(OverApplication.getContext()).setAlbum(music,holder.album_book,ImageLoader.SMALL,w,w);




        }else {

            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (musicPositionList!=null){
            return musicPositionList.size();
        }
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

    private void playMusic(int position){

        int p = musicPositionList.get(position);//获取真实地址

        Music music=PlayHelper.create().getPagerMusic(p);




    }
}

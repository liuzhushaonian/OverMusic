package com.app.legend.overmusic.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.OpenAlbumFragmentEvent;
import com.app.legend.overmusic.event.OpenArtistFragmentEvent;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;

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
            int position=viewHolder.getAdapterPosition();
            playMusic(position);
        });

        viewHolder.menu.setOnClickListener(v -> {

            setPopupMenu(v,R.menu.playing_music_menu);
            int position=viewHolder.getAdapterPosition();

            clickMenu(position);
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (musicPositionList!=null){
            int p=musicPositionList.get(position);

            Music music= PlayHelper.create().getPagerMusic(position);

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

        Music music=PlayHelper.create().getPagerMusic(position);

        PlayHelper.create().playMusicByClickPlayingList(music,position);

    }

    private void clickMenu(int position){

        Music music=PlayHelper.create().getPagerMusic(position);


        popupMenu.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()){

                case R.id.music_play:
                    //播放
                    playMusic(position);

                    break;
                case R.id.music_add_to_list:


                    //添加至列表
                    break;

                case R.id.set_as_ring:


                    break;
                case R.id.next_play:
                    PlayHelper.create().addNextMusic(music);
                    //设为下一曲播放
                    break;
                case R.id.see_artist:
                    Artist artist=new Artist();
                    artist.setName(music.getArtistName());
                    artist.setId(music.getArtistId());


                    openArtist(artist);
                    //查看歌手
                    break;
                case R.id.see_album:

                    Album album=new Album();
                    album.setId(music.getAlbumId());
                    album.setArtist(music.getArtistName());
                    album.setAlbum_name(music.getAlbumName());
                    album.setArtist_id(music.getArtistId());

                    openAlbum(album);

                    //查看专辑
                    break;

//                case R.id.music_delete:
//
////                    deleteMusicPosition(position);
//                    //删除
//                    break;


            }


            return true;
        });
    }

    private void deleteMusicPosition(int position){
//        musicPositionList.remove(position);
//        notifyDataSetChanged();

        PlayHelper.create().deleteMusicPosition(position);

        notifyDataSetChanged();


    }

    private void openArtist(Artist artist){
        RxBus.getDefault().post(new OpenArtistFragmentEvent(artist));
    }

    private void openAlbum(Album album){
        RxBus.getDefault().post(new OpenAlbumFragmentEvent(album));
    }
}

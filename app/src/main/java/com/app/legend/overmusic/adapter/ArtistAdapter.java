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
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AddArtistMusicToList;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.event.SearchArtistEvent;
import com.app.legend.overmusic.fragment.ArtistInfoFragment;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.PlayHelper;
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
 * Created by legend on 2018/2/6.
 */

public class ArtistAdapter extends BaseAdapter<ArtistAdapter.ViewHolder> {

    private List<Artist> artists;
    private int type=0;
    public static final int NORMAL=0x00100;
    public static final int SEARCH=0x00300;


    public ArtistAdapter(int type) {
        this.type = type;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.artist_list_item, parent, false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            open(position);
        });

        viewHolder.button.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            setPopupMenu(v,R.menu.artist_menu);
            clickMenu(position);

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (artists!=null){
            Artist artist=artists.get(position);
            holder.name.setText(artist.getName());
//            holder.info.setText(artist.getAlbum());
            holder.family_name.setText(getFirst(artist.getName()));
            if (type==SEARCH){
                holder.button.setVisibility(View.GONE);
            }


        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        if (artists!=null){
            return artists.size();
        }
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        TextView family_name,name,info;
        ImageView button;
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            family_name=itemView.findViewById(R.id.artist_list_album);
            name=itemView.findViewById(R.id.artist_list_song_name);
//            info=itemView.findViewById(R.id.artist_list_info);

            button=itemView.findViewById(R.id.artist_list_button);
        }
    }

    private String getFirst(String name){

        String f=name.substring(0,1);

        return f;
    }

    private void open(int position){

        Artist artist = artists.get(position);
        if (type!=SEARCH) {

            ArtistInfoFragment fragment = new ArtistInfoFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ArtistInfoFragment.TAG, artist);
            fragment.setArguments(bundle);

            addFragment(fragment);
        }else {
            infoForSearch(artist);
        }

    }

    private void clickMenu(int position){
        Artist artist=artists.get(position);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.artist_open:
                    open(position);
                    break;
                case R.id.artist_play:
                    playAllArtistMusic(artist);
                    break;
                case R.id.artist_add_to_list:
                    addToList(artist);
                    break;
                case R.id.artist_delete:
                    deleteArtist(artist);
                    break;

            }


            return true;
        });
    }

    private void playAllArtistMusic(Artist artist){

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {
                    List<Music> musicList= Mp3Util.newInstance().getArtistMusic(artist);
                    e.onNext(musicList);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(music -> {
//                    PlayHelper.create().playAndUpdate(music.get(0),music,0);
                    PlayHelper.create().playMusicAndUpdateList(music,0);
                });
    }

    private void deleteArtist(Artist artist){
        if (artists.contains(artist)){
            artists.remove(artist);
            notifyDataSetChanged();
        }

    }

    //添加歌曲到列表事件
    private void addToList(Artist artist){
        RxBus.getDefault().post(new AddArtistMusicToList(artist));
    }

    //搜索页面点击事件
    private void infoForSearch(Artist artist){
        RxBus.getDefault().post(new SearchArtistEvent(artist));
    }

//    private void addFragment(Fragment fragment){
//        RxBus.getDefault().post(new AddFragmentEvent(fragment));
//    }
}

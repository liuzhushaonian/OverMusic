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
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AddAlbumMusicToListEvent;
import com.app.legend.overmusic.event.AddFragmentEvent;
import com.app.legend.overmusic.fragment.AlbumInfoFragment;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.Mp3Util;
import com.app.legend.overmusic.utils.OverApplication;
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

public class AlbumAdapter extends BaseAdapter<AlbumAdapter.ViewHolder> {

    private List<Album> albums;



    public void setAlbums(List<Album> albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.album_list_item, parent, false);
        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            open(position);
        });

        viewHolder.button.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            setPopupMenu(v,R.menu.album_menu);
            menuClick(position);

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (albums!=null){

            Album album=albums.get(position);
            holder.name.setText(album.getAlbum_name());
            holder.artist.setText(album.getArtist());
            ImageLoader.getImageLoader(OverApplication.getContext())
                    .setAlbumInfoBook(album.getId(),holder.book,ImageLoader.ALBUM,holder.width,holder.width);
        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        if (albums!=null){
            return albums.size();
        }

        return 0;
    }

    static class ViewHolder extends BaseAdapter.ViewHolder{

        View view;
        TextView name,artist;
        ImageView book,button;
        int width=0;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            name=itemView.findViewById(R.id.album_name);
            artist=itemView.findViewById(R.id.album_info);
            book=itemView.findViewById(R.id.album_book);
            button=itemView.findViewById(R.id.album_button);

            reDraw();
        }

        private void reDraw(){

            ViewGroup.LayoutParams layoutParams=view.getLayoutParams();
            int space= OverApplication.getContext().getResources().getDimensionPixelSize(R.dimen.album_space);
            int width=OverApplication.getContext().getResources().getDisplayMetrics().widthPixels;
            int defaultSpace=OverApplication.getContext().getResources().getDimensionPixelSize(R.dimen.press_space);
            int itemWidth=(width-space)/2;
            this.width=itemWidth;
            int itemHeight=itemWidth+defaultSpace;

            layoutParams.width=itemWidth;
            layoutParams.height=itemHeight;

            view.setLayoutParams(layoutParams);

            ViewGroup.LayoutParams imageParams=book.getLayoutParams();

            imageParams.height=itemWidth;

            book.setLayoutParams(imageParams);


        }
    }

    private void open(int position){
        Album album=albums.get(position);
        Fragment fragment=new AlbumInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(AlbumInfoFragment.TAG,album);
        fragment.setArguments(bundle);
        addFragment(fragment);

    }

    private void menuClick(int position){
        Album album=albums.get(position);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.open_album:
                    open(position);

                    break;
                case R.id.play_album:

                    playAllAlbumMusic(album);
                    break;
                case R.id.add_list:

                    addToList(album);
                    break;
                case R.id.save_album_book:
                    saveBitmap(album.getId());
                    break;
                case R.id.album_delete:

                    deleteAlbum(album);
                    break;
            }


            return true;
        });

    }

    //播放整张专辑歌曲
    private void playAllAlbumMusic(Album album){

        Observable
                .create((ObservableOnSubscribe<List<Music>>) e -> {
                    List<Music> musicList= Mp3Util.newInstance().getAlbumMusic(album);
                    if (musicList!=null){
                        e.onNext(musicList);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(musicList -> {
//                    PlayHelper.create().playAndUpdate(musicList.get(0),musicList,0);
                    PlayHelper.create().playMusicAndUpdateList(musicList,0);
                });
    }

    private void addToList(Album album){
        RxBus.getDefault().post(new AddAlbumMusicToListEvent(album));
    }

    private void deleteAlbum(Album album){
        if (albums.contains(album)){
            albums.remove(album);
            notifyDataSetChanged();
        }
    }

}

package com.app.legend.overmusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.event.SearchAlbumEvent;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.RxBus;

import java.util.List;

/**
 *
 * Created by legend on 2018/2/14.
 */

public class SearchAlbumAdapter extends BaseAdapter<SearchAlbumAdapter.ViewHolder> {

    private List<Album> albumList;

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.search_album_list_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            Album album=albumList.get(position);
            infoForSearch(album);
        });



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(albumList!=null){
            Album album=albumList.get(position);
            holder.name.setText(album.getAlbum_name());
            holder.info.setText(album.getArtist());

            int width = OverApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.press_space);

            ImageLoader.getImageLoader(OverApplication.getContext()).setAlbumInfoBook(album.getId(),holder.book,ImageLoader.SMALL,width,width);

        }else {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        if (albumList!=null){
            return albumList.size();
        }
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        View view;
        ImageView book,button;
        TextView name,info;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            book=itemView.findViewById(R.id.search_album_list_book);
//            button=itemView.findViewById(R.id.search_album_list_button);
            name=itemView.findViewById(R.id.search_album_list_song_name);
            info=itemView.findViewById(R.id.search_album_list_info);
        }
    }

    private void infoForSearch(Album album){
        RxBus.getDefault().post(new SearchAlbumEvent(album));
    }


}

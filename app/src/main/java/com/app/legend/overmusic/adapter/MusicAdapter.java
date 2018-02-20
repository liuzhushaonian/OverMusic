package com.app.legend.overmusic.adapter;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.event.AddMusicToListEvent;
import com.app.legend.overmusic.event.DeleteMusicEvent;
import com.app.legend.overmusic.event.ListStatusEvent;
import com.app.legend.overmusic.event.SettingRingToneEvent;
import com.app.legend.overmusic.fragment.AlbumInfoFragment;
import com.app.legend.overmusic.fragment.ArtistInfoFragment;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.PlayHelper;
import com.app.legend.overmusic.utils.RxBus;
import java.util.ArrayList;
import java.util.List;
import io.reactivex.disposables.Disposable;


/**
 *音乐列表adapter
 * Created by legend on 2018/1/29.
 */

public class MusicAdapter extends BaseAdapter<MusicAdapter.ViewHolder> {

    private List<Music> allMusicList;

    private Disposable disposable;

    public static final int ALL_MUSIC=0x0000100;
    public static final int ALBUM_MUSIC=0x0000200;
    public static final int ARTIST_MUSIC=0x0000300;
    public static final int LIST_MUSIC=0x0000400;
    public static final int SEARCH_LIST_MUSIC=0x0000500;
    public static final int BOTTOM=0x0000600;



    private int type=-1;



    public MusicAdapter(int type) {
        this.type=type;
        register();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.music_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.view.setOnClickListener(v -> {
            int position = viewHolder.getAdapterPosition();
//            Music music = allMusicList.get(position);
//            PlayHelper.create().playAndUpdate(music, allMusicList, position);
//            music.setPlayStatus(1);
            play(position);

        });

        viewHolder.menu.setOnClickListener(v -> {
            switch (type) {

                case ALL_MUSIC:
                    setPopupMenu(v, R.menu.music_menu);
                    break;
                case ALBUM_MUSIC:
                    setPopupMenu(v,R.menu.album_info_music_menu);
                    break;
                case ARTIST_MUSIC:
                    setPopupMenu(v,R.menu.artist_info_music_menu);
                    break;
                case LIST_MUSIC:
                    setPopupMenu(v,R.menu.play_list_music_menu);
                    break;
                case SEARCH_LIST_MUSIC:
                    setPopupMenu(v, R.menu.music_menu);
                    break;

            }


            int position=viewHolder.getAdapterPosition();
            menuClick(position);

        });



        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (allMusicList != null) {
            Music music = allMusicList.get(position);

            holder.song.setText(music.getSongName());
            String info = music.getArtistName() + " | " + music.getAlbumName();

            holder.info.setText(info);

            int width = OverApplication.getContext().getResources().getDimensionPixelOffset(R.dimen.press_space);

            ImageLoader.getImageLoader(OverApplication.getContext()).setAlbum(music, holder.album_book, ImageLoader.SMALL, width, width);

            if (type!=LIST_MUSIC&&type!=SEARCH_LIST_MUSIC) {
                if (music.getPlayStatus() > 0 && PlayHelper.create().getCurrent_music().getId() == music.getId()) {
                    holder.state.setVisibility(View.VISIBLE);
                } else {
                    holder.state.setVisibility(View.GONE);
                }
            }

            if (type==SEARCH_LIST_MUSIC){
                holder.menu.setVisibility(View.GONE);
            }

        }


    }

    @Override
    public int getItemCount() {

        if (allMusicList != null) {
            return allMusicList.size();
        }

        return 0;
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        ImageView album_book, state, menu;
        TextView song, info;
        View view;

        private ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            album_book = itemView.findViewById(R.id.music_list_album);
            state = itemView.findViewById(R.id.music_list_play_state);
            menu = itemView.findViewById(R.id.music_list_button);

            song = itemView.findViewById(R.id.music_list_song_name);
            info = itemView.findViewById(R.id.music_list_info);

        }
    }

    public void setData(List<Music> list) {

        this.allMusicList = list;
        notifyDataSetChanged();
    }

    //显示播放状态
    private void changeItemShow(Music current) {
        current.setPlayStatus(1);
        int position = allMusicList.indexOf(current);
        notifyItemChanged(position);
    }

    //隐藏播放状态
    private void changeItemHide(Music pre) {
        if (pre != null) {
            if (allMusicList.contains(pre)) {
                pre.setPlayStatus(-1);
                int position = allMusicList.indexOf(pre);
                notifyItemChanged(position);
            }
        }

    }

    private void register(){

       disposable= RxBus.getDefault().tObservable(ListStatusEvent.class).subscribe(listStatusEvent -> {
           Music pre=listStatusEvent.getPre_music();
           Music current=listStatusEvent.getCurrent_music();
           if (type!=LIST_MUSIC&&type!=SEARCH_LIST_MUSIC) {
               changeItemShow(current);
               changeItemHide(pre);
           }
        });
    }

    public void unregister(){
        if (!disposable.isDisposed()){
            disposable.dispose();
        }
    }

    private void play(int position){
        Music music=allMusicList.get(position);
        if (type!=SEARCH_LIST_MUSIC) {
            PlayHelper.create().playAndUpdate(music, allMusicList, position);

        }else {//搜索结果点击播放时，仅播放点击结果歌曲
            List<Music> musicList=new ArrayList<>();
            musicList.add(music);
            PlayHelper.create().playAndUpdate(music,musicList,0);
        }

        music.setPlayStatus(1);

    }

    private void menuClick(int position){



        popupMenu.setOnMenuItemClickListener(item -> {

            Music music=allMusicList.get(position);

            switch (item.getItemId()){
                case R.id.music_play:
                    //播放
                    play(position);

                    break;
                case R.id.music_add_to_list:

//                    addMusicToList(music);
                    //添加至列表
                    break;

                case R.id.set_as_ring:
//                    settingRingTone(music);
//                    Mp3Util.newInstance().setRingTone(music);
                    //设为铃声
                    break;
                case R.id.next_play:
//                    PlayHelper.create().addNextMusic(music);
                    //设为下一曲播放
                    break;
                case R.id.see_artist:

                    toArtistFragment(music);

                    //查看歌手
                    break;
                case R.id.see_album:

                    toAlbumFragment(music);
                    //查看专辑
                    break;

                case R.id.list_delete:
                    deleteMusic(music,position);
                    break;

            }


            return true;
        });

    }

    //跳转歌手页面
    private void toArtistFragment(Music music){

        Artist artist=new Artist();
        artist.setAlbum(music.getAlbumName());
        artist.setName(music.getArtistName());
        artist.setId(music.getArtistId());

        ArtistInfoFragment fragment=new ArtistInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(ArtistInfoFragment.TAG,artist);
        fragment.setArguments(bundle);
        addFragment(fragment);

    }

    //跳转专辑页面
    private void toAlbumFragment(Music music){
        Album album=new Album();
        album.setArtist(music.getArtistName());
        album.setId(music.getAlbumId());
        album.setAlbum_name(music.getAlbumName());

        AlbumInfoFragment fragment=new AlbumInfoFragment();
        Bundle bundle=new Bundle();
        bundle.putSerializable(AlbumInfoFragment.TAG,album);

        fragment.setArguments(bundle);

        addFragment(fragment);

    }

//    添加音乐到列表
    private void addMusicToList(Music music){
        RxBus.getDefault().post(new AddMusicToListEvent(music));
    }

    //删除音乐，用于列表
    private void deleteMusic(Music music,int position){
        if (allMusicList.contains(music)){
            allMusicList.remove(music);
            notifyDataSetChanged();
            RxBus.getDefault().post(new DeleteMusicEvent(music,position));
        }

    }

    private void settingRingTone(Music music){
        RxBus.getDefault().post(new SettingRingToneEvent(music));
    }

//    private void addFragment(Fragment fragment){
//
//    }


}
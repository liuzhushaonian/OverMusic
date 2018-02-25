package com.app.legend.overmusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *
 * Created by legend on 2018/1/25.
 */

public class Mp3Util {

    private static volatile Mp3Util mp3Util;

    private ArrayList<Music> mp3InfoArrayList;

    private void getMp3InfoArrayList(Context context){
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        mp3InfoArrayList=new ArrayList<>();
        assert cursor != null;
        for (int i = 0; i<cursor.getCount(); i++){
            cursor.moveToNext();
            String uri=cursor.getString(cursor.getColumnIndex((MediaStore.Audio.Media.DATA)));
            long _id=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            long duration=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));


            if (isMusic!=0){
                Music music=new Music();

                music.setAlbumId(albumsId);
                music.setAlbumName(albums);
                music.setArtistId(artistId);
                music.setArtistName(artist);
                music.setSongName(title);
                music.setTime(duration);
                music.setUrl(uri);
                music.setId(_id);
                mp3InfoArrayList.add(music);
            }
        }
        cursor.close();
    }

    public static Mp3Util newInstance(){

        if (mp3Util==null){
            synchronized (Mp3Util.class) {
                mp3Util = new Mp3Util();
            }
        }

        return mp3Util;
    }

    public void scanMusic(){
        getMp3InfoArrayList(OverApplication.getContext());


    }

    public List<Music> getAllMusic(Context context){


        if (mp3InfoArrayList==null){

            getMp3InfoArrayList(context);
        }

        return mp3InfoArrayList;
    }

    /**
     * 获取album
     * @return
     */
    public List<Album> getAlbumList(){
        List<Album> list=getAlbumSet();

        return list;
    }


    /**
     * 获取artist
     * @return
     */
    public List<Artist> getArtistList(){
        List<Artist> artists=getArtistSet();

        return artists;
    }

    //获取全部专辑
    private List<Album> getAlbumSet(){

        Cursor cursor = OverApplication.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        Set<Integer> set=new HashSet<>();
        List<Album> albumList=new ArrayList<>();

        assert cursor!=null;
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            if (isMusic!=0){

                Album album=new Album();
                album.setAlbum_name(albums);
                album.setId(albumsId);
                album.setArtist(artist);
                album.setArtist_id(artistId);
                if (!set.contains(albumsId)) {
                    albumList.add(album);
                }

                set.add(albumsId);

            }

        }

        return albumList;

    }

    //获取全部歌手
    private List<Artist> getArtistSet(){
        Cursor cursor = OverApplication.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=18000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        List<Artist> list=new ArrayList<>();

        Set<Long> set=new HashSet<>();

        assert cursor!=null;
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long artistId=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            if (isMusic!=0){
                Artist artist1=new Artist();
                artist1.setId(artistId);
                artist1.setName(artist);
                artist1.setAlbum(albums);

                if (!set.contains(artistId)){
                    list.add(artist1);
                }

                set.add(artistId);
            }
        }

        return list;

    }


    //根据album的id获取相关music
    public List<Music> getAlbumMusic(Album album){
        List<Music> albumMusicList=new ArrayList<>();
        int id=album.getId();

        for (Music music:mp3InfoArrayList){
            if (music.getAlbumId()==id){
                albumMusicList.add(music);
            }
        }

        return albumMusicList;
    }

    //根据artist的id获取相关music
    public List<Music> getArtistMusic(Artist artist){
        List<Music> artistMusicList=new ArrayList<>();
        long id=artist.getId();
        for (Music music:mp3InfoArrayList){
            if (music.getArtistId()==id){
                artistMusicList.add(music);
            }
        }

        return artistMusicList;
    }

    public List<Album> getArtistAlbum(Artist artist){
        List<Album> albumList=getArtistAlbumList(artist);

        Log.d("size--->>",albumList.size()+"");
        return albumList;
    }

    private List<Album> getArtistAlbumList(Artist artists){

        Cursor cursor = OverApplication.getContext().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.ARTIST_ID + "="+artists.getId(), null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        List<Album> albumList=new ArrayList<>();
        Set<Integer> set=new HashSet<>();

        assert cursor!=null;
        for (int i=0;i<cursor.getCount();i++){
            cursor.moveToNext();
            String albums=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            int albumsId=cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
            int isMusic=cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
            String artist=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            if (isMusic!=0){

                Album album=new Album();
                album.setAlbum_name(albums);
                album.setId(albumsId);
                album.setArtist(artist);
                if (!set.contains(albumsId)) {
                    albumList.add(album);
                }
                set.add(albumsId);
            }

        }

        return albumList;
    }


    //获取列表音乐
    public List<Music> getPlayListMusic(PlayList playList){
        List<Music> musicList=new ArrayList<>();
        String songs=playList.getSongs();
        if (songs.isEmpty()){
            return musicList;
        }

        String[] strings=songs.split(";");

        for (String s:strings){
            long id= Long.parseLong(s);
            Music music=getIdMusic(id);

            if (music!=null){
                musicList.add(music);
            }
        }

        return musicList;


    }

    private Music getIdMusic(long id){

        for (Music music:mp3InfoArrayList){
            if (music.getId()==id){
                return music;
            }
        }

        return null;
    }


    //转换时间
    public static String formatTime(long time){
//        String min=time/(1000*60)+"";
//        String sec=time%(1000*60)+"";
//        if (min.length()<1){
//            min="0"+time/(1000*60)+"";
//        }else {
//            min=time/(1000*60)+"";
//        }
//        if (sec.length()==4){
//            sec="0"+(time%(1000*60))+"";
//        }else if (sec.length()==3){
//            sec="00"+(time%(1000*60))+"";
//        }else if (sec.length()==2){
//            sec="000"+(time%(1000*60))+"";
//        }else if (sec.length()==1){
//            sec="0000"+(time%(1000*60))+"";
//        }
//
//        return min+":"+sec.trim().substring(0,2);

        SimpleDateFormat format=new SimpleDateFormat("mm:ss");

        format.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        return format.format(time);
    }

    //搜索获取音乐
    public List<Music> getSearchMusic(String string){
        List<Music> musicList=new ArrayList<>();
        for (Music music:mp3InfoArrayList){
            String name=music.getSongName();
            if (name.contains(string)){
                musicList.add(music);
            }
        }

        return musicList;

    }

    public List<Artist> getSearchArtist(String string){
        List<Artist> artistList=new ArrayList<>();
        for (Music music:mp3InfoArrayList){
            String artist=music.getArtistName();
            if (artist.contains(string)){
                Artist arti=new Artist();
                arti.setId(music.getArtistId());
                arti.setName(music.getArtistName());


                artistList.add(arti);
            }
        }

        return artistList;

    }

    public List<Album> getSearchAlbum(String string){
        List<Album> albumList=new ArrayList<>();
        for (Music music:mp3InfoArrayList){
            String albumName=music.getAlbumName();
            if (albumName.contains(string)){
                Album album=new Album();
                album.setArtist_id(music.getArtistId());
                album.setArtist(music.getArtistName());
                album.setAlbum_name(music.getAlbumName());
                album.setId(music.getAlbumId());

                albumList.add(album);
            }
        }

        return albumList;

    }

    public void setRingTone(Music music){
        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Uri uri= Uri.parse(music.getUrl());
                    RingtoneManager.setActualDefaultRingtoneUri(OverApplication.getContext(),RingtoneManager.TYPE_RINGTONE,uri);
                    e.onNext(1);

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    Toast.makeText(OverApplication.getContext(),"设置成功",Toast.LENGTH_SHORT).show();
                });

    }

    public Music getListPositionMusic(int position){

        if (this.mp3InfoArrayList==null){
            getMp3InfoArrayList(OverApplication.getContext());
        }

        return mp3InfoArrayList.get(position);
    }


}

package com.app.legend.overmusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.bean.PlayList;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by legend on 2018/2/12.
 */

public class Database extends SQLiteOpenHelper{

    private static volatile Database database;
    private static final String MUSICDATABASE="OverMusicDatabase";//数据库名称
    private static int VERSION=1;//数据库版本
    private SQLiteDatabase sqLiteDatabase;//数据库实例
    private static final String DEFAULT_TABLE="PlayList";
    private static final String ID="id";
    private static final String NAME="name";
    private static final String SONGS="songs";
    private static final String HISTORY_TABLE="history";

    private static final String DEFAULT="CREATE TABLE IF NOT EXISTS "+DEFAULT_TABLE+"(" +
            ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
            NAME+" TEXT NOT NULL UNIQUE," +
            SONGS+" TEXT DEFAULT ''" +
            ")";

    private static final String HISTORY="CREATE TABLE IF NOT EXISTS "+HISTORY_TABLE+"(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "strings TEXT NOT NULL UNIQUE" +
            ")";


    public static Database getDefault(){

            if (database == null) {
                synchronized (Database.class) {
                database = new Database(OverApplication.getContext(), MUSICDATABASE, null, VERSION);
            }
        }

        return database;
    }


    private Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        sqLiteDatabase=getReadableDatabase();
//        sqLiteDatabase.execSQL(DEFAULT);
        sqLiteDatabase.execSQL(HISTORY);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

//        db.execSQL(HISTORY);//历史输入表
        db.execSQL(DEFAULT);

        String sql="select id from "+DEFAULT_TABLE;

        Cursor cursor=db.rawQuery(sql,null);

        if (cursor!=null){
            if (cursor.getCount()==0){

                String DEFAULT_LIST = "insert into " + DEFAULT_TABLE +
                        "(id,name,songs)" +
                        "values" +
                        "(1,'默认列表','')";
                db.execSQL(DEFAULT_LIST);
            }
            cursor.close();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //添加列表
    public int addList(String listName){

        int result=-1;
        String sql="insert into "+DEFAULT_TABLE+" ("+NAME+") values ('"+listName+"')";

        try {
            sqLiteDatabase.execSQL(sql);
            result=1;
        }catch (Exception e){
            e.printStackTrace();
            result=0;
            Log.d("waning!!--->>database","");
        }

        return result;
    }

    /**
     * 添加列表音乐
     * @param music 传入需要保存的music
     */
    public void addMusicToList(String name, Music music){

        try {

            String sql_getSongs="select "+SONGS+" from "+DEFAULT_TABLE+" where "+NAME+" = '"+name+"'";

            Cursor c=sqLiteDatabase.rawQuery(sql_getSongs,null);
            if (c!=null){
                if (c.moveToFirst()){
                    String songs=c.getString(c.getColumnIndex("songs"));
                    if (songs==null||songs.isEmpty()) {
                        songs = music.getId()+"";
                    }else {
                        songs = songs + ";" + music.getId();
                    }
                    String sql="update "+DEFAULT_TABLE+" set "+SONGS+"= '"+songs+"' where "+NAME+" = '"+name+"'";

                    sqLiteDatabase.execSQL(sql);
                }
                c.close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 删除列表音乐
     * @param playList 传入对象
     * @param music music对象
     * @param position 所在位置
     */
    public void deleteMusicFromList(PlayList playList,Music music,int position){
        try {
            String sql_getSongs="select "+SONGS+" from "+DEFAULT_TABLE+" where "+ID+"="+playList.getId();

            Cursor cursor=sqLiteDatabase.rawQuery(sql_getSongs,null);
            if (cursor!=null){
                if (cursor.moveToFirst()){

                    String songs=cursor.getString(cursor.getColumnIndex("songs"));

                    String[] strings=songs.split(";");

                    String s=strings[position];

//                    Log.d("sizeee--->>",strings.length+"");
//                    Log.d("position",position+"");
//                    Log.d("s---->>",s);
//                    Log.d("music--->>",music.getId()+"");

                    String id= String.valueOf(music.getId());

                    if (s.equals(id)){
                        strings[position]="";
                    }

                    String song="";

                    for (String s1:strings){

//                        Log.d("s1---->>",s1);

                        if (!s1.equals("")){
                            if (song.equals("")){

                                song=s1;
                            }else {
                                song=song+";"+s1;
                            }

                        }
                    }

                    String sql="update "+DEFAULT_TABLE+" set "+SONGS+" = '"+song+"' where "+ID+" = "+playList.getId();
                    sqLiteDatabase.execSQL(sql);
                }

                cursor.close();
            }


        }catch (Exception e){
            e.printStackTrace();

        }


    }


    /**
     * 删除列表
     * @param playList 传入对象
     */
    public int deleteList(PlayList playList){

        int result=0;

        try {
            String sql="delete from "+DEFAULT_TABLE+" where id = "+playList.getId();
            sqLiteDatabase.execSQL(sql);
            result=1;
        }catch (Exception e){
            e.printStackTrace();
            result=0;
        }

        return result;
    }

    /**
     * 获取所有列表
     * @return 返回列表
     */
    public List<PlayList> getAllPlayList(){
        List<PlayList> playLists=new ArrayList<>();

        String GET_ALL_LIST="select * from "+DEFAULT_TABLE;

        try {

            Cursor cursor=sqLiteDatabase.rawQuery(GET_ALL_LIST,null);

            if (cursor!=null){
                if (cursor.moveToFirst()){
                    do {
                        PlayList playList=new PlayList();
                        int id=cursor.getInt(cursor.getColumnIndex("id"));
                        String name=cursor.getString(cursor.getColumnIndex("name"));
                        String songs=cursor.getString(cursor.getColumnIndex("songs"));
                        playList.setId(id);
                        playList.setName(name);
                        playList.setSongs(songs);

                        playLists.add(playList);

                    }while (cursor.moveToNext());
                }

                cursor.close();
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return playLists;
    }

    /**
     * 获取所有列表名称
     * 此方法暂时作废
     * @return 返回列表
     */
    public List<String> getAllListName(){

        try {
            List<String> strings=new ArrayList<>();

            String sql="select name from "+DEFAULT_TABLE;

            Cursor cursor=sqLiteDatabase.rawQuery(sql,null);
            if (cursor!=null){
                if (cursor.moveToFirst()){
                    do {
                        String name=cursor.getString(cursor.getColumnIndex("name"));
                        strings.add(name);
                    }while (cursor.moveToNext());
                }
                cursor.close();
            }

            return strings;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    //列表重命名
    public int renameList(PlayList playList,String newName){
        int result=0;

        try {
            String sql="update "+DEFAULT_TABLE+" set "+NAME+" = '"+newName+"' where "+ID+" = "+playList.getId();
            sqLiteDatabase.execSQL(sql);
            result=1;
        }catch (Exception e){
            e.printStackTrace();
            result=-1;
        }

        return result;

    }

    //添加历史查询记录
    public void addHistory(String string){

        try {
            String sql="insert into "+HISTORY_TABLE+"(strings) values ('"+string+"')";
            sqLiteDatabase.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //查询所有历史输入记录
    public List<String> getHistory(){
        List<String> stringList=new ArrayList<>();
        try {


            String sql="select * from "+HISTORY_TABLE;

            Cursor cursor=sqLiteDatabase.rawQuery(sql,null);

            if (cursor!=null){
                if (cursor.moveToFirst()){
                    do {
                        String item=cursor.getString(cursor.getColumnIndex("strings"));
                        stringList.add(item);
                    }while (cursor.moveToNext());
                }

                cursor.close();
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return stringList;

    }

    //删除一条历史记录
    public void deleteHistory(String history){
        try {
            String sql="delete from "+HISTORY_TABLE+" where strings = '"+history+"'";
            sqLiteDatabase.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //清除历史记录
    public void deleteAllHistory(){

        try {
            String sql="delete from "+HISTORY_TABLE;
            sqLiteDatabase.execSQL(sql);
        }catch (Exception e){
            e.printStackTrace();
        }

    }



}

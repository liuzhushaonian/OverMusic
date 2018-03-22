package com.app.legend.overmusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Music;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * Created by legend on 2017/9/13.
 */

public class ImageLoader {

    private LruCache<String,Bitmap> lruCache;

    private static volatile ImageLoader imageLoader;

    private static String CACHE_PATH= "";//文件缓存

    static Context context;

    public static final int SMALL=1000;
    public static final int ALBUM=2000;
    public static final int BIG=3000;
    public static final int ALBUMINFO=4000;
    private static final int CPU_COUNT=Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE=CPU_COUNT+1;
    private static final int MAXNUM_POOL_SIZE=CPU_COUNT*2+1;
    private static final long KEEP_ALIVE=10L;

    private static final ThreadFactory mThreadFactory=new ThreadFactory() {
        private final AtomicInteger count=new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable,"ImageLoader#"+count.getAndIncrement());
        }
    };

    private static final Executor ThreadPool=new ThreadPoolExecutor(
            CORE_POOL_SIZE,MAXNUM_POOL_SIZE,KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),mThreadFactory);


    private ImageLoader() {
        int maxMemory= (int) (Runtime.getRuntime().maxMemory()/1024);
        int cacheSize=maxMemory/8;

        lruCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getRowBytes()*bitmap.getHeight()/1024;
            }
        };
    }


    //单例模式
    public static ImageLoader getImageLoader(Context con){
        if (imageLoader==null){

            synchronized (ImageLoader.class) {
                imageLoader = new ImageLoader();
                context = con;
                CACHE_PATH = context.getFilesDir().getAbsolutePath();
            }
        }

        return imageLoader;
    }


    public void setLocalImage(String url,ImageView imageView,int width,int height,int type){
        bindLocalImage(url,imageView,width,height,type);
    }

    //清除缓存
    public void clean(){
        lruCache.evictAll();

        File file=new File(CACHE_PATH);

        if (file!=null&&file.exists()&&file.isDirectory()){

            for (File file1:file.listFiles()){
                file1.delete();
            }
        }
    }


    // 取Bitmap
    private Bitmap getBitmapFromLocal(String url,int width,int height,int type){

//        BitmapFactory.Options options=new BitmapFactory.Options();
//
//        options.inPreferredConfig= Bitmap.Config.RGB_565;
//
//        options.inJustDecodeBounds=true;
//
//        Bitmap bitmap=BitmapFactory.decodeFile(url,options);
//
//        options.inSampleSize=reSize(options,width,height);
//
//        options.inJustDecodeBounds=false;
//
//        bitmap=BitmapFactory.decodeFile(url,options);
        Bitmap bitmap=BitmapFactory.decodeFile(url);

        if (bitmap!=null) {

            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);

            cacheBitmapToMemory(url, type, bitmap);
            cacheBitmpToDisk(url, type, bitmap);

        }

        return bitmap;

    }

    private void bindLocalImage(final String url, final ImageView imageView, final int width, final int height,int type){


        Observable
                .create((ObservableOnSubscribe<Bitmap>) e -> {

                    Bitmap bitmap=null;
                    imageView.setTag(url);

                    bitmap=getCache(url,type,width,height);

                    if (bitmap==null) {
                        bitmap = getBitmapFromLocal(url, width, height,type);
                    }

                    if (bitmap!=null){
                        cacheBitmpToDisk(url,type,bitmap);
                        cacheBitmapToMemory(url,type,bitmap);
                        e.onNext(bitmap);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    if (imageView.getTag().equals(url)) {
                        imageView.setImageBitmap(bitmap);
                    }
                });

    }

    //统一获取缓存
    private Bitmap getCache(String name,int type,int width,int height){

        String string=name+"music"+type;
        Bitmap bitmap=null;

        bitmap=getBitmapFromMemory(string);

        if (bitmap==null){

            bitmap=getBitmapFromDisk(string,width,height);
        }

        if (bitmap!=null) {
            //进行内存缓存
            cacheBitmapToMemory(name,type,bitmap);
        }

        return bitmap;

    }

    private void cacheBitmapToMemory(String name,int type,Bitmap bitmap){
        String string=name+"music"+type;

        cacheInMemory(bitmap,string);

    }

    private void cacheBitmpToDisk(String name,int type,Bitmap bitmap){
        String string=name+"music"+type;

        cacheImageInDisk(bitmap,string);

    }


    /**
     * 本地缓存
     * @param bitmap
     * @param url MD5加密命名
     */
    private void cacheImageInDisk(Bitmap bitmap,String url){
        String name=getMd5(url);

        try {


            File file=new File(CACHE_PATH,name);

            File parentFile=file.getParentFile();

            if (!parentFile.exists()){
                parentFile.mkdirs();
            }

            bitmap.compress(Bitmap.CompressFormat.WEBP,100,new FileOutputStream(file));

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }

    /**
     * 从本地读取缓存
     * @param url
     * @return
     */
    private Bitmap getBitmapFromDisk(String url,int reqWidth,int reqHeight){
        Bitmap bitmap=null;
        String name=getMd5(url);

        File file=new File(CACHE_PATH).getAbsoluteFile();

        String file_path=file+"/"+name;

        try {

//            BitmapFactory.Options options=new BitmapFactory.Options();
//
//
////        options.inSampleSize=2;
//            options.inPreferredConfig= Bitmap.Config.RGB_565;
//
//            options.inJustDecodeBounds=true;
//
//            bitmap=BitmapFactory.decodeFile(file_path,options);
//
//            options.inSampleSize=reSize(options,reqWidth,reqHeight);
//
//            options.inJustDecodeBounds=false;
            bitmap=BitmapFactory.decodeFile(file_path);

            if (bitmap!=null){
                bitmap=Bitmap.createScaledBitmap(bitmap,reqWidth,reqHeight,false);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

            return bitmap;


    }

    /**
     * 写入内存缓存
     * @param bitmap
     * @param url
     */
    private void cacheInMemory(Bitmap bitmap,String url){
        String name=getMd5(url);

        lruCache.put(name,bitmap);
    }

    /**
     * 读取内存缓存
     * @param url
     * @return
     */
    private Bitmap getBitmapFromMemory(String url){
        String name=getMd5(url);

        Bitmap bitmap=lruCache.get(name);

        return bitmap;
    }



    //md5加密改名
    private String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }



    private int reSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int size=1;

        int width=options.outWidth;

        int height=options.outHeight;



        if (height>reqHeight/2||width>reqWidth/2){
            int halfHeight=height/2;

            int halfWidth=width/2;

//            Log.d("res--->>",reqWidth+"");



            while ((halfHeight/size)>=reqHeight&&
                    (halfWidth/size)>=reqWidth){
                size =2*size;
//                Log.d("hll-->>",size+"");
            }
        }

        return size;
    }

    /**
     * 外部设置，弃用Rxjava版，因为无法设置默认图片
     * @param music
     * @param imageView
     * @param type
     * @param width
     * @param height
     */
    public void setAlbum(Music music, ImageView imageView, int type, int width, int height){

        Runnable runnable= () -> {
            String url=getAlbumArt(music.getAlbumId());

            Bitmap bitmap=getCache(url,type,width,height);//查找本地缓存

            if (bitmap==null){//查找不到本地图片缓存，查找网络图片缓存

                bitmap=getCache(music.getAlbumName(),type,width,height);


            }


            if (bitmap==null){//查找本地数据库获取
                bitmap=getBitmapFromLocal(url,width,height,type);
            }

            if (bitmap==null){//以上皆查找不到，开启网络查找
                String doing = "type=search&&search_type=10&s=" + music.getAlbumName();

                String json=InternetUtil.getUtil().getJson(doing);

                String net_url=JsonUtil.getJsonUtil().getAlbumPic(json);

                bitmap=getBitmapFromNet(music.getAlbumName(),net_url,width,height,type);
            }

            Result result=new Result(type,width,height,bitmap,imageView,music.getAlbumId());

            handler.obtainMessage(100,result).sendToTarget();
        };
        ThreadPool.execute(runnable);

    }

    /**
     * 设置专辑页面大图
     *
     * @param imageView
     * @param type
     * @param width
     * @param height
     */
    public void setAlbumInfoBook(Album album, ImageView imageView, int type, int width, int height){


        Runnable runnable=()->{

            String url=getAlbumArt(album.getId());

            Bitmap bitmap=getCache(url,type,width,height);

            if (bitmap==null){

                bitmap=getCache(album.getAlbum_name(),type,width,height);
            }

            if (bitmap==null){
                bitmap=getBitmapFromLocal(url,width,height,type);
            }

            if (bitmap==null){

                String doing = "type=search&search_type=10&s=" + album.getAlbum_name();//搜索专辑

                String json=InternetUtil.getUtil().getJson(doing);

                String net_url=JsonUtil.getJsonUtil().getAlbumPic(json);

                bitmap=getBitmapFromNet(album.getAlbum_name(),net_url,width,height,type);
            }

            Result result=new Result(type,width,height,bitmap,imageView,album.getId());

            handler.obtainMessage(200,result).sendToTarget();

        };

        ThreadPool.execute(runnable);

    }

    /**
     * 设置歌手图片
     * @param name
     * @param imageView
     * @param w
     * @param h
     * @param type
     */
    public void setArtistPic(String name,ImageView imageView,int w,int h,int type){

        Runnable runnable=()->{

//            String url=getAlbumArt(album.getId());

            Bitmap bitmap=getCache(name,type,w,h);//找本地缓存


            if (bitmap==null){//开网络查找

                String doing = "type=search&search_type=100&s=" + name;//搜索歌手

                String json=InternetUtil.getUtil().getJson(doing);

                String net_url=JsonUtil.getJsonUtil().getArtistPic(json);

                Log.d("net_url--->>",net_url);

                bitmap=getBitmapFromNet(name,net_url,w,h,type);
            }

            ArtistResult result=new ArtistResult(w,h,bitmap,imageView,name,type);

            handler.obtainMessage(300,result).sendToTarget();

        };

        ThreadPool.execute(runnable);


    }



    //提供外部直接获取封面图
    public Bitmap getBitmap(int id){
        String url=getAlbumArt(id);
        Bitmap bitmap=BitmapFactory.decodeFile(url);

        return bitmap;

    }

    public Bitmap getSizeBitmap(int id,int w,int h){

        String url=getAlbumArt(id);
        Bitmap bitmap=BitmapFactory.decodeFile(url);
        if (bitmap!=null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
        }
        return bitmap;

    }

    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int action=msg.what;

            switch (action){
                case 100:
                    Result result= (Result) msg.obj;
                    Bitmap bitmap=result.getBitmap();


                    if (bitmap!=null){
                        result.getImageView().setImageBitmap(bitmap);
                    }else {

                        ImageView imageView=result.getImageView();

                        imageView.setBackgroundColor(ColorUtil.getAlbumColor(result.getId()));

                        if (result.getType()==BIG){

                            imageView.setImageResource(R.drawable.ic_audiotrack_black_150dp);


                        }else {

                            imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp);

                        }
                    }

                    break;
                case 200:

                    Result result2= (Result) msg.obj;
                    Bitmap bitmap2=result2.getBitmap();

                    if (bitmap2!=null){
                        result2.getImageView().setImageBitmap(bitmap2);
                    }else {

                        ImageView imageView=result2.getImageView();
                        imageView.setBackgroundColor(ColorUtil.getAlbumColor(result2.getId()));

                        imageView.setImageResource(R.drawable.ic_audiotrack_black_100dp);
                    }

                    break;
                case 300:

                    ArtistResult artistResult= (ArtistResult) msg.obj;
                    Bitmap pic=artistResult.getBitmap();
                    ImageView imageView=artistResult.getImageView();

                    if (pic!=null){
                        imageView.setImageBitmap(pic);
                        imageView.setVisibility(View.VISIBLE);
                    }else {
                        imageView.setVisibility(View.GONE);
                    }

                    break;
            }

//            Result result= (Result) msg.obj;
//            Bitmap bitmap=result.getBitmap();
//
//            if (bitmap!=null){
//                result.getImageView().setImageBitmap(bitmap);
//            }else {
//
//                ImageView imageView=result.getImageView();
//
//                imageView.setImageResource(R.drawable.ic_audiotrack_black_24dp);
//            }
        }
    };

    private String getAlbumArt(int album_id){
        String mUriAlbums="content://media/external/audio/albums";
        String[] projection=new String[]{"album_art"};
        Cursor cursor=OverApplication.getContext().getContentResolver().query(
                Uri.parse(mUriAlbums+"/"+Integer.toString(album_id)),projection,null,null,null);
        String albums_art=null;
        if (cursor.getCount()>0&&cursor.getColumnCount()>0){
            cursor.moveToNext();
            albums_art=cursor.getString(0);
        }
        cursor.close();
        cursor=null;

        return albums_art;
    }

    static class Result{
        int type;
        int width,height;
        Bitmap bitmap;
        ImageView imageView;
        int id;

        public Result(int type, int width, int height, Bitmap bitmap, ImageView imageView,int id) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.bitmap = bitmap;
            this.imageView = imageView;
            this.id=id;
        }

        public int getType() {
            return type;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public int getId() {
            return id;
        }

    }


    static class ArtistResult{

        private int w;
        private int h;
        private Bitmap bitmap;
        private String name;
        private int type;
        private ImageView imageView;

        public ImageView getImageView() {
            return imageView;
        }

        public ArtistResult(int w, int h, Bitmap bitmap, ImageView imageView, String name, int type) {
            this.w = w;
            this.h = h;
            this.bitmap = bitmap;
            this.name = name;
            this.type = type;
            this.imageView=imageView;

        }

        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }

    }


    /**
     * 从网络获取图片
     *
     * @return
     */
    private Bitmap getBitmapFromNet(String name,String url,int w,int h,int type){

        InputStream inputStream=null;

        Bitmap bitmap=null;

        try {

            Request.Builder builder1=new Request.Builder().url(url).method("GET",null);

            Request request=builder1.build();

            OkHttpClient.Builder builder=new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20,TimeUnit.SECONDS)
                    .readTimeout(20,TimeUnit.SECONDS);

            OkHttpClient okHttpClient=builder.build();

            Call call=okHttpClient.newCall(request);


            Response response=call.execute();

            inputStream = response.body().byteStream();

            BitmapFactory.Options options=new BitmapFactory.Options();

            options.inPreferredConfig= Bitmap.Config.ARGB_8888;

            options.inSampleSize=1;

            bitmap=BitmapFactory.decodeStream(inputStream,null,options);


        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitmap!=null){//进行缓存

            cacheBitmapToMemory(name,type,bitmap);
            cacheBitmpToDisk(name,type,bitmap);
            bitmap=Bitmap.createScaledBitmap(bitmap,w,h,false);
        }



        return bitmap;

    }

}

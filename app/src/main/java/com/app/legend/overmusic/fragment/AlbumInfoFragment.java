package com.app.legend.overmusic.fragment;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.MainActivity;
import com.app.legend.overmusic.adapter.MusicAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IAlbumMusicPresenter;
import com.app.legend.overmusic.interfaces.TranslucentListener;
import com.app.legend.overmusic.presenter.AlbumInfoPresenter;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.MyNestedScrollView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumInfoFragment extends BaseFragment implements IAlbumMusicPresenter,TranslucentListener{

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    MusicAdapter adapter;
    AlbumInfoPresenter presenter;
    private Toolbar toolbar;
    private Album album;
    private ImageView album_book;
    private ImageView bg;
    private TextView album_name,album_info;
    private FrameLayout frameLayout;
    private MyNestedScrollView nestedScrollView;
    public static final String TAG="album";

    public AlbumInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_album_info, container, false);
        recyclerView=view.findViewById(R.id.album_info_recycler_view);
        toolbar=view.findViewById(R.id.album_info_toolbar);
//        ((MainActivity)getActivity()).addToolbar(toolbar);
        nestedScrollView=view.findViewById(R.id.nested_scroll_view);
//        album_book=view.findViewById(R.id.album_info_book);
        bg=view.findViewById(R.id.bg);
//        album_name=view.findViewById(R.id.album_info_name);
//        album_info=view.findViewById(R.id.album_info_artist);
        frameLayout=view.findViewById(R.id.frame_layout);

        linearLayoutManager=new LinearLayoutManager(getContext());
        adapter=new MusicAdapter(MusicAdapter.ALBUM_MUSIC);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        presenter=new AlbumInfoPresenter(this);
        getAlbum();
        getData();
        initToolbar();
        initBook();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        this.toolbar.setBackgroundColor(getThemeColor());
        toolbar.getBackground().setAlpha(16);
    }

    private void getAlbum(){
        Bundle bundle=getArguments();
        Album album= (Album) bundle.getSerializable(TAG);
        this.album=album;
    }

    private void initBook(){
        if (this.album!=null){

            reDraw();

            Bitmap bitmap=ImageLoader.getImageLoader(getContext()).getBitmap(album.getId());

            if (bitmap!=null) {


//                bitmap = ImageUtil.getBitmap(getContext(), bitmap, 25);
//
//                int d=getResources().getDimensionPixelSize(R.dimen.album_info_w);
//                ImageLoader.getImageLoader(getContext()).setAlbumInfoBook(album.getId(),album_book,ImageLoader.ALBUMINFO,d,d);
//                album_name.setText(album.getAlbum_name());
//                album_info.setText(album.getArtist());
//                Observable
//                        .create((ObservableOnSubscribe<Integer>) e -> {
//                            int color= ColorUtil.getColor(bitmap);
//                            e.onNext(color);
//                        })
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(integer -> {
//                            album_name.setTextColor(integer);
//                            album_name.setText(album.getAlbum_name());
//                        });

                bg.setImageBitmap(bitmap);

            }else {
                bg.setScaleType(ImageView.ScaleType.FIT_CENTER);

//                BitmapDrawable drawable= (BitmapDrawable) getResources().getDrawable(R.drawable.bg,getActivity().getTheme());
//
//                bitmap=drawable.getBitmap();
//                bitmap=ImageUtil.getBitmap(getContext(),bitmap,25);
//                bg.setImageBitmap(bitmap);


            }
        }

    }

    private void reDraw(){
        LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) frameLayout.getLayoutParams();
        int h= (int) (getResources().getDisplayMetrics().widthPixels*0.8);
        layoutParams.height=h;
        frameLayout.setLayoutParams(layoutParams);
        nestedScrollView.setTranslucentListener(this,h);
    }

    private void initToolbar(){

        toolbar.setPadding(0,getStatusBarHeight(),0,0);

        toolbar.setTitle(album.getAlbum_name());
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            ((MainActivity)getActivity()).removeFragment(this);
        });

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        toolbar.getBackground().setAlpha(16);
    }


    @Override
    public void setData(List<Music> list) {
        adapter.setData(list);
    }

    private void getData(){
        presenter.getData(album);
    }


    @Override
    public void onTranslucent(int alpha) {
        toolbar.getBackground().setAlpha(alpha);
    }
}

package com.app.legend.overmusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.activity.MainActivity;
import com.app.legend.overmusic.adapter.AlbumAdapter;
import com.app.legend.overmusic.adapter.AlbumMusicAdapter;
import com.app.legend.overmusic.adapter.MusicAdapter;
import com.app.legend.overmusic.bean.Album;
import com.app.legend.overmusic.bean.Artist;
import com.app.legend.overmusic.bean.Music;
import com.app.legend.overmusic.interfaces.IArtistMusicPresenter;
import com.app.legend.overmusic.interfaces.TranslucentListener;
import com.app.legend.overmusic.presenter.ArtistInfoPresenter;
import com.app.legend.overmusic.utils.AlbumItemSpace;
import com.app.legend.overmusic.utils.ImageLoader;
import com.app.legend.overmusic.utils.MyNestedScrollView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistInfoFragment extends BaseFragment implements IArtistMusicPresenter,TranslucentListener{

    private Artist artist;
    private RecyclerView albumList,musicList;
    private LinearLayoutManager linearLayoutManager;
    private GridLayoutManager gridLayoutManager;
    private AlbumAdapter albumMusicAdapter;
    private MusicAdapter musicAdapter;
    private TextView artistName;
    private Toolbar toolbar;
    private ArtistInfoPresenter presenter;
    private MyNestedScrollView nestedScrollView;
    public static final String TAG="artist";
    private ImageView pic;
    private FrameLayout frameLayout;


    public ArtistInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_artist_music, container, false);
        presenter=new ArtistInfoPresenter(this);
        getComponent(view);
        reDraw();
        getArtist();
        initToolbar();
        initList();
        getData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.toolbar.setBackgroundColor(getThemeColor());
        toolbar.getBackground().setAlpha(16);
    }

    private void getArtist(){
        Bundle bundle=getArguments();
        this.artist= (Artist) bundle.getSerializable(TAG);

        assert artist != null;
        artistName.setText(artist.getName());

        int w=getResources().getDisplayMetrics().widthPixels;
        int h= (int) (w*0.8);


        ImageLoader.getImageLoader(getContext())
                .setArtistPic(artist.getName(),pic,w,h,10002);

    }

    private void getComponent(View view){

        albumList=view.findViewById(R.id.artist_info_album_list);
        musicList=view.findViewById(R.id.artist_info_music_list);
        artistName=view.findViewById(R.id.artist_info_name);
        toolbar=view.findViewById(R.id.artist_info_toolbar);
        nestedScrollView=view.findViewById(R.id.artist_info_netes_scroll_view);
        pic=view.findViewById(R.id.artist_pic);
        frameLayout=view.findViewById(R.id.frame_top);

    }

    private void initToolbar(){

        if (this.artist!=null) {
            toolbar.setTitle(this.artist.getName());
        }

        toolbar.setPadding(0,getStatusBarHeight(),0,0);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            ((MainActivity)getActivity()).removeFragment(this);
//            Toast.makeText(getContext(),"click",Toast.LENGTH_SHORT).show();
        });

        int h=getResources().getDimensionPixelSize(R.dimen.album_info_d);

        nestedScrollView.setTranslucentListener(this,h);

        toolbar.getBackground().setAlpha(16);


//        ((MainActivity)getActivity()).addToolbar(toolbar);

    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(getContext());
        gridLayoutManager=new GridLayoutManager(getContext(),1);
        gridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        albumMusicAdapter=new AlbumAdapter();
        albumList.setAdapter(albumMusicAdapter);
        albumList.setLayoutManager(gridLayoutManager);
        albumList.addItemDecoration(new AlbumItemSpace(AlbumItemSpace.INFO));

        musicAdapter=new MusicAdapter(MusicAdapter.ARTIST_MUSIC);
        musicList.setAdapter(musicAdapter);
        musicList.setLayoutManager(linearLayoutManager);
        musicList.setNestedScrollingEnabled(false);
    }

    private void getData(){
        //获取数据
        presenter.getAlbumData(this.artist);
        presenter.getMusicData(this.artist);
    }

    @Override
    public void setAlbumData(List<Album> albums) {
        albumMusicAdapter.setAlbums(albums);
    }

    @Override
    public void setMusicData(List<Music> musicList) {

//        Log.d("music--size--->>>>",musicList.size()+"");
        musicAdapter.setData(musicList);
    }

    @Override
    public void onTranslucent(int alpha) {
        toolbar.getBackground().setAlpha(alpha);
    }


    /**
     * 重写frameLayout的高度
     */
    private void reDraw(){

        int h= (int) (getResources().getDisplayMetrics().widthPixels*0.8);

        LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) frameLayout.getLayoutParams();

        layoutParams.height=h;

        frameLayout.setLayoutParams(layoutParams);

    }
}

package com.app.legend.overmusic.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.PlayListAdapter;
import com.app.legend.overmusic.bean.PlayList;
import com.app.legend.overmusic.event.DeletePlayListEvent;
import com.app.legend.overmusic.event.RenamePlayListEvent;
import com.app.legend.overmusic.interfaces.IPlayListPresenter;
import com.app.legend.overmusic.presenter.PlayListPresenter;
import com.app.legend.overmusic.utils.RxBus;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayListFragment extends BaseFragment implements IPlayListPresenter{

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private PlayListAdapter adapter;
    private LinearLayout linearLayout;
    private TextView textView;
    private PlayListPresenter presenter;
    private Disposable delete_dis,list_dis;


    public PlayListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_play_list, container, false);

        getComponent(view);
        initList();
        presenter=new PlayListPresenter(this);
        getData();
        register();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregister(delete_dis);
        unregister(list_dis);
    }

    private void getComponent(View view){

        recyclerView=view.findViewById(R.id.play_list_recycler_view);
        linearLayout=view.findViewById(R.id.play_list_null_info);
        textView=view.findViewById(R.id.play_list_info);
    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(getContext());
        adapter=new PlayListAdapter();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    public void getData(){

//        if (getPermission) {
            presenter.getData();
            linearLayout.setVisibility(View.GONE);
//        }else {
//            textView.setText(getResources().getString(R.string.permission_info));
//            linearLayout.setVisibility(View.VISIBLE);
//        }
    }

    private void register(){
        delete_dis= RxBus.getDefault().tObservable(DeletePlayListEvent.class).subscribe(deletePlayListEvent -> {
           deleteList(deletePlayListEvent.getPlayList());
        });

        list_dis=RxBus.getDefault().tObservable(RenamePlayListEvent.class).subscribe(renamePlayListEvent -> {
            renameList(renamePlayListEvent.getPlayList());
        });

    }

    private void unregister(Disposable disposable){
        if (disposable!=null&&!disposable.isDisposed()){
            disposable.dispose();
        }

    }

    private void deleteList(PlayList playList){
        presenter.deleteList(getActivity(),playList);
    }

    private void renameList(PlayList playList){
        presenter.renameList(getActivity(),playList);
    }

    @Override
    public void setData(List<PlayList> playLists) {

//        Log.d("listsize----->>",playLists.size()+"");
        adapter.setPlayListList(playLists);
    }
}

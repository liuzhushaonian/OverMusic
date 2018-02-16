package com.app.legend.overmusic.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.event.QueryEvent;
import com.app.legend.overmusic.utils.Database;
import com.app.legend.overmusic.utils.OverApplication;
import com.app.legend.overmusic.utils.RxBus;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 *搜索历史adapter
 * Created by legend on 2018/2/14.
 */

public class SearchHistoryAdapter extends BaseAdapter<SearchHistoryAdapter.ViewHolder>{

    private List<String> historyList;

    public void setHistoryList(List<String> historyList) {
        this.historyList = historyList;
        if (!this.historyList.isEmpty()) {
            this.historyList.add(this.historyList.size(),"清除搜索记录");
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.history_list_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            int position=viewHolder.getAdapterPosition();
            String data=historyList.get(position);
            query(data);

        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (historyList!=null){
            String history=historyList.get(position);
            holder.textView.setText(history);
            if (history.equals("清除搜索记录")){
                holder.imageView.setVisibility(View.GONE);
                holder.textView.setGravity(Gravity.CENTER);
            }else {
                holder.imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (historyList!=null){
            return historyList.size();
        }
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {

        View view;
        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            view=itemView;
            textView=itemView.findViewById(R.id.history_item);
            imageView=itemView.findViewById(R.id.item_icon);
        }
    }

    private void query(String data){
        if (data.equals("清除搜索记录")){
            clear();
        }else {
            RxBus.getDefault().post(new QueryEvent(data));
        }
    }

    private void clear(){

        Observable
                .create((ObservableOnSubscribe<Integer>) e -> {
                    Database.getDefault().deleteAllHistory();
                    e.onNext(1);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    this.historyList.clear();
                    notifyDataSetChanged();
                });


    }
}

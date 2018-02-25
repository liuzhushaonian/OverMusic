package com.app.legend.overmusic.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.app.legend.overmusic.R;
import com.app.legend.overmusic.adapter.ColorAdapter;
import com.app.legend.overmusic.bean.Color;
import com.app.legend.overmusic.interfaces.IColorPresenter;
import com.app.legend.overmusic.presenter.ColorPresenter;
import com.app.legend.overmusic.utils.RippleView;

import java.util.List;

public class ColorActivity extends BaseActivity implements IColorPresenter{


    private ColorPresenter presenter;
    private RecyclerView recyclerView;
    private ColorAdapter colorAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;
    private RippleView rippleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        getComponent();
        initList();
        initToolbar();
        presenter=new ColorPresenter(this);
        getColorData();
        itemClick();
    }

    @Override
    protected void setThemeColor() {
        rippleView.setBackgroundColor(getThemeColor());
    }

    /**
     * 恢复状态
     */
    @Override
    protected void onResume() {
        super.onResume();
        setThemeColor();
    }

    private void getComponent(){

        recyclerView=findViewById(R.id.color_list);
        rippleView=findViewById(R.id.color_bg);
        toolbar=findViewById(R.id.color_toolbar);
    }

    private void initList(){
        linearLayoutManager=new LinearLayoutManager(this);
        colorAdapter=new ColorAdapter();
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(colorAdapter);
    }

    private void initToolbar(){

        toolbar.setTitle("选择颜色");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);

        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });


    }


    private void getColorData(){
        presenter.getColorData();
    }

    @Override
    public void setData(List<Color> colors) {
        colorAdapter.setColorList(colors);
    }

    private void itemClick(){

        colorAdapter.setListener((v, color) -> {
            int[] position=new int[2];

            v.getLocationOnScreen(position);

            int x=position[0];
            int y=position[1];

            int w=getResources().getDisplayMetrics().widthPixels;

            double d=y*y+w*w;

            int limit= (int) Math.sqrt(d);

            rippleView.startRipper(x,y,color.getColor(),limit);

            saveThemeColor(color.getColor());//保存颜色
        });
    }




}

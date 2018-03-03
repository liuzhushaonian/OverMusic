package com.app.legend.overmusic.fragment;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.app.legend.overmusic.R;

import java.lang.reflect.Field;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


    protected SharedPreferences sharedPreferences;

    public BaseFragment() {
        // Required empty public constructor
    }



    protected int getStatusBarHeight(){

        try {
            Class<?> c=Class.forName("com.android.internal.R$dimen");
            Object object=c.newInstance();
            Field field=c.getField("status_bar_height");
            int x=Integer.parseInt(field.get(object).toString());
            return getResources().getDimensionPixelSize(x);

        }catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }

    protected int getThemeColor(){
        int defaultValue=getResources().getColor(R.color.colorBlueGrey);
        return getActivity().getSharedPreferences("over_music_shared", Context.MODE_PRIVATE).getInt("color",defaultValue);
    }

}

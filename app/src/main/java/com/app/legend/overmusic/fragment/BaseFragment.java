package com.app.legend.overmusic.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import java.lang.reflect.Field;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


    private static final String[] permissionStrings=
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    protected boolean getPermission=false;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission();
    }

    protected void getPermission(){
        if (ContextCompat.checkSelfPermission(getContext(), permissionStrings[0])!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{permissionStrings[0]},1000);
        }else {
            getPermission=true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1000:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    getPermission=true;
                }else {
                    getPermission=false;
                    Toast.makeText(getContext(),"无法获取权限，请授予相应权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
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

}

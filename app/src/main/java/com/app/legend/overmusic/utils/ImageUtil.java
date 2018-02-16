package com.app.legend.overmusic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 *
 * Created by legend on 2017/10/9.
 */

public class ImageUtil {

    public static Bitmap getBitmap(Context context, Bitmap source, int radius){
        Bitmap bitmap=source;
        RenderScript renderScript=RenderScript.create(context);

        final Allocation input=Allocation.createFromBitmap(renderScript,bitmap);

        final Allocation output=Allocation.createTyped(renderScript,input.getType());

        ScriptIntrinsicBlur scriptIntrinsicBlur=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

        scriptIntrinsicBlur.setInput(input);

        scriptIntrinsicBlur.setRadius(radius);

        scriptIntrinsicBlur.forEach(output);

        output.copyTo(bitmap);

        renderScript.destroy();

        return bitmap;
    }
}

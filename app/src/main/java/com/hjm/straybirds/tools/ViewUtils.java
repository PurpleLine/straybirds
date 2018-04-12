package com.hjm.straybirds.tools;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.hjm.straybirds.MyApplication;

/**
 * Created by hejunming on 2018/3/22.
 */

public class ViewUtils {

    private static Context mContext = MyApplication.getmContext();

    public static int dip2px(int dipVal) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipVal,
                mContext.getResources().getDisplayMetrics());
    }

    public static int[] getWindowPxy() {
        DisplayMetrics displayMetrics = MyApplication.getmContext().getResources().getDisplayMetrics();
        int[] ret = new int[2];
        ret[0] = displayMetrics.widthPixels;
        ret[1] = displayMetrics.heightPixels;
        return ret;
    }
}

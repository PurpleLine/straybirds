package com.hjm.straybirds.tools;

import android.content.Context;
import android.support.annotation.IntRange;
import android.widget.Toast;

import com.hjm.straybirds.MyApplication;

/**
 * Created by hejunming on 2018/3/21.
 */

public final class ToastTool {
    private static Toast mToast;
    private static Context mContext;

    private ToastTool() {
        mContext = MyApplication.getmContext();
        if (mToast == null) {
            mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        }
    }

    public static void toShow(String cotent) {
        toShow(cotent, 0);
    }

    public synchronized static void toShow(String content, @IntRange(from = 0, to = 1) int time) {

        if (mToast == null) {
            new ToastTool();
        }

        mToast.setDuration(time == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        mToast.setText(content);
        mToast.show();
    }
}

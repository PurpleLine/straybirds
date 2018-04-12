package com.hjm.straybirds;

import android.app.Application;
import android.content.Context;

import com.hjm.straybirds.tools.LogTool;

/**
 * Created by hejunming on 2018/3/20.
 */

public class MyApplication extends Application {

    private static boolean isRun = false;

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        isRun = true;
        mContext = getApplicationContext();
        LogTool.log("Application", "onCreate", LogTool.VERBOSE);
    }



    public static boolean isRun() {
        return isRun;
    }

    public static Context getmContext() {
        return mContext;
    }

}

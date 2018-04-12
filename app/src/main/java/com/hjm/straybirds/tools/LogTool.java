package com.hjm.straybirds.tools;

import android.support.annotation.IntRange;
import android.util.Log;

/**
 * Created by hejunming on 2018/3/20.
 */

public final class LogTool {
    private static int mLevel = 0;
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NO_LOG = 6;


    public static void log(String tag, String msg, @IntRange(from = 1, to = 5) int level) {

        if (level >= mLevel) {
            switch (level) {
                case VERBOSE:
                    Log.v(tag, msg);
                    break;
                case DEBUG:
                    Log.d(tag, msg);
                    break;
                case INFO:
                    Log.i(tag, msg);
                    break;
                case WARN:
                    Log.w(tag, msg);
                    break;
                case ERROR:
                    Log.e(tag, msg);
                    break;
                default:
            }
        }
    }
}

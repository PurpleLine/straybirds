package com.hjm.straybirds.tools;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by hejunming on 2018/4/5.
 */

public class PermissionUtils {

    public static boolean checkPromission(Context context, String ... permission) {
        for (String s : permission) {
            if (ContextCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}

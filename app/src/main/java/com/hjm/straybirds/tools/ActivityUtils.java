package com.hjm.straybirds.tools;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by hejunming on 2018/4/5.
 */

public class ActivityUtils {

    public static void checkImplicitIntent(Activity activity, Intent intent, int requestCode) {
        if (intent != null) {
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, requestCode);
            }
        } else {
            ToastTool.toShow("操作失败");
        }
    }
}

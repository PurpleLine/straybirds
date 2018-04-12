package com.hjm.straybirds.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.hjm.straybirds.tools.LruCacheUtils;

/**
 * Created by hejunming on 2018/4/8.
 */

public class MyIntentService extends IntentService {

    public static final String EXTRA_KEY = "EXTRA_KEY";
    public static final String DELETE_IMG_NAMES = "DELETE_IMG_NAMES";

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String  key = intent.getStringExtra(EXTRA_KEY);
        if (key == null) {
            return;
        }
        switch (key) {
            case DELETE_IMG_NAMES: {
                String[] buf = intent.getStringArrayExtra(DELETE_IMG_NAMES);
                LruCacheUtils.getInstance().deleteBitmap(this, buf);
            }
            break;
            default:
        }
    }
}

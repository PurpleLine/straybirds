package com.hjm.straybirds.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.hjm.straybirds.MyApplication;

/**
 * Created by hejunming on 2018/4/6.
 */

public class LruCacheUtils {

    public static final int NORMAL_PICTURE = 0;
    public static final int BIG_PICTURE = 1;
    public static final int SMALL_PICTURE = 2;
    private static final String BIG_PREFIX = "BIG_";
    private static final String SMALL_PREFIX = "SMALL_";

    private static LruCacheUtils mLruCacheUtils;
    private LruCache<String, Bitmap> mCache = null;

    private LruCacheUtils(Context context) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        mCache = new LruCache<String, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public static LruCacheUtils getInstance() {
        if (mLruCacheUtils == null) {
            synchronized (LruCacheUtils.class) {
                if (mLruCacheUtils == null) {
                    mLruCacheUtils = new LruCacheUtils(MyApplication.getmContext());
                }
            }
        }
        return mLruCacheUtils;
    }

    private void addBitmapToCache(String key, Bitmap value) {
        if (key != null && getBitmapFromCache(key) == null
                && value != null && !value.isRecycled()) {
            mCache.put(key, value);
        } else {
            LogTool.log("LruCacheUtils", "addBitmapToCache fail" + key, LogTool.ERROR);
        }
    }

    private Bitmap getBitmapFromCache(String key) {
        if (key != null) {
            Bitmap bm = mCache.get(key);
            if(bm != null && !bm.isRecycled()) {
                return bm;
            }
        }
        LogTool.log("LruCacheUtils", "getBitmapFromCache fail" + key, LogTool.ERROR);
        return null;
    }

    public void deleteBitmap(final Context context, final String... keys) {
        for (String key : keys) {
            //先移除存储卡里面的
            PictureUtils.deletePicture(context, key);
            //再移除缓存里面的 NORMAL
            if (getBitmapFromCache(key) != null) {
                mCache.remove(key);
            }
            //BIG
            if (getBitmapFromCache(BIG_PREFIX + key) != null) {
                mCache.remove(key);
            }
            //SMALL
            if (getBitmapFromCache(SMALL_PREFIX + key) != null) {
                mCache.remove(key);
            }
        }
    }

    public Bitmap loadBitmap(final Context context, final String _key, final int picture_mode) {
        final String key;
        if (picture_mode == BIG_PICTURE) {
            key = BIG_PREFIX + _key;
        } else if (picture_mode == SMALL_PICTURE) {
            key = SMALL_PREFIX + _key;
        } else {
            key = _key;
        }

        Bitmap bm = getBitmapFromCache(key);
        if (bm != null) {
            return bm;
        } else {
            final String path = PictureUtils.getPathViaName(context, _key);
            final int[] pxy = ViewUtils.getWindowPxy();
            if (picture_mode == BIG_PICTURE) {
                //同步从存储卡加载
                bm = PictureUtils.getCustomBitmap(path, pxy[0], pxy[1], false, true);
            } else if (picture_mode == NORMAL_PICTURE) {
                //同步从存储卡加载
                bm = PictureUtils.getCustomBitmap(path, ViewUtils.dip2px(300), ViewUtils.dip2px(200), true, true);
            } else {
                //小图要批量异步加载,如果缓存有则直接提供,若没有不在此处读取存储卡加载
                return null;
            }
            if (bm != null) {
                addBitmapToCache(key, bm);
                return bm;
            } else {
                return null;
            }
        }
    }


    public boolean loadSmallPicToCache(final Context context, final String key) {
        final String path = PictureUtils.getPathViaName(context, key);
        Bitmap bm = getSmallBitmapFromCache(key);
        if (bm == null) {
            bm = PictureUtils.getCustomBitmap(path, ViewUtils.dip2px(80), ViewUtils.dip2px(80), true, true);
            if (bm != null) {
                addBitmapToCache(SMALL_PREFIX + key, bm);
                return true;
            }
        }
        return false;
        //异步加载到缓存后,通知主线程从缓存同步去获取
    }

    public Bitmap getSmallBitmapFromCache(String name) {
        return getBitmapFromCache(SMALL_PREFIX + name);
    }
}

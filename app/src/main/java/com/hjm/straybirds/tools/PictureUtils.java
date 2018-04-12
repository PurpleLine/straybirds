package com.hjm.straybirds.tools;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;

import com.hjm.straybirds.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by hejunming on 2018/3/24.
 */

public class PictureUtils {

    private static final String TAG = "PictureUtils";

    public static final int CHOOSE_PICTURE_FROM_ALBUM = 1;
    public static final int CHOOSE_PICTURE_FROM_CAMERA = 2;
    public static final int PHOTO_CROP = 3;
    public static final int FILE_ONLY_READ = 0;
    public static final int FILE_TO_COVER = 1;

    public static final String ICON_NAME = "icon.jpg";
    public static final String CAMERA_JPG = "camera.jpg";

    private static String mGeneratePicName;

    public static void showChooseDialog(Activity activity, String name, boolean cameraEnable) {
        //弹出dialog让用户选择从相册或拍照来生成图像
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.activity_login_dialog_title);
        dialog.setItems(R.array.activity_login_dialog_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        //从相册获取
                        ActivityUtils.checkImplicitIntent(activity, PictureUtils.setChoosePictureFromAlbum(), PictureUtils.CHOOSE_PICTURE_FROM_ALBUM);
                    }
                    break;
                    case 1: {
                        //拍照获取
                        if (cameraEnable) {
                            ActivityUtils.checkImplicitIntent(activity, PictureUtils.setChoosePictureFromCamera(activity, getDefaultDiaryPicturePath(activity), name), PictureUtils.CHOOSE_PICTURE_FROM_CAMERA);
                        } else {
                            ToastTool.toShow("无相机权限");
                        }
                    }
                    break;
                    default:
                }
            }
        });
        dialog.show();
    }

    public static String getDefaultDiaryPicturePath(Context context) {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator
                + context.getPackageName() + File.separator + "PICTURE";
        //创建路径
        File filePath = new File(path);
        if (!filePath.exists()) {
            if (!filePath.mkdirs()) {
                LogTool.log(TAG, "mkdir() error!", LogTool.ERROR);
                return null;
            }
        }
        return filePath.getPath();
    }

    public static void copyPictureFromUri(Context context, Uri from, Uri to) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            InputStream inputStream;
            FileOutputStream outputStream;
            File dst = new File(to.getPath());

            try {
                //不支持mark
                inputStream = context.getContentResolver().openInputStream(from);
                outputStream = new FileOutputStream(dst);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                //使用完后及时关闭
                inputStream.close();
                int width = options.outWidth;
                int height = options.outHeight;
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int x = metrics.widthPixels;
                int y = metrics.heightPixels;
                if (width > x || height > y) {
                    options.inSampleSize = Math.max(width / x, height / y);
                }
                options.inJustDecodeBounds = false;
                //decode之后,流的位置已经改变,若支持mark,可以reset来复位,此处重新打开一次不是很合理,需要修改
                inputStream = context.getContentResolver().openInputStream(from);
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();

                outputStream.close();
                inputStream.close();
                bitmap.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    //打开相册
    public static Intent setChoosePictureFromAlbum() {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    //打开相机,保存到指定路径
    public static Intent setChoosePictureFromCamera(Context context, String path, String fileName) {
        Uri uri;
        if ((uri = getFileUri(context, path, fileName, FILE_TO_COVER)) != null) {
            //打开相机,并传入图像返回的uri(不传入则返回缩略图)
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1024);
            return intent;
        } else {
            LogTool.log(TAG, "getFileUri() fail", LogTool.ERROR);
            return null;
        }

    }

    //按照默认配置裁剪指定路径的图片输出到指定的Uri
    public static Intent cutForPhoto(Context context, String path, String fileName, Uri inUri) {

        Uri outPut;
        if ((outPut = getFileUri(context, path, fileName, FILE_TO_COVER)) != null) {
            return configIntentforCut(inUri, outPut);
        }
        LogTool.log(TAG, "getFileUri() fail", LogTool.ERROR);
        return null;
    }

    public static Uri getFileUri(Context context, String path, String filename, @IntRange(from = 0, to = 1) int mode) {
        //检查存储是否挂载
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //创建指向文件的索引
            File file = new File(path, filename);
            //覆盖模式
            if (mode == FILE_TO_COVER) {
                //文件存在则删除
                if (file.exists()) {
                    if (!file.delete()) {
                        LogTool.log(TAG, "file.delete() fail", LogTool.ERROR);
                        return null;
                    }
                }
                try {
                    if (!file.createNewFile()) {
                        LogTool.log(TAG, "file.creatNewFile() fail", LogTool.ERROR);
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else { //只读模式
                if (!file.exists()) {
                    LogTool.log(TAG, "file not exist", LogTool.ERROR);
                    return null;
                }
            }

            //创建文件的uri
            Uri uri;
            //7.0以上的版本不能直接从文件获取Uri,需要使用FileProvider来获取(注意到清单文件中注册provider)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.getPackageName(), file);
            } else {
                uri = Uri.fromFile(file);
            }
            return uri;
        }
        LogTool.log(TAG, "media not mount", LogTool.ERROR);
        return null;
    }

    private static Intent configIntentforCut(Uri input, Uri outPut) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", true);
        // aspectX,aspectY 是宽高的比例，这里设置正方形
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //设置要裁剪的宽高
        intent.putExtra("outputX", ViewUtils.dip2px(200));
        intent.putExtra("outputY", ViewUtils.dip2px(200));
        intent.putExtra("scale", true);
        //如果图片过大，会导致oom，这里设置为false
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        //压缩图片
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.setDataAndType(input, "image/*");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPut);
        return intent;
    }

    public static String generatePicName(String name) {
        mGeneratePicName = name + "_" + String.valueOf(new Date().getTime()) + ".jpg";
        return mGeneratePicName;
    }

    public static String getPicName() {
        return mGeneratePicName;
    }


    public static Bitmap getDeftLowBitmap(Context context, String picName, boolean isScale) {
        Uri uri = getFileUri(context, getDefaultDiaryPicturePath(context), picName, FILE_ONLY_READ);
        InputStream ins = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            ins = context.getContentResolver().openInputStream(uri);
            options.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeStream(ins, null, options);
            ins.close();
            float width = options.outWidth;
            float height = options.outHeight;
            DisplayMetrics metrics = new DisplayMetrics();
            int x = metrics.widthPixels;
            int y = metrics.heightPixels;
            int scale = 1;
            if (width > x || height > y) {
                scale = Math.max(Math.round(width / x), Math.round(height / y));
            }
            if (isScale) {
                scale = scale * 2;
            }
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            ins = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(ins, null, options);
            ins.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

    public static Bitmap getCustomBitmap(final String path, final int width, final int height, final boolean crop, final boolean rgb_565) {
        return getCustomBitmap(path, -1, -1, width, height, crop, rgb_565);
    }

    public static Bitmap getCustomBitmap(final String path, final int decodeWidth, final int decodeheight, final int width, final int height, final boolean crop, final boolean rgb_565) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bm = null;

        if (decodeheight == -1 || decodeWidth == -1) {
            bm = BitmapFactory.decodeFile(path, options);
            if (bm != null) {
                bm.recycle();
                bm = null;
            }
        } else {
            options.outWidth = decodeWidth;
            options.outHeight = decodeheight;
        }

        final int beX = (int) Math.round(options.outWidth * 1.0 / width);
        final int beY = (int) Math.round(options.outHeight * 1.0 / height);
        options.inSampleSize = (crop ? (beX > beY ? beY : beX) : (beX > beY ? beX : beY));
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1;
        }

        while (options.outWidth * options.outHeight / options.inSampleSize > MAX_DECODE_PICTURE_SIZE) {
            options.inSampleSize++;
        }

        int newWidth = width;
        int newHeight = height;

        if (crop) {
            if (beY > beX) {
                newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
            } else {
                newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
            }
        } else {
            if (beY > beX) {
                newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
            } else {
                newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
            }
        }

        options.inJustDecodeBounds = false;
        if (rgb_565) {
            options.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        bm = BitmapFactory.decodeFile(path, options);
        options = null;
        if (bm == null) {
            return null;
        }

        Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        if (scale != null && scale != bm) {
            bm.recycle();
            bm = scale;
        }

        final int x = bm.getWidth() - width;
        final int y = bm.getHeight() - height;
        if (crop && x >= 0 && y >= 0) {
            Bitmap cropped = Bitmap.createBitmap(bm, x >> 2, y >> 2, width, height);
            if (cropped == null) {
                return bm;
            }

            if (cropped != bm) {
                bm.recycle();
                bm = cropped;
            }
        }
        return bm;
    }

    public static void compressPicture(String srcPath, String dstPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bm = BitmapFactory.decodeFile(srcPath, options);
        if (bm != null) {
            bm.recycle();
            bm = null;
        }

        final int[] pxy = ViewUtils.getWindowPxy();
        if (options.outHeight * options. outWidth > MAX_DECODE_PICTURE_SIZE) {
            bm = getCustomBitmap(srcPath, options.outWidth, options.outHeight, pxy[0], pxy[1], true, false);
            if (bm == null) {
                return;
            }
        } else {
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(srcPath, options);
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dstPath);
            bm.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bm.recycle();
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void compressPicture(Context context, Uri src, Uri dst) {
        compressPicture(getRealFilePath(context, src), getRealFilePath(context, dst));
    }

    public static void compressPicture(Context context, String srcPath, Uri dst) {
        compressPicture(srcPath, getRealFilePath(context, dst));
    }

    public static void compressPicture(Context context, Uri src, String dstPath) {
        compressPicture(getRealFilePath(context, src), dstPath);
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    public static String getPathViaName(Context context, String name) {
        return getDefaultDiaryPicturePath(context) + File.separator + name;
    }

    public static Bitmap getDefaultDirBitmap(Context context, String name, int dpx, int dpy, boolean crop, boolean rgb_565) {
        String path = getDefaultDiaryPicturePath(context) + File.separator + name;
        int width = ViewUtils.dip2px(dpx);
        int height = ViewUtils.dip2px(dpy);
        return getCustomBitmap(path, width, height, crop, rgb_565);
    }

    public static void deletePicture(Context context, String name) {
        File file = new File(getPathViaName(context, name));

        if (file.exists()) {
            file.delete();
        }
        file = null;
    }

    private static String[] getAllPictureName(@NonNull String path) {
        File dir = new File(path);
        List<String> names = new ArrayList<>();

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    names.add(getFileName(file.toString()));
                }
            }
            return names.toArray(new String[names.size()]);
        } else {
            return null;
        }
    }

    public static String[] getDefPathAllPicture(Context context) {
        return getAllPictureName(getDefaultDiaryPicturePath(context));
    }
}

package com.hjm.straybirds.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.hjm.straybirds.service.MyIntentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by hejunming on 2018/3/27.
 */

public class DiaryManager {

    private static final int MAX_SIZE = 10;
    private static DiaryManager mManager;
    private MyDbHelper mDbHelper;
    private List<Diary> mDiaries;
    private List<Diary> mBakDiaries;

    private long mRecentRecord;
    private long mStartIndex;
    private long mEndIndex;

    private DiaryManager(Context context) {
        //若持有activity或service类型的context,当他们销毁时,如果继续持有对它们的引用,会导致占用资源无法被回收,
        // 而application类型的context生命周期是最长的,完全退出应用时才会销毁
        mDbHelper = new MyDbHelper(context.getApplicationContext());
        mDiaries = new ArrayList<>();
    }

    //DCL单例模式
    public static DiaryManager getInstance(Context context) {
        if (mManager == null) {
            synchronized (DiaryManager.class) {
                if (mManager == null) {
                    mManager = new DiaryManager(context);
                }
            }
        }
        return mManager;
    }

    public void setDiaries(List<Diary> list) {
        if (mBakDiaries == null) {
            mBakDiaries = new ArrayList<>();
            mBakDiaries.addAll(mDiaries);
        }
        mDiaries.clear();
        mDiaries.addAll(list);
    }

    public void restoreDiaries() {
        if (mBakDiaries != null) {
            mDiaries.clear();
            mDiaries.addAll(mBakDiaries);
            mBakDiaries.clear();
            mBakDiaries = null;
            Collections.sort(mDiaries);
            while (mDiaries.size() > MAX_SIZE) {
                mDiaries.remove(mDiaries.size() - 1);
            }
        }
    }

    public List<Diary> getDiaries() {
        return mDiaries;
    }

    public List<Diary> getBakDiaries() {
        return mBakDiaries;
    }





    private MyCursorWrapper queryDb(SQLiteDatabase db, String[] columns, String selection, String[] selectionArgs,
                                    String groupBy, String having, String orderBy, String limit) {
        return new MyCursorWrapper(db.query(MyDbHelper.DiaryDbSchema.NAME, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit));
    }

    public List<Diary> initialDiary() {
        if (mDiaries.isEmpty()) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            MyCursorWrapper cursor = queryDb(db, null, null, null, null,
                    null, MyDbHelper.DiaryDbSchema.Cols.DATE + " DESC", "10");
            if (cursor.moveToFirst()) {
                do {
                    mDiaries.add(cursor.getDiary());
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            if (!mDiaries.isEmpty()) {
                Collections.sort(mDiaries);
                mRecentRecord = mDiaries.get(0).getDate().getTime();
                mStartIndex = mRecentRecord;
                mEndIndex = mDiaries.get(mDiaries.size() - 1).getDate().getTime();
            } else {
                mRecentRecord = new Date().getTime();
                mStartIndex = mEndIndex = mRecentRecord;
            }
        }
        return mDiaries;
    }

    public boolean queryNewDiary() {
        boolean ret;
        List<Diary> buf = new ArrayList<>();
        final String selectionArgs1 = String.valueOf(mStartIndex);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        MyCursorWrapper cursor = queryDb(db,
                null,
                MyDbHelper.DiaryDbSchema.Cols.DATE + " >?",
                new String[] {selectionArgs1},
                null, null,
                MyDbHelper.DiaryDbSchema.Cols.DATE + " ASC",
                "10");
        if (cursor.moveToFirst()) {
            do {
                buf.add(cursor.getDiary());
            } while (cursor.moveToNext());
            //记录首部
            if (!buf.isEmpty()) {
                mStartIndex = buf.get(buf.size() - 1).getDate().getTime();
            }
            ret = true;
        } else {
            ret = false;
        }
        cursor.close();
        db.close();
        for(Diary diary : buf) {
            int i = 0;
            if (!mDiaries.contains(diary)) {
                mDiaries.add(diary);
                i++;
            }
            ret = i > 0;
        }
        buf.clear();
        Collections.sort(mDiaries);
        //记录尾部
        if (mDiaries.size() > MAX_SIZE) {
            //移除最老的
            while (mDiaries.size() > MAX_SIZE) {
                mDiaries.remove(mDiaries.size() - 1);
            }
            //尾部已经发生变化,所以需要更新尾部
            mEndIndex = mDiaries.get(mDiaries.size() - 1).getDate().getTime();
        }
        return ret;
    }

    public boolean queryOldDiary() {
        boolean ret;
        List<Diary> buf = new ArrayList<>();
        final String selectionArgs = String.valueOf(mEndIndex);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        MyCursorWrapper cursor = queryDb(db, null, MyDbHelper.DiaryDbSchema.Cols.DATE + " <?", new String[] {selectionArgs},
                null, null, MyDbHelper.DiaryDbSchema.Cols.DATE + " DESC", "10");
        if (cursor.moveToFirst()) {
            do {
                buf.add(cursor.getDiary());
            } while (cursor.moveToNext());
            //记录尾部
            if (!buf.isEmpty()) {
                mEndIndex = buf.get(buf.size() - 1).getDate().getTime();
            }
            ret = true;
        } else {
            ret = false;
        }
        cursor.close();
        db.close();
        for(Diary diary : buf) {
            int i = 0;
            if (!mDiaries.contains(diary)) {
                mDiaries.add(diary);
                i++;
            }
            ret = i > 0;
        }
        buf.clear();
        Collections.sort(mDiaries);
        //记录首部
        if (mDiaries.size() > MAX_SIZE) {
            //移除最新的
            while (mDiaries.size() > MAX_SIZE) {
                mDiaries.remove(0);
            }
            //首部已经发生变化,所以需要更新首部
            mStartIndex = mDiaries.get(0).getDate().getTime();
        }
        return  ret;
    }

    private void addDiary(Diary diary) {
        if (!mDiaries.contains(diary)) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.insert(MyDbHelper.DiaryDbSchema.NAME, null, getContentVal(diary));
            db.close();

            //简单点...添加的直接展示在顶端
            mDiaries.add(diary);
            if (mBakDiaries != null) {
                mBakDiaries.add(diary);
            }

            /*
            //判断链表首端的时间是不是最新的,如果不是则表明当前链表的内容不需要展示最新的
            long recentRecord;
            if (mBakDiaries != null && !mBakDiaries.isEmpty()) {
                recentRecord = mBakDiaries.get(0).getDate().getTime();
            } else if (!mDiaries.isEmpty()) {
                recentRecord = mDiaries.get(0).getDate().getTime();
            } else {
                recentRecord = mRecentRecord;
            }

            if (recentRecord >= mRecentRecord) {
                //此处不判断mBakDiaries的大小,在还原过去的时候再判断处理
                if (mBakDiaries != null) {
                    mBakDiaries.add(0, diary);
                } else {
                    mDiaries.add(0, diary);
                    if (mDiaries.size() > MAX_SIZE) {
                        mDiaries.remove(MAX_SIZE);
                    }
                }
            }
            //更新记录的最新时间
            mRecentRecord = diary.getDate().getTime();

            if (!mDiaries.isEmpty()) {
                mStartIndex = mDiaries.get(0).getDate().getTime();
                mEndIndex = mDiaries.get(mDiaries.size() - 1).getDate().getTime();
            }
            */
        }
    }

    public void deleteDiary(Context context, Diary diary) {
        if (mDiaries.contains(diary)) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.delete(MyDbHelper.DiaryDbSchema.NAME,
                    MyDbHelper.DiaryDbSchema.Cols.ID + "=? ",
                    new String[]{diary.getId()});
            db.close();

            /*
            //如果删除的是最新的,则需要后滚记录的最新时间
            if (mRecentRecord == diary.getDate().getTime()) {
                if(mBakDiaries != null && mBakDiaries.size() > 1) {
                    mRecentRecord = mBakDiaries.get(1).getDate().getTime();
                } else if (mDiaries.size() > 1) {
                    mRecentRecord = mDiaries.get(1).getDate().getTime();
                } else {
                    mRecentRecord = new Date().getTime();
                }
            }
            */

            mDiaries.remove(diary);
            if (mBakDiaries != null) {
                mBakDiaries.remove(diary);
            }
        }
        //后台删除图片
        Intent intent = new Intent(context, MyIntentService.class);
        intent.putExtra(MyIntentService.EXTRA_KEY, MyIntentService.DELETE_IMG_NAMES);
        intent.putExtra(MyIntentService.DELETE_IMG_NAMES, diary.getContentImgs());
        context.startService(intent);
    }

    private void updateDiary(Diary diary) {
        if (mDiaries.contains(diary)) {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.update(MyDbHelper.DiaryDbSchema.NAME,
                    getContentVal(diary),
                    MyDbHelper.DiaryDbSchema.Cols.ID + "=? ",
                    new String[]{diary.getId()});
            db.close();

            //mRecentRecord = diary.getDate().getTime();

            mDiaries.set(mDiaries.indexOf(diary), diary);
            if (mBakDiaries != null) {
                mBakDiaries.set(mBakDiaries.indexOf(diary), diary);
            }
        }
    }

    public void commitDiary(Diary diary) {
        if (mDiaries.contains(diary)) {
            updateDiary(diary);
        } else {
            addDiary(diary);
        }
    }

    private static class MyCursorWrapper extends CursorWrapper {

        private static boolean mFirstInitail = true;
        private static int mIndexId;
        private static int mIndexDate;
        private static int mIndexTitle;
        private static int mIndexContent;
        private static int mIndexMood;
        private static int mIndexCity;
        private static int mIndexWeather;

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */

        MyCursorWrapper(Cursor cursor) {
            super(cursor);

            if (mFirstInitail) {
                mIndexId = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.ID);
                mIndexDate = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.DATE);
                mIndexTitle = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.TITLE);
                mIndexContent = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.CONTENT);
                mIndexMood = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.MOOD);
                mIndexCity = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.CITY);
                mIndexWeather = cursor.getColumnIndex(MyDbHelper.DiaryDbSchema.Cols.WEATHER);
                mFirstInitail = false;
            }
        }

        Diary getDiary() {
            Diary diary = new Diary();
            diary.setId(getString(mIndexId));
            diary.setDate(new Date(getLong(mIndexDate)));
            diary.setTitle(getString(mIndexTitle));
            diary.setDiaryContents(getString(mIndexContent));
            diary.setMood(getInt(mIndexMood));
            diary.setCity(getString(mIndexCity));
            diary.setWeather(getString(mIndexWeather));
            return diary;
        }
    }

    private ContentValues getContentVal(Diary diary) {
        ContentValues values = new ContentValues();
        values.put(MyDbHelper.DiaryDbSchema.Cols.ID, diary.getId());
        values.put(MyDbHelper.DiaryDbSchema.Cols.DATE, diary.getDate().getTime());
        values.put(MyDbHelper.DiaryDbSchema.Cols.TITLE, diary.getTitle());
        values.put(MyDbHelper.DiaryDbSchema.Cols.CONTENT, diary.getDiaryContents());
        values.put(MyDbHelper.DiaryDbSchema.Cols.MOOD, diary.getMood());
        values.put(MyDbHelper.DiaryDbSchema.Cols.CITY, diary.getCity());
        values.put(MyDbHelper.DiaryDbSchema.Cols.WEATHER, diary.getWeather());

        return values;
    }

}

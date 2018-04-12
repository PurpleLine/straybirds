package com.hjm.straybirds.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hejunming on 2018/3/27.
 */

public class MyDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "diary";
    private static final int DB_VERSION = 1;

    public MyDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + DiaryDbSchema.NAME + "( "
                + "_id INTEGER primary key autoincrement,"
                + DiaryDbSchema.Cols.ID + " TEXT,"
                + DiaryDbSchema.Cols.DATE + " INTEGER,"
                + DiaryDbSchema.Cols.TITLE + " TEXT,"
                + DiaryDbSchema.Cols.CONTENT + " TEXT,"
                + DiaryDbSchema.Cols.MOOD + " INTEGER,"
                + DiaryDbSchema.Cols.CITY + " TEXT,"
                + DiaryDbSchema.Cols.WEATHER + " TEXT"
                + " )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static class DiaryDbSchema {

        public static final String NAME = "diary";

        public static class Cols {
            public static final String ID = "uuid";
            public static final String DATE = "date";
            public static final String TITLE = "title";
            public static final String CONTENT = "content";
            public static final String MOOD = "mood";
            public static final String CITY = "city";
            public static final String WEATHER = "weather";
        }
    }
}

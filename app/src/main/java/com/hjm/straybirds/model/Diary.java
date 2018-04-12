package com.hjm.straybirds.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by hejunming on 2018/3/27.
 */

public class Diary implements Comparable<Diary> {

    private static final String DIARYCONTENT_SPILT = ">&<!content_spilt!>&<";

    private String mId;
    private String mTitle;
    private int mMood;
    private String mWeather;
    private String mCity;
    private Date mDate;
    private List<DiaryContent> mContents;

    public Diary() {
        mId = UUID.randomUUID().toString();
        mTitle = "";
        mContents = new ArrayList<>();
        mContents.add(new DiaryContent());
        mMood = 0;
        mWeather = "未知";
        mCity = "未知";
        mDate = null;
    }

    public Diary(Diary diary) {
        mId = diary.getId();
        mTitle = diary.getTitle();
        mContents = new ArrayList<>();
        //不使用addall(),是因为该方式只是添加引用,而没有生成实例,该处是需要拷贝一份实例内容
        for (DiaryContent content: diary.getContents()) {
            DiaryContent temp = new DiaryContent(content);
            mContents.add(temp);
        }
        mMood = diary.getMood();
        mWeather = diary.getWeather();
        mCity = diary.getCity();
        if(diary.getDate() != null) {
            mDate = new Date(diary.getDate().getTime());
        } else {
            mDate = null;
        }
    }

    public boolean allContentEqualsTo(Diary diary) {
        return (this == diary)
                || (diary != null
                && mId.equals(diary.getId())
                && mTitle.equals(diary.getTitle())
                && mMood == diary.getMood()
                && mWeather.equals(diary.getWeather())
                && mCity.equals(diary.getCity())
                && mContents.equals(diary.getContents()));
    }

    public void clear() {
        mId = null;
        mTitle = null;
        mMood = 0;
        mWeather = null;
        mCity = null;
        mDate = null;
        mContents.clear();
        mContents = null;
    }

    public List<DiaryContent> getContents() {
        return mContents;
    }

    public String[] getContentTexts() {
        String[] strings = new String[mContents.size()];
        for (int i = 0; i < mContents.size(); i++) {
            strings[i] = mContents.get(i).getTextContent().toString();
        }
        return strings;
    }

    public String getContentText() {
        StringBuilder builder = new StringBuilder();
        for (String s : getContentTexts()) {
            builder.append(s);
        }

        return builder.toString();
    }

    public String[] getContentImgs() {
        List<String> list = new ArrayList<>();
        for (DiaryContent content : mContents) {
            String s = content.getImageName();
            if (s != null && !s.equals("null")) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public void setDiaryContents(String string) {
        mContents.clear();
        String[] buf = string.split(DIARYCONTENT_SPILT);
        for (String s : buf) {
            DiaryContent content = DiaryContent.obtianFromString(s);
            mContents.add(content);
        }
    }

    public void setDiaryContents(List<DiaryContent> diaryContents) {
        mContents = diaryContents;
    }

    public String getDiaryContents() {
        StringBuilder builder = new StringBuilder();
        for (DiaryContent content : mContents) {
            builder.append(content.toString());
            builder.append(DIARYCONTENT_SPILT);
        }
        if (builder.length() >= DIARYCONTENT_SPILT.length()) {
            builder.delete(builder.length() - DIARYCONTENT_SPILT.length(), builder.length());
        }
        return builder.toString();
    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getMood() {
        return mMood;
    }

    public void setMood(int mood) {
        mMood = mood;
    }

    public String getWeather() {
        return mWeather;
    }

    public void setWeather(String weather) {
        mWeather = weather;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof Diary)) {
            return mId.equals(((Diary) obj).getId());
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull Diary o) {
        if (mDate != null) {
            return -1 * Long.valueOf(mDate.getTime()).compareTo(o.getDate().getTime());
        }
        return 1;
    }

    public static class Mood {
        private static final int MOOD_COUNT = 9;
        public static final int NORMAL = 0;
        public static final int ANGRY = 1;
        public static final int BLUE = 2;
        public static final int HAPPY = 3;
        public static final int HATE = 4;
        public static final int LOVE = 5;
        public static final int SAD = 6;
        public static final int SECRET = 7;
        public static final int SURPRISE = 8;
    }

    //预定天气API获取,为字符串
//    public enum Weather {
//        Sunny, cloudy, Rainy, Snowy, Fog
//    }
}

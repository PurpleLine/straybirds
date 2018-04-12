package com.hjm.straybirds.model;

import android.text.TextUtils;

/**
 * Created by hejunming on 2018/3/31.
 */

public class DiaryContent {

    private static final String SPILT = "<&!<img_spilt>&!>";
    private String mImageName;
    private StringBuilder mTextContent;

    public DiaryContent() {
        mTextContent = new StringBuilder();
        mImageName = null;
    }

    public DiaryContent(DiaryContent content) {
        mTextContent = new StringBuilder(content.getTextContent().toString());
        mImageName = content.getImageName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof DiaryContent)) {
            return mTextContent.toString().equals(((DiaryContent) obj).getTextContent().toString())
                            && ((mImageName == null && TextUtils.isEmpty(((DiaryContent) obj).getImageName()))
                            || mImageName != null && mImageName.equals(((DiaryContent) obj).getImageName()));

        }
        return false;
    }

    public String getImageName() {
        return mImageName;
    }

    public void setImageName(String imageName) {
        mImageName = imageName;
    }

    public StringBuilder getTextContent() {
        return mTextContent;
    }

    public void setTextContent(StringBuilder textContent) {
        mTextContent = textContent;
    }

    @Override
    public String toString() {
        return mTextContent.toString() + SPILT + mImageName;
    }

    public static synchronized DiaryContent obtianFromString(String string) {
        DiaryContent diaryContent = new DiaryContent();
        if (string != null) {
            String[] strings = string.split(SPILT);
            diaryContent.getTextContent().append(strings[0]);
            if ("null".equals(strings[1])) {
                diaryContent.setImageName(null);
            } else {
                diaryContent.setImageName(strings[1]);
            }
        }
        return diaryContent;
    }
}

package com.hjm.straybirds.model;

/**
 * Created by hejunming on 2018/4/10.
 */

public class Picture {
    private boolean isInvalid;
    private String mName;
    private String mDiaryId;

    public Picture(String name) {
        mName = name;
        isInvalid = false;
        String[] buf = mName.split("_");
        if (buf.length > 1) {
            mDiaryId = buf[0];
        } else {
            mDiaryId = "null";
        }
    }

    public String getDiaryId() {
        return mDiaryId;
    }

    public void setDiaryId(String diaryId) {
        mDiaryId = diaryId;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    public void setInvalid(boolean invalid) {
        isInvalid = invalid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }
}

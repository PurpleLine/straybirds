package com.hjm.straybirds.model;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by hejunming on 2018/4/11.
 */

public class PictureLists extends BmobObject {
    private String name;
    private BmobFile content;
    private transient int count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BmobFile getContent() {
        return content;
    }

    public void setContent(BmobFile content) {
        this.content = content;
    }
}

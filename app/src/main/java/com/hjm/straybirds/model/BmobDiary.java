package com.hjm.straybirds.model;

import cn.bmob.v3.BmobObject;

/**
 * Created by hejunming on 2018/4/11.
 */

public class BmobDiary extends BmobObject{
    private String id;
    private String title;
    private String contents;
    private String mood;
    private String weather;
    private String city;
    private String date;

    public BmobDiary() {

    }

    public BmobDiary(String _id, String _title, String _contents, String _mood, String _weather, String _city, String _date) {
        id = _id;
        title = _title;
        contents =_contents;
        mood = _mood;
        weather = _weather;
        city = _city;
        date = _date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

package com.example.gek.pizza.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Model of tables
 */

public class News implements Parcelable {
    private String title;
    private String description;
    private String photoUrl;
    private String photoName;
    private String key;
    private long timeStamp;

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }


    public News() {
    }


    public News(String title, String description, String photoUrl, String photoName) {
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.photoName = photoName;

        // need for sort in RecyclerView
        this.timeStamp = -1 * new Date().getTime();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.photoUrl);
        dest.writeString(this.photoName);
        dest.writeString(this.key);
        dest.writeLong(this.timeStamp);
    }

    protected News(Parcel in) {
        this.title = in.readString();
        this.description = in.readString();
        this.photoUrl = in.readString();
        this.photoName = in.readString();
        this.key = in.readString();
        this.timeStamp = in.readLong();
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel source) {
            return new News(source);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
}
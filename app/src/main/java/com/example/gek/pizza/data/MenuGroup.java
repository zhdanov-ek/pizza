package com.example.gek.pizza.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model of MenuGroup
 */

public class MenuGroup implements Parcelable {
    private String name;
    private String photoUrl;
    private String photoName;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public MenuGroup(String name, String photoUrl, String photoName) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.photoName = photoName;
    }

    public MenuGroup() {
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.photoUrl);
        dest.writeString(this.photoName);
        dest.writeString(this.key);
    }

    protected MenuGroup(Parcel in) {
        this.name = in.readString();
        this.photoUrl = in.readString();
        this.photoName = in.readString();
        this.key = in.readString();
    }

    public static final Creator<MenuGroup> CREATOR = new Creator<MenuGroup>() {
        @Override
        public MenuGroup createFromParcel(Parcel source) {
            return new MenuGroup(source);
        }

        @Override
        public MenuGroup[] newArray(int size) {
            return new MenuGroup[size];
        }
    };
}

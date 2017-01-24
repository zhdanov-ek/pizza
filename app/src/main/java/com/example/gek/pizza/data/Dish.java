package com.example.gek.pizza.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Блюдо
 */

public class Dish implements Parcelable {
    private String name;
    private String description;
    private float price;
    private String photoUrl;
    private String photoName;
    private String keyGroup;
    private String key;

    public Dish() {
    }

    public Dish(String name, String description, float price, String photoUrl, String photoName, String keyGroup) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.photoUrl = photoUrl;
        this.photoName = photoName;
        this.keyGroup = keyGroup;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getKeyGroup() {
        return keyGroup;
    }

    public void setKeyGroup(String keyGroup) {
        this.keyGroup = keyGroup;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeFloat(this.price);
        dest.writeString(this.photoUrl);
        dest.writeString(this.photoName);
        dest.writeString(this.keyGroup);
        dest.writeString(this.key);
    }

    protected Dish(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.price = in.readFloat();
        this.photoUrl = in.readString();
        this.photoName = in.readString();
        this.keyGroup = in.readString();
        this.key = in.readString();
    }

    public static final Creator<Dish> CREATOR = new Creator<Dish>() {
        @Override
        public Dish createFromParcel(Parcel source) {
            return new Dish(source);
        }

        @Override
        public Dish[] newArray(int size) {
            return new Dish[size];
        }
    };
}

package com.example.gek.pizza.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
  Модель стола. Вид столика, размещение и т.д.
 */

public class Table implements Parcelable{

    private Integer tableId;
    private Float rotation;
    private Integer xCoordinate;
    private Integer yCoordinate;
    private Integer xResolution;
    private Integer yResolution;
    private String key;
    private Integer pictureId;
    private Integer isPortraitMode;
    private String pictureName;

    public Table() {
    }

    public Integer getPortraitMode() {
        return isPortraitMode;
    }

    public void setPortraitMode(Integer portraitMode) {
        isPortraitMode = portraitMode;
    }

    public Table(Integer tableId, Integer xCoordinate, Integer yCoordinate,
                 Integer xResolution, Integer yResolution, Integer pictureId,
                 Float rotation, Integer isPortraitMode, String pictureName) {
        this.tableId = tableId;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.xResolution = xResolution;
        this.yResolution = yResolution;
        this.pictureId   = pictureId;
        this.rotation    = rotation;
        this.isPortraitMode = isPortraitMode;
        this.pictureName = pictureName;

        this.key = key;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public Float getRotation() {
        return rotation;
    }

    public void setRotation(Float rotation) {
        this.rotation = rotation;
    }

    public Integer getPictureId() {
        return pictureId;
    }

    public void setPictureId(Integer pictureId) {
        this.pictureId = pictureId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getxCoordinate() {
        return xCoordinate;
    }

    public void setxCoordinate(Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Integer getyCoordinate() {
        return yCoordinate;
    }

    public void setyCoordinate(Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    public Integer getxResolution() {
        return xResolution;
    }

    public void setxResolution(Integer xResolution) {
        this.xResolution = xResolution;
    }

    public Integer getyResolution() {
        return yResolution;
    }

    public void setyResolution(Integer yResolution) {
        this.yResolution = yResolution;
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
        dest.writeInt(this.tableId);
        dest.writeString(this.key);
        dest.writeInt(this.xCoordinate);
        dest.writeInt(this.yCoordinate);
        dest.writeInt(this.xResolution);
        dest.writeInt(this.yResolution);
        dest.writeInt(this.pictureId);
        dest.writeFloat(this.rotation);
        dest.writeInt(this.isPortraitMode);
        dest.writeString(this.pictureName);
    }

    protected Table(Parcel in) {
        this.tableId = in.readInt();
        this.xCoordinate = in.readInt();
        this.yCoordinate = in.readInt();
        this.xResolution = in.readInt();
        this.yResolution = in.readInt();
        this.pictureId = in.readInt();
        this.key = in.readString();
        this.rotation = in.readFloat();
        this.isPortraitMode= in.readInt();
        this.pictureName= in.readString();
    }

    public static final Creator<Table> CREATOR = new Creator<Table>() {
        @Override
        public Table createFromParcel(Parcel source) {
            return new Table(source);
        }

        @Override
        public Table[] newArray(int size) {
            return new Table[size];
        }
    };
}

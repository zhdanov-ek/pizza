package com.example.gek.pizza.data;

import java.util.Date;

/**
 * Use in CourierService for save courier position to DB
 */

public class LastPosition {
    private Double latitude;
    private Double longitude;
    private Date date;


    public LastPosition() {
    }

    public LastPosition(Double latitude, Double longitude, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}

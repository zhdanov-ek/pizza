package com.example.gek.pizza.data;

/**
 * Created by Ivleshch on 22.02.2017.
 */

public class StateTableReservation {
    private String reservationKey;
    private int reservationState;
    private String key;

    public StateTableReservation() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReservationKey() {
        return reservationKey;
    }

    public void setReservationKey(String reservationKey) {
        this.reservationKey = reservationKey;
    }

    public int getReservationState() {
        return reservationState;
    }

    public void setReservationState(int reservationState) {
        this.reservationState = reservationState;
    }
}

package com.example.gek.pizza.data;

/**
 * Model for saving state of last delivery
 */

public class StateLastDelivery {
    private String deliveryId;
    private int deliveryState;

    public StateLastDelivery() {
    }


    public int getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(int deliveryState) {
        this.deliveryState = deliveryState;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

}

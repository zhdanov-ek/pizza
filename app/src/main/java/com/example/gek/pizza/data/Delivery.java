package com.example.gek.pizza.data;

import java.util.ArrayList;

/**
 * Класс используется для передачи заявки на доставку в заведение
 */

public class Delivery {
    private String clientName;
    private String phoneClient;
    private String addressClient;
    private String commentClient;
    private String commentShop;
    private float totalSum;
    private ArrayList<Order> orders;

    //todo Добавить время создания, принятия, отправки и получения (закрытия) доставки

    public Delivery() {
    }

    public Delivery(String clientName, String phoneClient, String addressClient,
                    String commentClient, String commentShop, float totalSum, ArrayList<Order> orders) {
        this.clientName = clientName;
        this.phoneClient = phoneClient;
        this.addressClient = addressClient;
        this.commentClient = commentClient;
        this.commentShop = commentShop;
        this.totalSum = totalSum;
        this.orders = orders;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhoneClient() {
        return phoneClient;
    }

    public void setPhoneClient(String phoneClient) {
        this.phoneClient = phoneClient;
    }

    public String getAddressClient() {
        return addressClient;
    }

    public void setAddressClient(String addressClient) {
        this.addressClient = addressClient;
    }

    public String getCommentClient() {
        return commentClient;
    }

    public void setCommentClient(String commentClient) {
        this.commentClient = commentClient;
    }

    public String getCommentShop() {
        return commentShop;
    }

    public void setCommentShop(String commentShop) {
        this.commentShop = commentShop;
    }

    public float getTotalSum() {
        return totalSum;
    }

    public void setTotalSum(float totalSum) {
        this.totalSum = totalSum;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

}

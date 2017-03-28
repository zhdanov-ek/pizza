package com.example.gek.pizza.data;

import java.util.ArrayList;
import java.util.Date;

/**
 * This model store all data for delivery: list dishes, custom pizza, contacts
 */

public class Delivery {
    private String nameClient;
    private String phoneClient;
    private String addressClient;
    private String commentClient;
    private String commentShop;
    private float totalSum;
    private ArrayList<String> keysDishes;
    private ArrayList<Integer> numbersDishes;
    private ArrayList<String> textMyPizza;
    private ArrayList<Integer> numbersMyPizza;
    private String key;
    private Date dateNew;
    private Date dateCooking;
    private Date dateTransport;
    private Date dateArchive;
    private Boolean isPaid;                     // paid or reject (need to archive)
    private String userId;
    private String userEmail;
    private String longitude;
    private String latitude;


    public Delivery() {
        this.dateNew = new Date();
    }

    public String getNameClient() {
        return nameClient;
    }

    public void setNameClient(String nameClient) {
        this.nameClient = nameClient;
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

    public ArrayList<Integer> getNumbersDishes() {
        return numbersDishes;
    }

    public void setNumbersDishes(ArrayList<Integer> numbersDishes) {
        this.numbersDishes = numbersDishes;
    }

    public ArrayList<String> getKeysDishes() {
        return keysDishes;
    }

    public void setKeysDishes(ArrayList<String> keysDishes) {
        this.keysDishes = keysDishes;
    }

    public ArrayList<String> getTextMyPizza() {
        return textMyPizza;
    }

    public void setTextMyPizza(ArrayList<String> textMyPizza) {
        this.textMyPizza = textMyPizza;
    }

    public ArrayList<Integer> getNumbersMyPizza() {
        return numbersMyPizza;
    }

    public void setNumbersMyPizza(ArrayList<Integer> numbersMyPizza) {
        this.numbersMyPizza = numbersMyPizza;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getDateNew() {
        return dateNew;
    }

    public void setDateNew(Date dateNew) {
        this.dateNew = dateNew;
    }

    public Date getDateCooking() {
        return dateCooking;
    }

    public void setDateCooking(Date dateCooking) {
        this.dateCooking = dateCooking;
    }

    public Date getDateTransport() {
        return dateTransport;
    }

    public void setDateTransport(Date dateTransport) {
        this.dateTransport = dateTransport;
    }

    public Date getDateArchive() {
        return dateArchive;
    }

    public void setDateArchive(Date dateArchive) {
        this.dateArchive = dateArchive;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}

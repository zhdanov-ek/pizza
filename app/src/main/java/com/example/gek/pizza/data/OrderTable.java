package com.example.gek.pizza.data;

import java.util.Date;

/**
  Information about reservation
 */

public class OrderTable{
    private String clientName;
    private String phoneClient;
    private String commentClient;
    private String tableKey;
    private Date date;
    private String key;
    private Integer isNotificated;
    private Integer isCheckedByAdmin;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getIsCheckedByAdmin() {
        return isCheckedByAdmin;
    }

    public void setIsCheckedByAdmin(Integer isCheckedByAdmin) {
        this.isCheckedByAdmin = isCheckedByAdmin;
    }

    public Integer getIsNotificated() {
        return isNotificated;
    }

    public void setIsNotificated(Integer isNotificated) {
        this.isNotificated = isNotificated;
    }



    public OrderTable() {
        this.date = new Date();
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

    public String getCommentClient() {
        return commentClient;
    }

    public void setCommentClient(String commentClient) {
        this.commentClient = commentClient;
    }

    public String getTableKey() {
        return tableKey;
    }

    public void setTableKey(String tableKey) {
        this.tableKey = tableKey;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}

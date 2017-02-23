package com.example.gek.pizza.data;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;


/**
 * Singleton
 * Current auth: have status of auth: user, shop or anonymous
 */

public class Connection {
    private static final String TAG = "CONNECTION singleton";
    private static Connection instance;
    private int currentAuthStatus;

    // Еmail of shop for auth as administration of pizzeria
    private String shopEmail = "uf.ua.bph@gmail.com";

    public static synchronized Connection getInstance(){
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    // Constructor
    private Connection(){
    }


    public void signOut(){
        FirebaseAuth.getInstance().signOut();
        currentAuthStatus = Const.AUTH_NULL;
        Log.d(TAG, "sign out FireBase");
    }


    public String getShopEmail() {
        return shopEmail;
    }
    public int getCurrentAuthStatus() {
        return currentAuthStatus;
    }
    public void setCurrentAuthStatus(int currentAuthStatus) {
        this.currentAuthStatus = currentAuthStatus;
    }
}

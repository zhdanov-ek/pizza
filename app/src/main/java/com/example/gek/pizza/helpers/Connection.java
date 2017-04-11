package com.example.gek.pizza.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.services.CourierService;
import com.example.gek.pizza.services.MonitoringYourDeliveryService;
import com.example.gek.pizza.services.MonitoringYourReservationService;
import com.example.gek.pizza.services.ShopService;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Singleton
 * Current auth: have status of auth: user, shop or anonymous
 */

public class Connection {
    private static final String TAG = "CONNECTION singleton";
    private static Connection instance;
    private int lastAuthState;
    private int currentAuthStatus;
    private boolean serviceRunning;
    private String userId;
    private String userName;
    private String userPhotoUrl;

    // Еmails for auth as shop and courier of pizzeria
    private SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    private String shopEmail =
            sharedPreferences.getString(Const.SETTINGS_ADMIN_EMAIL_KEY, Const.ADMIN_EMAIL_BY_DEFAULT);
    private String courierEmail =
            sharedPreferences.getString(Const.SETTINGS_COURIER_EMAIL_KEY, Const.COURIER_EMAIL_BY_DEFAULT);

    public static synchronized Connection getInstance(){
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    // Constructor
    private Connection(){
        serviceRunning = false;
        userId = "";
        userName = "";
        userPhotoUrl = "";
        if (sharedPreferences.contains(Const.SETTINGS_LAST_AUTH_STATE)){
            lastAuthState = sharedPreferences.getInt(Const.SETTINGS_LAST_AUTH_STATE, Const.AUTH_NULL);
        } else {
            lastAuthState = Const.AUTH_NULL;
        }
    }


    /** Sign out user: close service if need */
    public void signOut(Context ctx){
        switch (currentAuthStatus){
            case Const.AUTH_SHOP:
                ctx.stopService(new Intent(ctx, ShopService.class));
                break;
            case Const.AUTH_COURIER:
                ctx.stopService(new Intent(ctx, CourierService.class));
                break;
            case Const.AUTH_USER:
                ctx.stopService(new Intent(ctx, MonitoringYourDeliveryService.class));
                ctx.stopService(new Intent(ctx, MonitoringYourReservationService.class));

                // destroy Favorites and orders
                // Need for correct work favorites in new session
                Basket.getInstance().clearOrders();
                Favorites.getInstance().closeSession();
                break;
        }

        FirebaseAuth.getInstance().signOut();
        currentAuthStatus = Const.AUTH_NULL;
        serviceRunning = false;
        Log.d(TAG, "sign out FireBase");

//      programmatically logout
        LoginManager.getInstance().logOut();
    }


    public String getShopEmail() {
        return shopEmail;
    }
    public void setShopEmail(String shopEmail) {
        this.shopEmail = shopEmail;
    }

    public String getCourierEmail() {
        return courierEmail;
    }
    public void setCourierEmail(String courierEmail) {
        this.courierEmail = courierEmail;
    }

    // if pre state is SHOP or COURIER need do stop services
    public void setCurrentAuthStatus(int currentAuthStatus, Context ctx) {
        this.currentAuthStatus = currentAuthStatus;
        if (lastAuthState != currentAuthStatus){
            sharedPreferences.edit().putInt(Const.SETTINGS_LAST_AUTH_STATE, currentAuthStatus).apply();
            if (lastAuthState == Const.AUTH_SHOP){
                ctx.stopService(new Intent(ctx, ShopService.class));
            } else if (lastAuthState == Const.AUTH_COURIER){
                ctx.stopService(new Intent(ctx, CourierService.class));
            }
        }
    }
    public int getCurrentAuthStatus() {
        return currentAuthStatus;
    }

    public boolean getServiceRunning() {
        return serviceRunning;
    }
    public void setServiceRunning(boolean serviceRunning) {
        this.serviceRunning = serviceRunning;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }
    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
}

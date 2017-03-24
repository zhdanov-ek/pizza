package com.example.gek.pizza.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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
    private int currentAuthStatus;

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
    }


    public void signOut(Context ctx){
        //todo включить после основных работ отладочных и удаления кнопок в главном меню (запуск сервиса)
//        if (currentAuthStatus == Const.AUTH_SHOP) {
//            ctx.stopService(new Intent(ctx, ShopService.class));
//        }
        FirebaseAuth.getInstance().signOut();

        // destroy Favorites if signOut from UserStatus. Need for correct work favorites in new session
        if (currentAuthStatus ==  Const.AUTH_USER)  {
            Favorites.getInstance().closeSession();
        }
        currentAuthStatus = Const.AUTH_NULL;
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

    public int getCurrentAuthStatus() {
        return currentAuthStatus;
    }

    public void setCurrentAuthStatus(int currentAuthStatus) {
        this.currentAuthStatus = currentAuthStatus;
    }
}

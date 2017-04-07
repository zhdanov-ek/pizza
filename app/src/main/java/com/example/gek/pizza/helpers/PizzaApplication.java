package com.example.gek.pizza.helpers;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;


/**
 * Initialize global values and objects
 */

public class PizzaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Crashlytics  (Uncomment before building APK):
        Fabric.with(this, new Crashlytics());

        // Enable offline mode for Firebase - caching data in local storage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Load dishes
        AllDishes.getInstance();

        AllTopics.getInstance();

    }

}

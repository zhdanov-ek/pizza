package com.example.gek.pizza.helpers;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Инициализация глобальных настроек
 */

public class PizzaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Включаем кеширование данных что позволяет отображать данные офлайн
        // Инициализация этого значения делается до начала работы с FireBase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

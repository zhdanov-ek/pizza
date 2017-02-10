package com.example.gek.pizza.helpers;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.example.gek.pizza.data.AllDishes;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;

/**
 * Инициализация глобальных настроек
 */

public class PizzaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Crashlytics  (Uncomment before building APK):
        Fabric.with(this, new Crashlytics());
        // Включаем кеширование данных что позволяет отображать данные офлайн
        // Инициализация этого значения делается до начала работы с FireBase
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //Грузим сразу список блюд в синглтон. Потом в заказах используем эту информацию
        AllDishes.getInstance();

        // todo  убираем лисенер списка блюд. Нужно переместить куда-нибудь. Тут не работает
        // AllDishes.getInstance().removeListener();
    }

}

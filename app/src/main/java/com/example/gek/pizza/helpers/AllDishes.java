package com.example.gek.pizza.helpers;

import android.util.Log;

import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;


/**
 * Синглтон, который хранит актуальный список всех блюд. Загружается при старте программы.
 * Данные используются для вывода списка блюд в обработке заказов на доставку
 */

public class AllDishes {
    public ArrayList<Dish> dishes;
    private static AllDishes instance;
    private static String TAG = "AllDishes";


    // В конструкторе устанавливаем лисенер, который будет актуализировать список наших блюд
    private AllDishes(){
        dishes = new ArrayList<>();
        db.child(Const.CHILD_DISHES).addValueEventListener(dishesListener);
        Log.d(TAG, "Add dish listenerFavoritesDishes");
    }

    // Получаем инстанс через метод, а не конструктор, который скрыт
    public static synchronized AllDishes getInstance(){
        if (instance == null) {
            instance = new AllDishes();
        }
        return instance;
    }

    // Ищем блюдо по ключу
    public Dish getDish(String key){
        for (Dish dish: dishes) {
            if (dish.getKey().contentEquals(key)){
                return dish;
            }
        }
        return null;
    }


    private ValueEventListener dishesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dishes.clear();
            for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                dishes.add(childSnapShot.getValue(Dish.class));
            }
            Log.d(TAG, "New dishes has been loaded");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d(TAG, "onCancelled load dishes from server");
        }
    };

}

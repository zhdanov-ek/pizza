package com.example.gek.pizza.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Вспомогательные методы
 */

public class Utils {

    // todo Написать метод для получения текущего времени и приведения его в нормальный вид

    // Проверяет есть ли инет
    public static boolean hasInternet(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // возвращает URI картинки
    public static void choosePhoto(AppCompatActivity apa){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(apa.getPackageManager()) != null){
            apa.startActivityForResult(photoPickerIntent, Const.REQUEST_LOAD_IMG);
        } else {
            Toast.makeText(apa, "No program for choose image!", Toast.LENGTH_LONG).show();
        }
    }

    // Отбирает блюда по ключу группы
    public static ArrayList<Dish> selectGroup(ArrayList<Dish> allDishes, String keyGroup){
        ArrayList<Dish> resultList = new ArrayList<>();
        for (Dish dish: allDishes) {
            if (dish.getKeyGroup().contentEquals(keyGroup)){
                resultList.add(dish);
            }
        }
        return resultList;
    }

    /** Формируем уникальное имя для фотки на базе входящей строки.
     *  Убираем нежелательные символы для нормального хранения в файрбейс
     *               . $ [ ] # /            */
    public static String makePhotoName(String name){
        String time = Calendar.getInstance().getTime().toString();
        String nameFile = name + time;
        nameFile = nameFile.replace(".", "");
        nameFile = nameFile.replace("@", "");
        nameFile = nameFile.replace(" ", "");
        nameFile = nameFile.replace("#", "");
        nameFile = nameFile + ".jpg";
        return  nameFile;
    }

    /** Create name of key for delivery child */
    public static String makeDeliveryNumber(String phone){
        String s = Calendar.getInstance().getTime().toString();
        s = phone + s;
        s = s.replace(".", "");
        s = s.replace("$", "");
        s = s.replace("[", "");
        s = s.replace("]", "");
        s = s.replace("#", "");
        s = s.replace("/", "");
        return s;
    }


    // возвращает значение настроек
    public static String getSetting(AppCompatActivity app, String settingKey) {
        SharedPreferences prefs = app.getSharedPreferences(Const.SETTINGS_KEY, Context.MODE_PRIVATE);

        return prefs.getString(settingKey, "");
    }


    // Ищет блюдо в корзине. Возвращает 0 если его там нет и кол-во если блюдо уже в заказе
    public static int findInBasket(Dish dish){
        for (int i = 0; i < Basket.getInstance().orders.size(); i++) {
            if (dish.getKey().contentEquals(Basket.getInstance().orders.get(i).getKeyDish())){
                return Basket.getInstance().orders.get(i).getCount();
            }
        }
        return 0;
    }

    // Convert float "50.2" to string "50.20 usd"
    public static String toPrice(float f){
        String s = String.format("%.2f", f) + " грн";
        return s;
    }

    // Формирует строку для доставки в виде: Пельмени (30 грн) х 2 шт = 60 грн
    public static String makeOrderString(Dish dish, int count){
        String s = dish.getName() + " (" + Utils.toPrice(dish.getPrice()) + ") x " +
                count + " = " + Utils.toPrice(dish.getPrice()*count);
        return s;
    }


}

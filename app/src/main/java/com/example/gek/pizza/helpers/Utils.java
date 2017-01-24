package com.example.gek.pizza.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by gek on 17.01.17.
 */

public class Utils {

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
     *  Убираем нежелательные символы для нормального хранения в файрбейс */
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

}

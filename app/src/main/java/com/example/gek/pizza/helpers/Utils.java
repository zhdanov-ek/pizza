package com.example.gek.pizza.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.gek.pizza.data.Const;

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
}

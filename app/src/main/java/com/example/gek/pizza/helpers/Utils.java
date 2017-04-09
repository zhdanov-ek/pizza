package com.example.gek.pizza.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.Dish;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Helpers methods
 */

public class Utils {

    /** Check internet connection */
    public static boolean hasInternet(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /** Return URI image */
    public static void choosePhoto(AppCompatActivity apa){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(apa.getPackageManager()) != null){
            apa.startActivityForResult(photoPickerIntent, Const.REQUEST_LOAD_IMG);
        } else {
            Toast.makeText(apa,
                    apa.getResources().getString(R.string.mes_no_soft_for_choose_image),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Select dishes with one group */
    public static ArrayList<Dish> selectGroup(ArrayList<Dish> allDishes, String keyGroup){
        ArrayList<Dish> resultList = new ArrayList<>();
        for (Dish dish: allDishes) {
            if (dish.getKeyGroup().contentEquals(keyGroup)){
                resultList.add(dish);
            }
        }
        return resultList;
    }

    /** Make name for photo. Need for safe to storage.
     *  Delete character if found: . $ [ ] # /
     *  */
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


    /** Remove symbol for write FireBase. Use in EditText */
    public static InputFilter getInputFilterSymbol(){
        final String blockCharacterSet = "$[]#/.";
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && blockCharacterSet.contains(("" + source))) {
                    return "";
                }
                return null;
            }
        };
    }


    // todo Move this function to Basket
    /**
     * Find dish in basket. Return 0 if not find, or quantity in other case
     */
    public static int findInBasket(Dish dish){
        for (int i = 0; i < Basket.getInstance().orders.size(); i++) {
            if (dish.getKey().contentEquals(Basket.getInstance().orders.get(i).getKeyDish())){
                return Basket.getInstance().orders.get(i).getCount();
            }
        }
        return 0;
    }

    //todo move " грн" to resources
    /** Convert float "50.2" to string "50.20 usd" */
    public static String toPrice(float f){
        return String.format("%.2f", f) + " грн";

    }

    /** Make text for show in delivery. Example: Tomato (10 usd) х 2 pieces = 20 usd */
    public static String makeOrderString(Dish dish, int count, Context ctx){
        String s;
        if (dish != null){
            s = dish.getName() + " \n\t\t" + Utils.toPrice(dish.getPrice()) + " x " +
                    count + " = " + Utils.toPrice(dish.getPrice()*count);
        } else {
            s = ctx.getResources().getString(R.string.mes_removed);
        }
        return s;
    }

    /** Format date to human readable style */
    public static String formatDate(Date date){
        SimpleDateFormat formatShort = new SimpleDateFormat("HH:mm");
        SimpleDateFormat formatFull = new SimpleDateFormat();
        SimpleDateFormat fetchDate = new SimpleDateFormat("yyyy.MM.dd");
        if (fetchDate.format(new Date()).contentEquals(fetchDate.format(date))){
            return formatShort.format(date);
        } else {
            return formatFull.format(date);
        }
    }


    public static String getTimeHistoryDelivery(Delivery delivery, Context ctx){
        SimpleDateFormat formatShort = new SimpleDateFormat(": HH:mm");
        String str = ctx.getResources().getString(R.string.delivery_time_new) +
                formatShort.format(delivery.getDateNew());
        if (delivery.getDateCooking() != null){
            str += "\n" +ctx.getResources().getString(R.string.delivery_time_cook) +
                    formatShort.format(delivery.getDateCooking());
        }
        if (delivery.getDateTransport() != null){
            str += "\n" +ctx.getResources().getString(R.string.delivery_time_transport) +
                    formatShort.format(delivery.getDateTransport());
        }
        if (delivery.getDateArchive() != null){
            str += "\n" +ctx.getResources().getString(R.string.delivery_time_archive) +
                    formatShort.format(delivery.getDateArchive());
        }
        return str;
    }

    public static void subscribeOrUnsubscribeFromTopic(boolean isSubscribe)  {
        for (String topic: AllTopics.getInstance().topics) {
            if(isSubscribe){
                FirebaseMessaging.getInstance().subscribeToTopic(topic);
            } else{
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
            }
        }
    }


    // Open system settings of program
    public static void openPermissionSettings(Context ctx) {
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + ctx.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ctx.startActivity(intent);
    }

}

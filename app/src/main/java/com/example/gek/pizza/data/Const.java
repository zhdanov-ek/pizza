package com.example.gek.pizza.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by gek on 17.01.17.
 */

public class Const {
    // DataBase
    public static final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    public static final String CHILD_DISHES = "dishes";
    public static final String CHILD_NEWS = "news";
    public static final String CHILD_MENU_GROUPS = "menu_groups";
    public static final String CHILD_SETTINGS = "settings";

    public static final String CHILD_DELIVERIES_NEW = "deliveries/new";
    public static final String CHILD_DELIVERIES_COOKING = "deliveries/cooking";
    public static final String CHILD_DELIVERIES_TRANSIT = "deliveries/transit";
    public static final String CHILD_DELIVERIES_ARCHIVE = "deliveries/archive";


    // Storage
    public static final String STORAGE = "gs://pizza-7ee24.appspot.com";

    public static final String MENU_GROUP_IMAGES_FOLDER = "menu_group_images";
    public static final String NEWS_IMAGES_FOLDER = "news_images";
    public static final String DISHES_IMAGES_FOLDER = "dishes_images";


    // Константы для редактирования и создания элементов
    public static final String MODE = "edit_mode";
    public static final int MODE_NEW = 0;
    public static final int MODE_EDIT = 1;

    public static final String EXTRA_NEWS = "news_object";
    public static final String EXTRA_DISH = "dish_object";
    public static final String EXTRA_MENU_GROUP = "menu_group_object";

    public static final String DISH_GROUP_KEY = "dish_group_key";
    public static final String DISH_GROUP_NAME = "dish_group_name";

//    public static final int DELIVERY_STATUS_NEW = 1;
//    public static final int DELIVERY_STATUS_COOK = 2;
//    public static final int DELIVERY_STATUS_TRANSIT = 3;
//    public static final int DELIVERY_STATUS_ARCHIVE = 4;

    // id используемые при вызовах активити через интент
    public static final int REQUEST_LOAD_IMG = 1;
    public static final int REQUEST_EDIT_NEWS = 10;
    public static final int REQUEST_EDIT_DISH = 11;

    //key для настроек
    public static final String SETTINGS_KEY             = "com.example.gek.pizza";
    public static final String SETTINGS_PHONE_KEY       = "phone";
    public static final String SETTINGS_ADDRESS_KEY     = "address";
    public static final String SETTINGS_EMAIL_KEY       = "email";
    public static final String SETTINGS_LATITUDE_KEY    = "latitude";
    public static final String SETTINGS_LONGITUDE_KEY   = "longitude";
    public static final Integer ZOOM_MAP                = 17;

    public static final int REQUEST_CODE_LOCATION  = 20;

    public static final String GOOGLE_DIRECTIONS_API   = "https://maps.googleapis.com/maps/";

    public static final Integer LOCATION_INTERVAL_UPDATE = 10;
    public static final Integer LOCATION_DISTANCE_UPDATE = 30;
    public static final Integer OFFSET_FROM_EDGES_OF_THE_MAP= 150;


}

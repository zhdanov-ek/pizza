package com.example.gek.pizza.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Constants: keys, urls, fields etc
 */

public class Const {
    // DataBase
    public static final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    public static final String CHILD_DISHES = "dishes";
    public static final String CHILD_NEWS = "news";
    public static final String CHILD_MENU_GROUPS = "menu_groups";
    public static final String CHILD_SETTINGS = "settings";
    public static final String CHILD_TABLES = "tables";
    public static final String CHILD_RESERVED_TABLES_NEW = "reserved_tables/new";
    public static final String CHILD_RESERVED_TABLES_ARCHIVE = "reserved_tables/archive";
    public static final String CHILD_TOPICS = "topics";
    public static final String CHILD_COURIER = "courier";

    public static final String REMOTE_CONFIG_ADMIN_EMAIL = "admin_email";

    public static final String CHILD_USERS = "users";
    public static final String CHILD_USER_DELIVERY_STATE = "delivery_state";
    public static final String CHILD_USER_RESERVATION_STATE = "reservation_state";
    public static final String CHILD_USER_FAVORITES = "favorites";

    // Отображают текущее состояние заказа в юзерской папке
    public static final int DELIVERY_STATE_NEW = 0;
    public static final int DELIVERY_STATE_COOKING = 1;
    public static final int DELIVERY_STATE_TRANSPORT = 2;
    public static final int DELIVERY_STATE_ARCHIVE = 9;

    // Пути где хранятся заказы в зависимости от текущего состояния
    public static final String CHILD_DELIVERIES_NEW = "deliveries/new";
    public static final String CHILD_DELIVERIES_COOKING = "deliveries/cooking";
    public static final String CHILD_DELIVERIES_TRANSPORT = "deliveries/transport";
    public static final String CHILD_DELIVERIES_ARCHIVE = "deliveries/archive";


    // Auth mode
    public static final int AUTH_NULL = 0;
    public static final int AUTH_USER = 1;
    public static final int AUTH_COURIER = 2;
    public static final int AUTH_SHOP = 5;


    // Storage
    public static final String STORAGE = "gs://pizza-7ee24.appspot.com";

    public static final String MENU_GROUP_IMAGES_FOLDER = "menu_group_images";
    public static final String NEWS_IMAGES_FOLDER = "news_images";
    public static final String DISHES_IMAGES_FOLDER = "dishes_images";


    // Константы для редактирования и создания элементов, запуска активити в различных режимов
    public static final String MODE = "mode";
    public static final int MODE_NEW = 0;
    public static final int MODE_EDIT = 1;
    public static final int MODE_ARCHIVE_DELIVERIES = 11;
    public static final int MODE_TRANSPORT_DELIVERIES = 12;

    public static final String EXTRA_NEWS = "news_object";
    public static final String EXTRA_DISH = "dish_object";
    public static final String EXTRA_MENU_GROUP = "menu_group_object";
    public static final String EXTRA_IS_FAVORITE = "is_favorite";
    public static final String EXTRA_TABLE = "table_object";
    public static final String EXTRA_TABLE_KEY = "table_object_key";
    public static final String EXTRA_DELIVERY_ID = "delivery_id";

    public static final String DISH_GROUP_KEY = "dish_group_key";
    public static final String DISH_GROUP_VALUE_REMOVED = "removed";

    // Константы для обозначения пунктов в меню екшнбара
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_REMOVE = 2;
    public static final int ACTION_ARCHIVE = 3;
    public static final int ACTION_BASKET = 4;

    // id используемые при вызовах активити через интент
    public static final int REQUEST_LOAD_IMG = 1;
    public static final int REQUEST_EDIT_DISH = 11;
    public static final int REQUEST_RESERVE_TABLE = 12;


    // Ключ для кастомной пиццы. Эти блюда не попадают в БД, а используются только на клиенте
    public static final String KEY_DISH_MY_PIZZA = "my_pizza";

    //key для настроек
    public static final String SETTINGS_KEY             = "com.example.gek.pizza";
    public static final String SETTINGS_PHONE_KEY       = "phone";
    public static final String SETTINGS_ADDRESS_KEY     = "address";
    public static final String SETTINGS_EMAIL_KEY       = "email";
    public static final String SETTINGS_LATITUDE_KEY    = "latitude";
    public static final String SETTINGS_LONGITUDE_KEY   = "longitude";
    public static final String SETTINGS_ADMIN_EMAIL_KEY = "admin_email";
    public static final String ADMIN_EMAIL_BY_DEFAULT   = "none@gmail.com";
    public static final Integer ZOOM_MAP                = 17;

    public static final int REQUEST_CODE_LOCATION  = 20;

    public static final String GOOGLE_DIRECTIONS_API   = "https://maps.googleapis.com/maps/";

    public static final Integer LOCATION_INTERVAL_UPDATE = 10;
    public static final Integer LOCATION_DISTANCE_UPDATE = 30;
    public static final Integer OFFSET_FROM_EDGES_OF_THE_MAP= 50;

    public static final String OPEN_FROM_NOTIFICATION = "open_from_notification";

    public static final Integer COUNT_OF_NOTIFICATION_REPEAT_ALARM = 2;
    public static final Integer TIME_INTERVAL_NOTIFICATION_START = 10000;  //10 sec
    public static final Integer TIME_INTERVAL_NOTIFICATION_REPEAT = 10000; //10 sec

    public static final Integer RESERVED_TABLE_NOTIFY_ID = 101;
    public static final Integer ODRED_NOTIFY_ID = 1;
    public static final Integer STATUS_CHECKED_BY_ADMIN = 1;
    public static final Integer RESERVED_TABLE_USER_NOTIFY_ID = 201;
    public static final Integer PUSH_NITIFICATIONS_ID = 301;

    public static final int RESERVATION_TABLE_STATE_CONFIRMED = 1;
    public static final int RESERVATION_TABLE_STATE_CANCEL = 2;

    public static final String GOOGLE_PROVIDER = "google.com";
    public static final String FACEBOOK_PROVIDER = "facebook.com";

    public static final String FCM_TITLE="title";
    public static final String FCM_BODY="body";
    public static final String FCM_DATA="data";
    public static final String FCM_TO="to";
    public static final String FCM_TOPICS="/topics/";
    public static final String FCM_AUTHORIZATION="authorization";
    public static final String FCM_KEY="key=";
    public static final String FCM_API="https://fcm.googleapis.com/fcm/send";

    public static final int LOCATION_COURIER_INTERVAL_UPDATE = 3;
}

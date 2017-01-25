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


    // id используемые при вызовах активити через интент
    public static final int REQUEST_LOAD_IMG = 1;
    public static final int REQUEST_EDIT_NEWS = 10;
    public static final int REQUEST_EDIT_DISH = 11;
}

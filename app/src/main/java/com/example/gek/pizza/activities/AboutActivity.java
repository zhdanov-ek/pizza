package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.gek.pizza.R;

/** О заведении: контакты, карта
 * Контактные данные должны иметь возможность корректироваться через админскую часть программы
 * Данные хранить в БД в отдельной ветке */

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}

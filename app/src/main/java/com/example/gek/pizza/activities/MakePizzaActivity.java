package com.example.gek.pizza.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;

/** Создание своей пиццы */

public class MakePizzaActivity extends BaseActivity {

    private int nextIngredient = 0;
    private ImageView ivPizza;

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() ==  Const.AUTH_SHOP){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_make_pizza, null, false);
        mDrawer.addView(contentView, 0);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        ivPizza = (ImageView) findViewById(R.id.ivPizza);
        findViewById(R.id.btnIngredient).setOnClickListener(listenerComponent);

    }

    View.OnClickListener listenerComponent = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Наши картинки в отдельных битмапах
            Bitmap basis = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_basis);
            Bitmap ananas = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_ananas);
            Bitmap mushroomsFried = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_mushrooms_fried);

            // Создаем канвас через который будем рисовать на нашей главной битмапе - основе
            Canvas canvas = new Canvas(basis);

            // Добавляем в главную битмапу через канву дополнительные картинки каждого ингредиента
            canvas.drawBitmap(basis, 0, 0, null);
            canvas.drawBitmap(ananas, 0, 0, null);
            canvas.drawBitmap(mushroomsFried, 0, 0, null);

            // Выводим картинку во вью
            ivPizza.setImageBitmap(basis);

        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
            menu.add(0, Const.ACTION_BASKET, 0, R.string.action_basket)
                    .setIcon(R.drawable.ic_basket)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case Const.ACTION_BASKET:
                startActivity(new Intent(this, BasketActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

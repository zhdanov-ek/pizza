package com.example.gek.pizza.activities;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;

import java.util.ArrayList;

/** Создание своей пиццы */

public class MakePizzaActivity extends BaseActivity {

    public static final String TAG = "3333333333";

    private RelativeLayout rlContent;
    private LinearLayout llIngredients;
    private ImageView ivPizza;
    private ArrayList<Integer> listIngredients;
    private ImageView currentIngredient;


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

        llIngredients = (LinearLayout) findViewById(R.id.llIngredients);
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        ivPizza = (ImageView) findViewById(R.id.ivPizza);

        findViewById(R.id.btnClear).setOnClickListener(listenerClear);

        // cлушаем события перетягивания на нашу пиццу
        ivPizza.setOnDragListener(onDragListenerIngredient);

        clearPizza();
    }

    View.OnLongClickListener ingredientLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Log.d(TAG, "onLongClick: ");
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            currentIngredient = (ImageView) view;
            return true;
        }
    };

//    View.OnTouchListener ingredientTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//
//            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                ClipData data = ClipData.newPlainText("", "");
//                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
//                view.startDrag(data, shadowBuilder, view, 0);
//                view.setVisibility(View.GONE);
//                return true;
//            } else {
//                return false;
//            }
//        }
//    };

    private void clearPizza(){
        if (listIngredients == null) {
            listIngredients = new ArrayList<>();
        } else {
            listIngredients.clear();
        }

        llIngredients.removeAllViews();

        for (int i = 0; i < 5; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setId(i);
            imageView.setPadding(2, 2, 2, 2);
            imageView.setImageBitmap(
                    BitmapFactory.decodeResource(getResources(), R.drawable.pizza_list_tomato));
            imageView.setOnLongClickListener(ingredientLongClickListener);
            llIngredients.addView(imageView);
        }
        updatePizza();
    }

    private void updatePizza(){
        Bitmap basis = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_basis);

        // Создаем канвас через который будем рисовать на нашей битмапе - result
        Bitmap result = Bitmap.createBitmap(basis.getWidth(), basis.getHeight(), basis.getConfig());
        Canvas canvas = new Canvas(result);

        // Выводим первый слой - основу пиццы
        canvas.drawBitmap(basis, 0, 0, null);

        // выводим все слои с массива - выбранные ингредиенты
        for (int resource: listIngredients){
            Bitmap layer = BitmapFactory.decodeResource(getResources(), resource);
            canvas.drawBitmap(layer, 0, 0, null);
        }

        // Выводим картинку во вью
        ivPizza.setImageBitmap(result);
    }

    private View.OnClickListener listenerClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            clearPizza();
        }
    };


    // Отрабатываем события во время перетягивания
    private View.OnDragListener onDragListenerIngredient = new View.OnDragListener() {
        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_STARTED");
                    break;

                case DragEvent.ACTION_DRAG_LOCATION:
                    float X = dragEvent.getX();
                    float Y = dragEvent.getY();
                    Log.d(TAG, "X " + (int) X + " Y " + (int) Y);
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_ENTERED");
                    break;

                case DragEvent.ACTION_DROP:
                    currentIngredient.setVisibility(View.GONE);
                    listIngredients.add(R.drawable.pizza_bacon);
                    listIngredients.add(R.drawable.pizza_tomatoes);
                    updatePizza();
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_DROPPED");
                    break;

                default:
                    break;
            }
            return true;
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

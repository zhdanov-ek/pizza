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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Ingredient;
import com.example.gek.pizza.data.Ingredients;
import com.example.gek.pizza.helpers.Utils;

import java.util.ArrayList;

/** Создание своей пиццы */

public class MakePizzaActivity extends BaseActivity {

    public static final String TAG = "3333333333";

    private RelativeLayout rlContent;
    private LinearLayout llIngredients;
    private ImageView ivPizza;
    private TextView tvTotal;
    private Button btnClear, btnAdd;
    private ArrayList<Integer> listIdAllImageView;
    private ArrayList<Integer> listIngredientsLayers;
    private ArrayList<Ingredient> basicListIngredients;
    private ImageView ivCurrentIngredient;
    private StringBuffer sbTotal;

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
        tvTotal = (TextView) findViewById(R.id.tvTotal);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPizza();
            }
        });
        btnAdd.setOnClickListener(listenerAdd);

        // cлушаем события перетягивания на нашу пиццу
        ivPizza.setOnDragListener(onDragListenerIngredient);

        basicListIngredients = Ingredients.getIngredients();
        listIdAllImageView = new ArrayList<>();

        // id для ImageView берем по значению картинки из ресурсов программы
        for (int i = 0; i < basicListIngredients.size(); i++) {
            ImageView ivCurrent = new ImageView(this);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150, 150);
            ivCurrent.setLayoutParams(params);
            ivCurrent.setId(basicListIngredients.get(i).getListImageResource());
            listIdAllImageView.add(basicListIngredients.get(i).getListImageResource());
            ivCurrent.setPadding(2, 2, 2, 2);
            ivCurrent.setImageBitmap(
                    BitmapFactory.decodeResource(getResources(), basicListIngredients.get(i).getListImageResource()));
            ivCurrent.setOnLongClickListener(ingredientLongClickListener);
            llIngredients.addView(ivCurrent);
        }
        clearPizza();
    }

    View.OnLongClickListener ingredientLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            Log.d(TAG, "onLongClick: ");
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, view, 0);
            ivCurrentIngredient = (ImageView) view;
            return true;
        }
    };


    /** Убираем все выбранные ингредиенты  и показываем иконки скрытые */
    private void clearPizza(){
        if (listIngredientsLayers == null) {
            listIngredientsLayers = new ArrayList<>();
        } else {
            listIngredientsLayers.clear();
            setVisibleIngredients();
        }
        for (int i = 0; i < llIngredients.getChildCount(); i++) {
            llIngredients.getChildAt(0).setVisibility(View.VISIBLE);
        }
        sbTotal = new StringBuffer();
        tvTotal.setText(sbTotal);
        updatePizza();
    }


    /** Прорисовка пиццы: рисуем основу и затем ингредиенты если они есть */
    private void updatePizza(){
        Bitmap basis = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_basis);

        // Создаем канвас через который будем рисовать на нашей битмапе - result
        Bitmap result = Bitmap.createBitmap(basis.getWidth(), basis.getHeight(), basis.getConfig());
        Canvas canvas = new Canvas(result);

        // Выводим первый слой - основу пиццы
        canvas.drawBitmap(basis, 0, 0, null);

        // выводим все слои с массива - выбранные ингредиенты
        for (int resource: listIngredientsLayers){
            Bitmap layer = BitmapFactory.decodeResource(getResources(), resource);
            canvas.drawBitmap(layer, 0, 0, null);
        }

        // Выводим картинку во вью
        ivPizza.setImageBitmap(result);

        if (sbTotal.length() == 0) {
            btnAdd.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);
        }
    }


    private View.OnClickListener listenerAdd =  new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };


    /** Отрабатываем события во время перетягивания */
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
                    // Скрываем ингредиент в списке и добавляем новый слой в массив, перерисовываем
                    ivCurrentIngredient.setVisibility(View.GONE);
                    Ingredient choosedIngredient = getIngredientWithId(ivCurrentIngredient.getId());
                    sbTotal.append(choosedIngredient.getName() + " " +
                            Utils.toPrice(choosedIngredient.getPrice()) + "\n");
                    tvTotal.setText(sbTotal);
                    listIngredientsLayers.add(choosedIngredient.getPizzaImageResource());
                    updatePizza();
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_DROPPED");
                    break;

                default:
                    break;
            }
            return true;
        }
    };

    /** Находим по айди ImageView инредиент (id задаются как listImageResource) */
    private Ingredient getIngredientWithId(int id){
        for (Ingredient ingredient: basicListIngredients) {
            if (ingredient.getListImageResource() == id){
                return ingredient;
            }
        }
        return null;
    }

    private void setVisibleIngredients(){
        for (int id: listIdAllImageView) {
            findViewById(id).setVisibility(View.VISIBLE);
        }
    }

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

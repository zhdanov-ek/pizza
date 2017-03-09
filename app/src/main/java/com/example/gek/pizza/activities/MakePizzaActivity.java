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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
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
    private ImageView ivFocus;
    private TextView tvTotalSum;
    private TextView tvListIngredients;
    private Button btnClear, btnAdd;
    private ArrayList<Integer> listIdAllImageView;
    private ArrayList<Integer> listIngredientsLayers;
    private ArrayList<Ingredient> basicListIngredients;
    private ImageView ivCurrentIngredient;
    private StringBuffer sbTotal;
    private float totalSum;
    private Animation animShow;
    private Animation animHide;

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
        toolbar.setTitle(R.string.title_make_pizza);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        animShow = AnimationUtils.loadAnimation(this, R.anim.alpha_show);
        animHide = AnimationUtils.loadAnimation(this, R.anim.alpha_hide);

        llIngredients = (LinearLayout) findViewById(R.id.llIngredients);
        rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        ivPizza = (ImageView) findViewById(R.id.ivPizza);
        ivFocus = (ImageView) findViewById(R.id.ivFocus);
        tvListIngredients = (TextView) findViewById(R.id.tvListIngredients);
        tvTotalSum = (TextView) findViewById(R.id.tvTotal);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPizza();
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPizzaToBasket();
            }
        });

        // cлушаем события перетягивания на нашу пиццу
        ivPizza.setOnDragListener(onDragListenerIngredient);

        basicListIngredients = Ingredients.getIngredients();
        listIdAllImageView = new ArrayList<>();

        int sizeIngredient = (int) getResources().getDimension(R.dimen.make_pizza_size_ingredient_in_list);
        int paddingIngredient = (int) getResources().getDimension(R.dimen.make_pizza_padding_ingredient_in_list);
        // id для ImageView берем по значению картинки из ресурсов программы
        for (int i = 0; i < basicListIngredients.size(); i++) {
            ImageView ivCurrent = new ImageView(this);


            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sizeIngredient, sizeIngredient);
            ivCurrent.setLayoutParams(params);
            ivCurrent.setId(basicListIngredients.get(i).getListImageResource());
            listIdAllImageView.add(basicListIngredients.get(i).getListImageResource());
            ivCurrent.setPadding(paddingIngredient, paddingIngredient, paddingIngredient, paddingIngredient);
            ivCurrent.setImageBitmap(
                    BitmapFactory.decodeResource(getResources(), basicListIngredients.get(i).getListImageResource()));
            ivCurrent.setOnLongClickListener(ingredientLongClickListener);
            llIngredients.addView(ivCurrent);
        }
        clearPizza();
    }


    /** Начинаем тянуть картинку */
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
        tvListIngredients.setText(sbTotal);
        tvTotalSum.setText("");
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


   /** Добавляем пиццу в корзину (заворачиваем данные в Dish, который принимает наш адаптер в корзине */
    private void addPizzaToBasket(){
        Dish pizza = new Dish();
        pizza.setName(getResources().getString(R.string.name_of_pizza));
        pizza.setDescription(sbTotal.toString());
        pizza.setPrice(totalSum);
        pizza.setKey(Const.KEY_DISH_MY_PIZZA);

        Basket.getInstance().addDish(pizza);
        clearPizza();
    }

    /** Отрабатываем события во время перетягивания */
    private View.OnDragListener onDragListenerIngredient = new View.OnDragListener() {
        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    ivFocus.startAnimation(animShow);
                    ivFocus.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_ENTERED");
                    break;

                case DragEvent.ACTION_DROP:
                    // Если это первый ингредиент то добавляем сначала основу
                    if (sbTotal.length() == 0){
                        sbTotal.append("\n" + Ingredients.getBasis().getName() + " " +
                            Utils.toPrice(Ingredients.getBasis().getPrice()));
                        totalSum = Ingredients.getBasis().getPrice();
                    }

                    // Скрываем ингредиент в списке и добавляем новый ингредиент
                    ivCurrentIngredient.setVisibility(View.GONE);
                    Ingredient choosedIngredient = getIngredientWithId(ivCurrentIngredient.getId());
                    sbTotal.append("\n" + choosedIngredient.getName() + " " +
                            Utils.toPrice(choosedIngredient.getPrice()));
                    tvListIngredients.setText(sbTotal);
                    totalSum += choosedIngredient.getPrice();
                    String strTotal = getResources().getString(R.string.total) + " = " +
                        Utils.toPrice(totalSum);
                    tvTotalSum.setText(strTotal);
                    listIngredientsLayers.add(choosedIngredient.getPizzaImageResource());
                    updatePizza();
                    ivFocus.startAnimation(animHide);
                    ivFocus.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_DROPPED");
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    ivFocus.startAnimation(animHide);
                    ivFocus.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "Action is DragEvent.ACTION_DRAG_EXITED");
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

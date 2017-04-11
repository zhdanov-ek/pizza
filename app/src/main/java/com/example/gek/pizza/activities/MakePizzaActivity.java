package com.example.gek.pizza.activities;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.DragEvent;
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
import com.example.gek.pizza.helpers.Basket;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.Ingredient;
import com.example.gek.pizza.data.Ingredients;
import com.example.gek.pizza.helpers.Utils;

import java.util.ArrayList;

/** Make custom pizza */

public class MakePizzaActivity extends BaseActivity {

    public static final String TAG = "MAKE_PIZZA";
    public static final String STATE_LIST_INGREDIENTS = "list_ingredients";
    public static final String STATE_TEXT_ORDER = "text_order";
    public static final String STATE_TOTAL_SUM = "total_sum";

    private LinearLayout llIngredients;
    private ImageView ivPizza;
    private ImageView ivFocus;
    private TextView tvInstruction;
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
        if ((Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_SHOP) ||
            (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_COURIER)){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_make_pizza);
        setToolbar(getString(R.string.title_make_pizza));

        animShow = AnimationUtils.loadAnimation(this, R.anim.alpha_show);
        animHide = AnimationUtils.loadAnimation(this, R.anim.alpha_hide);

        llIngredients = (LinearLayout) findViewById(R.id.llIngredients);
        ivPizza = (ImageView) findViewById(R.id.ivPizza);
        ivFocus = (ImageView) findViewById(R.id.ivFocus);
        tvInstruction = (TextView) findViewById(R.id.tvInstruction);
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

        // Listen ivent drop on pizza
        ivPizza.setOnDragListener(onDragListenerIngredient);

        basicListIngredients = Ingredients.getIngredients();
        listIdAllImageView = new ArrayList<>();

        int sizeIngredient = (int) getResources().getDimension(R.dimen.make_pizza_size_ingredient_in_list);
        int paddingIngredient = (int) getResources().getDimension(R.dimen.make_pizza_padding_ingredient_in_list);
        // id for  ImageView get from resource
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

        // initialize to default value
        clearPizza();

        // restore if need
        if (savedInstanceState != null){
            listIngredientsLayers = savedInstanceState.getIntegerArrayList(STATE_LIST_INGREDIENTS);
            totalSum = savedInstanceState.getFloat(STATE_TOTAL_SUM);
            sbTotal.append(savedInstanceState.getString(STATE_TEXT_ORDER));
            hideChoosedIngredients();
            updatePizza();
        }
    }

    private void hideChoosedIngredients(){
        for (int id: listIngredientsLayers) {
            for (Ingredient ingredient: basicListIngredients) {
                if (ingredient.getPizzaImageResource() == id){
                    findViewById(ingredient.getListImageResource()).setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_pizza);
        item.setCheckable(true);
        item.setChecked(true);
    }

    /** Begin drag ingredient */
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


    /** Remove all ingredient from basic pizza end show full list */
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


    private void updatePizza(){
        Bitmap basis = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_basis);

        // Create canvas where will draw layers
        Bitmap result = Bitmap.createBitmap(basis.getWidth(), basis.getHeight(), basis.getConfig());
        Canvas canvas = new Canvas(result);

        // Draw basic layer - empty pizza
        canvas.drawBitmap(basis, 0, 0, null);

        // draw all ingredients
        for (int resource: listIngredientsLayers){
            Bitmap layer = BitmapFactory.decodeResource(getResources(), resource);
            canvas.drawBitmap(layer, 0, 0, null);
        }

        // Show bitmap to view
        ivPizza.setImageBitmap(result);

        if (sbTotal.length() == 0) {
            btnAdd.setVisibility(View.GONE);
            btnClear.setVisibility(View.GONE);
            tvInstruction.setVisibility(View.VISIBLE);
        } else {
            tvListIngredients.setText(sbTotal);
            tvInstruction.setVisibility(View.GONE);
            String strTotal = getResources().getString(R.string.total) + " = " +
                    Utils.toPrice(totalSum);
            tvTotalSum.setText(strTotal);
            btnAdd.setVisibility(View.VISIBLE);
            btnClear.setVisibility(View.VISIBLE);
        }
    }


    /** Add pizza to Basket as "custom dish" */
    private void addPizzaToBasket(){
        Dish pizza = new Dish();
        pizza.setName(getResources().getString(R.string.name_of_pizza));
        pizza.setDescription(sbTotal.toString());
        pizza.setPrice(totalSum);
        pizza.setKey(Const.KEY_DISH_MY_PIZZA);

        Basket.getInstance().addDish(pizza);
        String mes = getString(R.string.name_of_pizza) + " (" + Utils.toPrice(totalSum) + ") " +
                getString(R.string.added_to_basket);

        String sum = mes + "\n" + getResources().getString(R.string.total_sum) + ": " +
                Utils.toPrice(Basket.getInstance().getTotalSum());
        Snackbar.make(btnClear, sum, Snackbar.LENGTH_SHORT).show();
        clearPizza();
    }

    /** Ivents of drag */
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
                    // First ingredient
                    if (sbTotal.length() == 0){
                        sbTotal.append("\n" + Ingredients.getBasis().getName() + " " +
                                Utils.toPrice(Ingredients.getBasis().getPrice()));
                        totalSum = Ingredients.getBasis().getPrice();
                    }

                    // Hide in list and redraw pizza with new ingredient
                    ivCurrentIngredient.setVisibility(View.GONE);
                    Ingredient choosedIngredient = getIngredientWithId(ivCurrentIngredient.getId());
                    sbTotal.append("\n" + choosedIngredient.getName() + " " +
                            Utils.toPrice(choosedIngredient.getPrice()));
                    totalSum += choosedIngredient.getPrice();
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

    /** Use id for find needed ImageView (ingredient in list) */
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(STATE_LIST_INGREDIENTS, listIngredientsLayers);
        outState.putFloat(STATE_TOTAL_SUM, totalSum);
        outState.putString(STATE_TEXT_ORDER, sbTotal.toString());
    }

}

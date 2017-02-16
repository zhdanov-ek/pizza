package com.example.gek.pizza.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.helpers.Utils;

public class DishShowActivity extends BaseActivity implements View.OnClickListener{
    private TextView tvName, tvPrice, tvDescription;
    private ImageView ivPhoto;
    private Button btnAdd;
    private Button btnRemove;
    private Button btnEdit;
    private LinearLayout llCounter;
    private TextView tvCounter;
    private ImageView ivMinus, ivPlus;
    private Dish dishOpen;

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_SHOP){
            btnEdit.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
        } else {
            btnAdd.setEnabled(false);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_show);

        tvName = (TextView) findViewById(R.id.tvName);
        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnRemove = (Button) findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(this);
        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(this);

        llCounter = (LinearLayout) findViewById(R.id.llCounter);
        tvCounter = (TextView) findViewById(R.id.tvCounter);
        ivMinus = (ImageView) findViewById(R.id.ivMinus);
        ivMinus.setOnClickListener(this);
        ivPlus = (ImageView) findViewById(R.id.ivPlus);
        ivPlus.setOnClickListener(this);

        if (getIntent().hasExtra(Const.EXTRA_DISH)){
            dishOpen = getIntent().getParcelableExtra(Const.EXTRA_DISH);
            fillValues(dishOpen);

            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
            myToolbar.setTitle(dishOpen.getName());
            setSupportActionBar(myToolbar);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // смотрим в корзине в заказе ли это блюдо
        int countDishInOrder = Utils.findInBasket(dishOpen);
        if (countDishInOrder != 0) {
            btnAdd.setVisibility(View.GONE);
            llCounter.setVisibility(View.VISIBLE);
            tvCounter.setText(String.valueOf(countDishInOrder));
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            llCounter.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRemove:
                removeDish(dishOpen);
                break;
            case R.id.btnEdit:
                editDish(dishOpen);
                break;
            case R.id.btnAdd:
                addNewDish();
                break;
            case R.id.ivMinus:
                pressMinus();
                break;
            case R.id.ivPlus:
                pressPlus();
                break;
        }
    }


    /** Увеличиваем количество в заказе на 1 */
    private void pressPlus(){
        int count = Integer.parseInt(tvCounter.getText().toString()) + 1;
        tvCounter.setText(String.valueOf(count));
        Basket.getInstance().changeCount(dishOpen.getKey(), count);
    }

    /** Уменьшаем количество в заказе на 1 или удаляем заказ если 0 */
    private void pressMinus(){
        int count = Integer.parseInt(tvCounter.getText().toString()) - 1;
        Basket.getInstance().changeCount(dishOpen.getKey(), count);
        if (count == 0) {
            llCounter.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            tvCounter.setText(String.valueOf(count));
        }
    }


    /** Добавляем блюдо в корзину, заменяем кнопку на цифру с количеством */
    private void addNewDish(){
        Basket.getInstance().addDish(dishOpen);
        tvCounter.setText("1");
        btnAdd.setVisibility(View.GONE);
        llCounter.setVisibility(View.VISIBLE);
    }

    /** После удачного редактирования карточки обновляем инфу и тут */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == Const.REQUEST_EDIT_DISH) && (resultCode == RESULT_OK) && (data != null)) {
            Dish changedDish = data.getParcelableExtra(Const.EXTRA_DISH);
            dishOpen = changedDish;
            fillValues(changedDish);
        }
    }


    /** Заполнение полей активити */
    private void fillValues(Dish dish){
        if ((dish.getPhotoUrl() != null) && (dish.getPhotoUrl().length() > 0)){
            Glide.with(this)
                    .load(dish.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.dish_empty)
                    .into(ivPhoto);
        } else {
            ivPhoto.setImageResource(R.drawable.dish_empty);
        }
        // Не даем удалить блюдо если оно уже в архиве
        if (dish.getKeyGroup().contentEquals(Const.DISH_GROUP_VALUE_REMOVED)){
            btnRemove.setVisibility(View.GONE);
        }
        tvName.setText(dish.getName());
        tvPrice.setText(Utils.toPrice(dish.getPrice()));
        tvDescription.setText(dish.getDescription());
    }


    /** Открываем активити для редактирования текущего блюда */
    private void editDish(Dish dish){
        Intent editIntent = new Intent(this, DishEditActivity.class);
        editIntent.putExtra(Const.MODE, Const.MODE_EDIT);
        editIntent.putExtra(Const.EXTRA_DISH, dish);
        startActivityForResult(editIntent, Const.REQUEST_EDIT_DISH);
    }





    /** Перемещение блюда в архив путем присваивания ключа группы REMOVED */
    private void removeDish(final Dish dish){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove);
        builder.setIcon(R.drawable.ic_warning);
        String message = getResources().getString(R.string.confirm_remove_item);
        builder.setMessage(message + "\n" + dish.getName());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Заменяем ключ группы, после чего это блюдо не будет возможности выбрать в меню
                dish.setKeyGroup(Const.DISH_GROUP_VALUE_REMOVED);
                Const.db.child(Const.CHILD_DISHES).child(dish.getKey()).setValue(dish);
                finish();
            }
        });
        builder.show();
    }
}

package com.example.gek.pizza.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DishShowActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvName, tvPrice, tvDescription;
    private ImageView ivPhoto;
    private Button btnAdd;
    private Button btnRemove;
    private Button btnEdit;
    private Dish dishOpen;

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

        if (getIntent().hasExtra(Const.EXTRA_DISH)){
            dishOpen = getIntent().getParcelableExtra(Const.EXTRA_DISH);
            fillValues(dishOpen);
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
            case R.id.btnAdd:
                Basket.getInstance().addDish(dishOpen);
                int num = Basket.getInstance().orders.size();
                Toast.makeText(this, "Всего в заказе " + num, Toast.LENGTH_SHORT).show();
                //todo добавить блюдо в заказ, скрыть кнопку показать "- х +"
                break;
        }
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
        tvName.setText(dish.getName());
        tvPrice.setText(Float.toString(dish.getPrice()));
        tvDescription.setText(dish.getDescription());
    }
    /** Открываем активити для редактирования текущего блюда */
    private void editDish(Dish dish){
        Intent editIntent = new Intent(this, DishEditActivity.class);
        editIntent.putExtra(Const.MODE, Const.MODE_EDIT);
        editIntent.putExtra(Const.EXTRA_DISH, dish);
        startActivityForResult(editIntent, Const.REQUEST_EDIT_DISH);
    }


    /** Удаление блюда из базы и фото с хранилища */
    private void removeDish(Dish dish){
        final Dish removeItem = dish;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.remove);
        builder.setIcon(R.drawable.ic_warning);
        String message = getResources().getString(R.string.confirm_remove_item);
        builder.setMessage(message + "\n" + removeItem.getName());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Получаем ссылку на наше хранилище и удаляем фото по названию
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
                if (removeItem.getPhotoUrl().length() > 0){
                    storageRef.child(Const.DISHES_IMAGES_FOLDER).child(removeItem.getPhotoName()).delete();
                }

                // Получаем ссылку на базу данных и удаляем новость по ключу
                Const.db.child(Const.CHILD_DISHES).child(removeItem.getKey()).removeValue();
                finish();
            }
        });
        builder.show();
    }

}

package com.example.gek.pizza.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;

public class DishShowActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvName, tvPrice, tvDescription;
    private ImageView ivPhoto;
    private Button btnAdd;
    private Dish dish;

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


        if (getIntent().hasExtra(Const.EXTRA_DISH)){
            dish = getIntent().getParcelableExtra(Const.EXTRA_DISH);

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
    }

    @Override
    public void onClick(View view) {
        //todo добавить блюдо в заказ, скрыть кнопку показать "- х +"
    }
}

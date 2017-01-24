package com.example.gek.pizza.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.DishShowActivity;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;

import java.util.ArrayList;

/**
 * Адаптер формирующий список блюд
 */

public class DishesAdapter  extends RecyclerView.Adapter<DishesAdapter.ViewHolder>{
    private ArrayList<Dish> listDishes;
    private Context ctx;

    public DishesAdapter( Context ctx, ArrayList<Dish> listDishes) {
        this.listDishes = listDishes;
        this.ctx = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_dish, parent, false);
        DishesAdapter.ViewHolder viewHolder = new DishesAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Dish currentDish = listDishes.get(position);
        holder.tvName.setText(currentDish.getName());
        holder.tvPrice.setText(Float.toString(listDishes.get(position).getPrice()));
        if ((currentDish.getPhotoUrl() != null) && (currentDish.getPhotoUrl().length() > 0)){
            Glide.with(ctx)
                    .load(currentDish.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.dish_empty)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.dish_empty);
        }
    }

    @Override
    public int getItemCount() {
        return listDishes.size();
    }

    // Описываем кастомынй вьюхолдер, который будет хранить наши вьюайтемы
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView ivPhoto;
        private TextView tvName;
        private TextView tvPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent showDish = new Intent(ctx, DishShowActivity.class);
            showDish.putExtra(Const.EXTRA_DISH, listDishes.get(getAdapterPosition()));
            ctx.startActivity(showDish);
        }
    }
}

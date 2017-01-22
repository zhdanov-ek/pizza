package com.example.gek.pizza.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.MenuGroup;

import java.util.ArrayList;

/**
 * Адаптер для наполнения главного меню заказов
 */

public class MenuOrdersAdapter extends RecyclerView.Adapter<MenuOrdersAdapter.ViewHolder> {
    private ArrayList<MenuGroup> menuList;
    private Context ctx;

    public MenuOrdersAdapter( Context ctx, ArrayList<MenuGroup> menuList) {
        this.menuList = menuList;
        this.ctx = ctx;
    }

    // Создаем вью которые заполнят экран и будут обновляться данными при прокрутке
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_menu_orders, parent, false);
        MenuOrdersAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MenuGroup menuGroup = menuList.get(position);
        holder.tvName.setText(menuGroup.getName());
        if ((menuGroup.getPhotoUrl() != null) && (menuGroup.getPhotoUrl().length() > 0)){
            Glide.with(ctx)
                    .load(menuGroup.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.news_icon)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.news_icon);
        }
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName;
        private ImageView ivPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
        }
    }
}

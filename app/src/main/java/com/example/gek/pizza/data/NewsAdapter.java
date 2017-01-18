package com.example.gek.pizza.data;

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

import java.util.ArrayList;

/**
 * Адаптер для формирование списка новостей
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>{

    private ArrayList<News> listNews;
    private Context ctx;

    public NewsAdapter(Context ctx, ArrayList<News> listNews){
        this.listNews = listNews;
        this.ctx = ctx;
    }


    // Создаем вью которые заполнят экран и будут обновляться данными при прокрутке
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_news, parent, false);
        NewsAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // Заносим значения в наши вью
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final News currentNews = listNews.get(position);
        holder.tvTitle.setText(currentNews.getTitle());
        if ((currentNews.getPhotoUrl() != null) && (currentNews.getPhotoUrl().length() > 0)){
            Glide.with(ctx)
                    .load(currentNews.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.news_icon)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.news_icon);
        }

    }

    @Override
    public int getItemCount() {
        return listNews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvTitle;
        private ImageView ivPhoto;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
        }
    }
}

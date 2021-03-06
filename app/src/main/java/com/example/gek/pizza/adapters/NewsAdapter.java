package com.example.gek.pizza.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.NewsEditActivity;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.News;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * Adapter for list of news
 */

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder>{
    private ArrayList<News> listNews;
    private Context ctx;

    public NewsAdapter(Context ctx, ArrayList<News> listNews){
        this.listNews = listNews;
        this.ctx = ctx;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_news, parent, false);
        NewsAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final News currentNews = listNews.get(position);
        holder.tvTitle.setText(currentNews.getTitle());
        holder.tvDescription.setText(currentNews.getDescription());
        if (currentNews.getPhotoUrl().length() > 0){
            Glide.with(ctx)
                    .load(currentNews.getPhotoUrl())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.news_empty)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.news_empty);
        }
    }

    @Override
    public int getItemCount() {
        return listNews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvTitle;
        private ImageView ivPhoto;
        private TextView tvDescription;
        private LinearLayout llDescription;
        private Button btnEdit;
        private Button btnRemove;

        private ViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            llDescription = (LinearLayout) itemView.findViewById(R.id.llDescription);
            btnEdit = (Button) itemView.findViewById(R.id.btnEdit);
            btnRemove = (Button) itemView.findViewById(R.id.btnRemove);

            itemView.setOnClickListener(this);
            btnRemove.setOnClickListener(this);
            btnEdit.setOnClickListener(this);

            int state = Connection.getInstance().getCurrentAuthStatus();
            if (state == Const.AUTH_SHOP){
                btnRemove.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.VISIBLE);
            } else {
                btnRemove.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnEdit:
                    News chooseNews = listNews.get(getAdapterPosition());
                    Intent openIntent = new Intent(ctx, NewsEditActivity.class);
                    openIntent.putExtra(Const.MODE, Const.MODE_EDIT);
                    openIntent.putExtra(Const.EXTRA_NEWS, chooseNews);
                    ctx.startActivity(openIntent);
                    break;
                case R.id.btnRemove:
                    removeNews(listNews.get(getAdapterPosition()));
                    break;
                default:
                    if (llDescription.getVisibility() == View.GONE){
                        llDescription.setVisibility(View.VISIBLE);
                    } else {
                        llDescription.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    }

    /** Remove news from DB and image from storage */
    private void removeNews(News news){
        final News removeItem = news;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.remove);
        builder.setIcon(R.drawable.ic_warning);
        String message = ctx.getResources().getString(R.string.confirm_remove_item);
        builder.setMessage(message + "\n" + removeItem.getTitle());
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // remove photo
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(Const.STORAGE);
                if (removeItem.getPhotoUrl().length() > 0){
                    storageRef.child(Const.NEWS_IMAGES_FOLDER).child(removeItem.getPhotoName()).delete();
                }

                // remove item
                Const.db.child(Const.CHILD_NEWS).child(removeItem.getKey()).removeValue();
            }
        });
        builder.show();
    }
}

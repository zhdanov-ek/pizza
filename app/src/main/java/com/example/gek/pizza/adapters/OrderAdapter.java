package com.example.gek.pizza.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Order;
import com.example.gek.pizza.helpers.Utils;


/**
 * Адаптер формирующий список заказа
 */

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{
    private Context ctx;

    public OrderAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_order_dish, parent, false);
        OrderAdapter.ViewHolder viewHolder = new OrderAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Order order = Basket.getInstance().orders.get(position);
        if (order.getPhotoUrlDish().length() > 0){
            Glide.with(ctx)
                    .load(order.getPhotoUrlDish())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.dish_empty)
                    .into(holder.ivPhoto);
        } else {
            holder.ivPhoto.setImageResource(R.drawable.dish_empty);
        }

        holder.tvName.setText(order.getNameDish());
        holder.tvPrice.setText(Utils.toPrice(order.getPriceDish()));
        holder.tvCounter.setText(String.valueOf(order.getCount()));
        holder.tvSum.setText(Utils.toPrice(order.getSum()));
    }

    @Override
    public int getItemCount() {
        return Basket.getInstance().orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvName;
        private ImageView ivPhoto;
        private TextView tvPrice;

        private LinearLayout llCounter;
        private TextView tvCounter;
        private ImageView ivMinus;
        private ImageView ivPlus;

        private TextView tvSum;
        private ImageView ivClear;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            llCounter = (LinearLayout) itemView.findViewById(R.id.llCounter);
            llCounter.setVisibility(View.VISIBLE);
            tvCounter = (TextView) itemView.findViewById(R.id.tvCounter);
            ivMinus = (ImageView) itemView.findViewById(R.id.ivMinus);
            ivMinus.setOnClickListener(this);
            ivPlus = (ImageView) itemView.findViewById(R.id.ivPlus);
            ivPlus.setOnClickListener(this);
            tvSum = (TextView) itemView.findViewById(R.id.tvSum);
            ivClear = (ImageView) itemView.findViewById(R.id.ivClear);
            ivClear.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            switch (view.getId()){
                case R.id.ivClear:
                    removeDish(position);
                    break;
                case R.id.ivMinus:
                    pressMinus(position);
                    break;
                case R.id.ivPlus:
                    pressPlus(position);
                    break;
            }
        }

        /** Удаляем полностью блюдо с заказа */
        private void removeDish(int position){
            Basket.getInstance().orders.remove(position);
            if (Basket.getInstance().orders.size() == 0) {
                //todo закрыть окно или лучше показать, что корзина пустая
                notifyItemRemoved(position);
            } else {
                notifyItemRemoved(position);
            }
        }

        /** Увеличиваем количество в заказе на 1 */
        private void pressPlus(int position){
            int count = Integer.parseInt(tvCounter.getText().toString()) + 1;
            Basket.getInstance().changeCount(Basket.getInstance().orders.get(position).getKeyDish(), count);
            tvCounter.setText(String.valueOf(count));
            tvSum.setText(Utils.toPrice(Basket.getInstance().orders.get(position).getSum()));

        }

        /** Уменьшаем количество в заказе на 1 (до 1 минимум) */
        private void pressMinus(int position){
            int count = Integer.parseInt(tvCounter.getText().toString());
            if (count > 1) {
                count--;
                Basket.getInstance().changeCount(Basket.getInstance().orders.get(position).getKeyDish(), count);
                tvCounter.setText(String.valueOf(count));
                tvSum.setText(Utils.toPrice(Basket.getInstance().orders.get(position).getSum()));
            }


        }
    }
}

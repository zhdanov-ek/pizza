package com.example.gek.pizza.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.helpers.Utils;

import java.util.ArrayList;

/**
 * Адаптер отображающий заказы на доставку (новые, готовка и доставка)
 */

public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.ViewHolder>{

    private ArrayList<Delivery> listDeliveries;
    private Context ctx;
    private int statusDeliveries;

    public DeliveriesAdapter(ArrayList<Delivery> listDeliveries, Context ctx, int statusDeliveries) {
        this.listDeliveries = listDeliveries;
        this.ctx = ctx;
        // нужно для понимания какие действия выполнять по нажатию на кнопки
        this.statusDeliveries = statusDeliveries;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_delivery, parent, false);
        DeliveriesAdapter.ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Delivery delivery = listDeliveries.get(position);
        holder.tvNameClient.setText(delivery.getNameClient());
        holder.tvPhoneClient.setText(delivery.getPhoneClient());
        holder.tvAddressClient.setText(delivery.getAddressClient());
        holder.tvTotalSum.setText(Utils.toPrice(delivery.getTotalSum()));
        //todo write other fields
    }

    @Override
    public int getItemCount() {
        return listDeliveries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView tvNameClient;
        private TextView tvPhoneClient;
        private TextView tvAddressClient;
        private TextView tvCommentClient;
        private TextView tvTotalSum;
        private TextView tvDetails;
        private TextView tvCommentShop;
        private LinearLayout llDetails;
        private Button btnPositive, btnNegative;
        private ImageView ivExpand;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNameClient = (TextView) itemView.findViewById(R.id.tvNameClient);
            tvPhoneClient = (TextView) itemView.findViewById(R.id.tvPhoneClient);
            tvAddressClient = (TextView) itemView.findViewById(R.id.tvAddressClient);
            tvCommentClient = (TextView) itemView.findViewById(R.id.tvCommentClient);
            tvTotalSum = (TextView) itemView.findViewById(R.id.tvTotalSum);
            tvDetails = (TextView) itemView.findViewById(R.id.tvDetails);
            tvCommentShop = (TextView) itemView.findViewById(R.id.tvCommentShop);
            llDetails = (LinearLayout) itemView.findViewById(R.id.llDetails);
            btnPositive = (Button) itemView.findViewById(R.id.btnPositive);
            btnNegative = (Button) itemView.findViewById(R.id.btnNegative);
            ivExpand = (ImageView) itemView.findViewById(R.id.ivExpand);

            btnPositive.setOnClickListener(this);
            btnNegative.setOnClickListener(this);
            ivExpand.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ivExpand:
                    if (llDetails.getVisibility() == View.GONE) {
                        llDetails.setVisibility(View.VISIBLE);
                    } else {
                        llDetails.setVisibility(View.GONE);
                    }
                    break;
                case R.id.btnPositive:
                    break;
                case R.id.btnNegative:
                    break;
                default:
                    break;
            }
        }
    }

}

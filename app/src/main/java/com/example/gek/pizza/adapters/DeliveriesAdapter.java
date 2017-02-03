package com.example.gek.pizza.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.Util;
import com.example.gek.pizza.R;
import com.example.gek.pizza.activities.DeliveriesActivity;
import com.example.gek.pizza.data.AllDishes;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

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
        if (delivery.getCommentClient().isEmpty()){
            holder.tvCommentClient.setVisibility(View.GONE);
        } else {
            holder.tvCommentClient.setText(delivery.getCommentClient());
        }
        if (delivery.getCommentShop() == null) {
            holder.tvCommentShop.setVisibility(View.GONE);
        } else {
            holder.tvCommentShop.setText(delivery.getCommentShop());
        }

        // По ключу блюда находим его в списке и получаем полную инфу. Берем кол-во и формируем строку
        String details = "";
        for (int i = 0; i < delivery.getNumbersDishes().size(); i++) {
            Dish nextDish = AllDishes.getInstance().getDish(delivery.getKeysDishes().get(i));
            details += Utils.makeOrderString(nextDish, delivery.getNumbersDishes().get(i)) +"\n";
        }
        holder.tvDetails.setText(details);

        switch (statusDeliveries){
            case Const.DELIVERY_STATUS_NEW:
                holder.btnPositive.setText(R.string.btn_cook);
                holder.btnNegative.setText(R.string.btn_cancel);
                break;
            case Const.DELIVERY_STATUS_COOK:
                holder.btnPositive.setText(R.string.btn_transit);
                holder.btnNegative.setText(R.string.btn_cancel);
                break;
            case Const.DELIVERY_STATUS_TRANSIT:
                holder.btnPositive.setText(R.string.btn_paid);
                holder.btnNegative.setText(R.string.btn_failure);
                break;
        }
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
        private ImageView ivShopComment;

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
            ivShopComment = (ImageView) itemView.findViewById(R.id.ivShopComment);

            btnPositive.setOnClickListener(positiveListener);
            btnNegative.setOnClickListener(cancelDelivery);
            ivExpand.setOnClickListener(this);
            ivShopComment.setOnClickListener(editShopComment);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ivExpand:
                    if (llDetails.getVisibility() == View.GONE) {
                        llDetails.setVisibility(View.VISIBLE);
                        ivExpand.setImageResource(R.drawable.ic_expand_more);
                    } else {
                        llDetails.setVisibility(View.GONE);
                        ivExpand.setImageResource(R.drawable.ic_expand_less);
                    }
                    break;
                default:
                    break;
            }
        }

        /** Отклонение доставки: запись переносится в архив и перед этим вносится комментарий */
        private View.OnClickListener cancelDelivery = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle(R.string.dialog_title_delivery_reject);
                builder.setIcon(R.drawable.ic_warning);

                // подгружаем вью в базовый диалог и наполняем его данными
                View customPart = ((AppCompatActivity) ctx).getLayoutInflater().inflate(R.layout.dialog_reason, null);
                builder.setView(customPart);
                final EditText etInput = (EditText) customPart.findViewById(R.id.etInput);
                etInput.setText(listDeliveries.get(getAdapterPosition()).getCommentShop());

                // Вносим комментарий заведения в доставку и перемещаем ее в архив
                builder.setPositiveButton(R.string.btn_to_archive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Delivery delivery = listDeliveries.get(getAdapterPosition());
                        delivery.setCommentShop(etInput.getText().toString());
                        db.child(Const.CHILD_DELIVERIES_ARCHIVE).child(delivery.getKey()).setValue(delivery);

                        String status = Const.CHILD_DELIVERIES_NEW;
                        // определяем в каком разделе находится объект сейчас и удаляем его
                        switch (statusDeliveries){
                            case Const.DELIVERY_STATUS_NEW:
                                status = Const.CHILD_DELIVERIES_NEW;
                                break;
                            case Const.DELIVERY_STATUS_COOK:
                                status = Const.CHILD_DELIVERIES_COOKING;
                                break;
                            case Const.DELIVERY_STATUS_TRANSIT:
                                status = Const.CHILD_DELIVERIES_TRANSIT;
                                break;
                        }
                        db.child(status).child(delivery.getKey()).removeValue();
                    }
                });

                builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.show();
            }
        };



        /** Добавление (изменение) комментария заведения */
        private View.OnClickListener editShopComment = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Enter comment");
                builder.setIcon(R.drawable.ic_comment);

                // Добавляем вью для ввода в стандартный диалог
                final EditText etCommentShop = new EditText(ctx);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                etCommentShop.setLayoutParams(lp);
                builder.setView(etCommentShop);

                if (tvCommentShop.getText().length() != 0){
                    etCommentShop.setText(tvCommentShop.getText());
                }

                // Кнопки с обработчиками
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Delivery delivery = listDeliveries.get(getAdapterPosition());
                        delivery.setCommentShop(etCommentShop.getText().toString());
                        String status;

                        // определяем в каком разделе находится объект и перезаписываем его
                        switch (statusDeliveries){
                            case Const.DELIVERY_STATUS_NEW:
                                status = Const.CHILD_DELIVERIES_NEW;
                                break;
                            case Const.DELIVERY_STATUS_COOK:
                                status = Const.CHILD_DELIVERIES_COOKING;
                                break;
                            case Const.DELIVERY_STATUS_TRANSIT:
                                status = Const.CHILD_DELIVERIES_TRANSIT;
                                break;
                            default:
                                status = Const.CHILD_DELIVERIES_NEW;
                                break;
                        }
                        db.child(status).child(delivery.getKey()).setValue(delivery);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.show();
            }
        };


        /** При нажатии на позитивную кнопку перемещаем доставу в следующую группу */
        private View.OnClickListener positiveListener =  new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Используя ключ доставки переносим ее другую категорию
                String keyDelivery = listDeliveries.get(getAdapterPosition()).getKey();
                String text = listDeliveries.get(getAdapterPosition()).getNameClient();
                switch (statusDeliveries){
                    case Const.DELIVERY_STATUS_NEW:
                        db.child(Const.CHILD_DELIVERIES_NEW)
                                .child(keyDelivery)
                                .removeValue();
                        db.child(Const.CHILD_DELIVERIES_COOKING)
                                .child(keyDelivery)
                                .setValue(listDeliveries.get(getAdapterPosition()));
                        Toast.makeText(ctx, text + " Send to cook", Toast.LENGTH_SHORT).show();
                        break;

                    case Const.DELIVERY_STATUS_COOK:
                        db.child(Const.CHILD_DELIVERIES_COOKING)
                                .child(keyDelivery)
                                .removeValue();
                        db.child(Const.CHILD_DELIVERIES_TRANSIT)
                                .child(keyDelivery)
                                .setValue(listDeliveries.get(getAdapterPosition()));
                        Toast.makeText(ctx, text + "Send to transit", Toast.LENGTH_SHORT).show();
                        break;

                    case Const.DELIVERY_STATUS_TRANSIT:
                        db.child(Const.CHILD_DELIVERIES_TRANSIT)
                                .child(keyDelivery)
                                .removeValue();
                        db.child(Const.CHILD_DELIVERIES_ARCHIVE)
                                .child(keyDelivery)
                                .setValue(listDeliveries.get(getAdapterPosition()));
                        Toast.makeText(ctx, text + "Send to archive", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };
    }


}

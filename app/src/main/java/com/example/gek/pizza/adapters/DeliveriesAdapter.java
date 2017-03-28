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
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.AllDishes;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.StateLastDelivery;
import com.example.gek.pizza.helpers.Utils;

import java.util.ArrayList;
import java.util.Date;

import static com.example.gek.pizza.data.Const.db;

/**
 * Adapter create data for list of deliveries (new, cook, transfer and archive)
 * If delivery change state need change state in user folder
 */


public class DeliveriesAdapter extends RecyclerView.Adapter<DeliveriesAdapter.ViewHolder>{
    private ArrayList<Delivery> listDeliveries;
    private Context ctx;
    private String statusDeliveries;

    public DeliveriesAdapter(ArrayList<Delivery> listDeliveries, Context ctx, String statusDeliveries) {
        this.listDeliveries = listDeliveries;
        this.ctx = ctx;
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
        holder.tvTotalSum.setText(Utils.toPrice(delivery.getTotalSum()) + "\n" +
                Utils.formatDate(delivery.getDateNew()));
        if (delivery.getCommentClient().isEmpty()){
            holder.tvCommentClient.setVisibility(View.GONE);
        } else {
            holder.tvCommentClient.setText(delivery.getCommentClient());
        }
        if ((delivery.getCommentShop() == null) || (delivery.getCommentShop().length() == 0)) {
            holder.tvCommentShop.setVisibility(View.GONE);
        } else {
            holder.tvCommentShop.setText(delivery.getCommentShop());
        }
        holder.tvTime.setText(Utils.getTimeHistoryDelivery(delivery, ctx));
        holder.tvEmail.setText(delivery.getUserEmail());
        String details = "";
        if (delivery.getTextMyPizza() != null){
            for (int j = 0; j < delivery.getTextMyPizza().size(); j++) {
                details += ctx.getResources().getString(R.string.name_of_pizza) + " x " +
                        delivery.getNumbersMyPizza().get(j);
                details += delivery.getTextMyPizza().get(j) + "\n\n";
            }
        }

        // Make string line for all dishes
        if (delivery.getKeysDishes() != null) {
            for (int i = 0; i < delivery.getNumbersDishes().size(); i++) {
                Dish nextDish = AllDishes.getInstance().getDish(delivery.getKeysDishes().get(i));
                details += Utils.makeOrderString(nextDish, delivery.getNumbersDishes().get(i), ctx);
                if (i < delivery.getNumbersDishes().size()){
                    details += "\n";
                }
            }
        }

        holder.tvDetails.setText(details);

        // Set text for buttons
        switch (statusDeliveries){
            case Const.CHILD_DELIVERIES_NEW:
                holder.btnPositive.setText(R.string.btn_cook);
                holder.btnNegative.setText(R.string.btn_cancel);
                break;
            case Const.CHILD_DELIVERIES_COOKING:
                holder.btnPositive.setText(R.string.btn_transit);
                holder.btnNegative.setText(R.string.btn_cancel);
                break;
            case Const.CHILD_DELIVERIES_TRANSPORT:
                holder.btnPositive.setText(R.string.btn_paid);
                holder.btnNegative.setText(R.string.btn_failure);
                break;
            case Const.CHILD_DELIVERIES_ARCHIVE:
                holder.btnPositive.setText(R.string.btn_restore);
                holder.btnNegative.setText(R.string.remove);
                if (!delivery.getPaid()){
                    holder.ivFail.setVisibility(View.VISIBLE);
                }
                break;
        }
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
        private TextView tvEmail;
        private TextView tvCommentShop;
        private TextView tvTime;
        private LinearLayout llDetails;
        private Button btnPositive, btnNegative;
        private ImageView ivExpand;
        private ImageView ivShopComment;
        private ImageView ivFail;

        private ViewHolder(View itemView) {
            super(itemView);
            tvNameClient = (TextView) itemView.findViewById(R.id.tvNameClient);
            tvPhoneClient = (TextView) itemView.findViewById(R.id.tvPhoneClient);
            tvAddressClient = (TextView) itemView.findViewById(R.id.tvAddressClient);
            tvCommentClient = (TextView) itemView.findViewById(R.id.tvCommentClient);
            tvTotalSum = (TextView) itemView.findViewById(R.id.tvTotalSum);
            tvDetails = (TextView) itemView.findViewById(R.id.tvDishes);
            tvEmail = (TextView) itemView.findViewById(R.id.tvUserEmail);
            tvCommentShop = (TextView) itemView.findViewById(R.id.tvCommentShop);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            llDetails = (LinearLayout) itemView.findViewById(R.id.llDetails);
            btnPositive = (Button) itemView.findViewById(R.id.btnPositive);
            btnNegative = (Button) itemView.findViewById(R.id.btnNegative);
            ivExpand = (ImageView) itemView.findViewById(R.id.ivExpand);
            ivShopComment = (ImageView) itemView.findViewById(R.id.ivShopComment);
            ivFail = (ImageView) itemView.findViewById(R.id.ivFail);

            btnPositive.setOnClickListener(this);
            btnNegative.setOnClickListener(this);
            ivExpand.setOnClickListener(this);
            ivShopComment.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ivExpand:
                    doExpand();
                    break;
                case R.id.ivShopComment:
                    editShopComment();
                    break;
                case R.id.btnPositive:
                    pressPositive();
                    break;
                case R.id.btnNegative:
                    pressNegative();
                    break;
            }

        }

        /** Expand details  */
        private void doExpand() {
            if (llDetails.getVisibility() == View.GONE) {
                llDetails.setVisibility(View.VISIBLE);
                ivExpand.setImageResource(R.drawable.ic_expand_more);
            } else {
                llDetails.setVisibility(View.GONE);
                ivExpand.setImageResource(R.drawable.ic_expand_less);
            }
        }


        /** Add (edit) comment from shop */
        private void editShopComment(){
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(R.string.dialog_title_edit_comment);
            builder.setIcon(R.drawable.ic_comment);

            // Add view in basic dialog
            final EditText etCommentShop = new EditText(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            etCommentShop.setLayoutParams(lp);
            builder.setView(etCommentShop);

            if (tvCommentShop.getText().length() != 0){
                etCommentShop.setText(tvCommentShop.getText());
            }

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Delivery delivery = listDeliveries.get(getAdapterPosition());
                    delivery.setCommentShop(etCommentShop.getText().toString());
                    db.child(statusDeliveries).child(delivery.getKey()).setValue(delivery);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            builder.show();
        }

        /**
         * Reject delivery
         * Delivery moved to archive and append comment.
         * Later change status in user folder
         */
        private void pressNegative(){
            final Delivery delivery = listDeliveries.get(getAdapterPosition());
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setIcon(R.drawable.ic_warning);
            builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            // If item in archive - remove. Else item moved to archive
            if (statusDeliveries.contentEquals(Const.CHILD_DELIVERIES_ARCHIVE)){
                builder.setTitle(R.string.dialog_title_delivery_remove);
                builder.setMessage(R.string.confirm_remove_item);
                builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.child(statusDeliveries).child(delivery.getKey()).removeValue();

                        // change state in user folder
                        db.child(Const.CHILD_USERS)
                                .child(listDeliveries.get(getAdapterPosition()).getUserId())
                                .child(Const.CHILD_USER_DELIVERY_STATE)
                                .setValue(null);
                        Toast.makeText(ctx, R.string.mes_removed, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                builder.setTitle(R.string.dialog_title_delivery_reject);
                delivery.setDateArchive(new Date());
                // Load View in basic dialog and fill his the data
                View customPart = ((AppCompatActivity) ctx).getLayoutInflater().inflate(R.layout.dialog_reason, null);
                builder.setView(customPart);
                final EditText etInput = (EditText) customPart.findViewById(R.id.etInput);
                etInput.setText(listDeliveries.get(getAdapterPosition()).getCommentShop());

                // Add comment of shop and move to archive
                builder.setPositiveButton(R.string.btn_to_archive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delivery.setCommentShop(etInput.getText().toString());
                        delivery.setPaid(false);
                        db.child(Const.CHILD_DELIVERIES_ARCHIVE).child(delivery.getKey()).setValue(delivery);
                        db.child(statusDeliveries).child(delivery.getKey()).removeValue();

                        // change state in user folder
                        StateLastDelivery stateLastDelivery = new StateLastDelivery();
                        stateLastDelivery.setDeliveryId(delivery.getKey());
                        stateLastDelivery.setDeliveryState(Const.DELIVERY_STATE_ARCHIVE);
                        db.child(Const.CHILD_USERS)
                                .child(listDeliveries.get(getAdapterPosition()).getUserId())
                                .child(Const.CHILD_USER_DELIVERY_STATE)
                                .setValue(stateLastDelivery);
                        Toast.makeText(ctx, R.string.mes_pass_archive, Toast.LENGTH_LONG).show();
                    }
                });
            }
            builder.show();
        }


        /**
         *  Move to next group or restore from archive
         *  After operations make change in user folder
         *  */
        private void pressPositive(){
            // Используя ключ доставки переносим ее в другую категорию и меняем состояния заказа в юзерской папке
            String keyDelivery = listDeliveries.get(getAdapterPosition()).getKey();
            StateLastDelivery stateLastDelivery = new StateLastDelivery();
            stateLastDelivery.setDeliveryId(keyDelivery);
            switch (statusDeliveries){
                case Const.CHILD_DELIVERIES_NEW:
                    listDeliveries.get(getAdapterPosition()).setDateCooking(new Date());
                    db.child(Const.CHILD_DELIVERIES_NEW)
                            .child(keyDelivery)
                            .removeValue();
                    db.child(Const.CHILD_DELIVERIES_COOKING)
                            .child(keyDelivery)
                            .setValue(listDeliveries.get(getAdapterPosition()));
                    Toast.makeText(ctx, R.string.mes_pass_kitchen, Toast.LENGTH_SHORT).show();
                    // change state in user folder
                    stateLastDelivery.setDeliveryState(Const.DELIVERY_STATE_COOKING);
                    db.child(Const.CHILD_USERS)
                            .child(listDeliveries.get(getAdapterPosition()).getUserId())
                            .child(Const.CHILD_USER_DELIVERY_STATE)
                            .setValue(stateLastDelivery);
                    break;

                case Const.CHILD_DELIVERIES_COOKING:
                    listDeliveries.get(getAdapterPosition()).setDateTransport(new Date());
                    db.child(Const.CHILD_DELIVERIES_COOKING)
                            .child(keyDelivery)
                            .removeValue();
                    db.child(Const.CHILD_DELIVERIES_TRANSPORT)
                            .child(keyDelivery)
                            .setValue(listDeliveries.get(getAdapterPosition()));
                    Toast.makeText(ctx, R.string.mes_pass_courier, Toast.LENGTH_SHORT).show();
                    // change state in user folder
                    stateLastDelivery.setDeliveryState(Const.DELIVERY_STATE_TRANSPORT);
                    db.child(Const.CHILD_USERS)
                            .child(listDeliveries.get(getAdapterPosition()).getUserId())
                            .child(Const.CHILD_USER_DELIVERY_STATE)
                            .setValue(stateLastDelivery);
                    break;

                case Const.CHILD_DELIVERIES_TRANSPORT:
                    listDeliveries.get(getAdapterPosition()).setDateArchive(new Date());
                    listDeliveries.get(getAdapterPosition()).setPaid(true);
                    db.child(Const.CHILD_DELIVERIES_TRANSPORT)
                            .child(keyDelivery)
                            .removeValue();
                    db.child(Const.CHILD_DELIVERIES_ARCHIVE)
                            .child(keyDelivery)
                            .setValue(listDeliveries.get(getAdapterPosition()));
                    Toast.makeText(ctx, R.string.mes_pass_archive, Toast.LENGTH_LONG).show();
                    // change state in user folder
                    stateLastDelivery.setDeliveryState(Const.DELIVERY_STATE_ARCHIVE);
                    db.child(Const.CHILD_USERS)
                            .child(listDeliveries.get(getAdapterPosition()).getUserId())
                            .child(Const.CHILD_USER_DELIVERY_STATE)
                            .setValue(stateLastDelivery);
                    break;

                case Const.CHILD_DELIVERIES_ARCHIVE:
                    // if restore item from archive need purge old date
                    listDeliveries.get(getAdapterPosition()).setDateCooking(null);
                    listDeliveries.get(getAdapterPosition()).setDateTransport(null);
                    listDeliveries.get(getAdapterPosition()).setDateArchive(null);
                    listDeliveries.get(getAdapterPosition()).setPaid(false);
                    db.child(Const.CHILD_DELIVERIES_ARCHIVE)
                            .child(keyDelivery)
                            .removeValue();
                    db.child(Const.CHILD_DELIVERIES_NEW)
                            .child(keyDelivery)
                            .setValue(listDeliveries.get(getAdapterPosition()));
                    Toast.makeText(ctx, R.string.mes_restored, Toast.LENGTH_LONG).show();
                    // change state in user folder
                    stateLastDelivery.setDeliveryState(Const.DELIVERY_STATE_NEW);
                    db.child(Const.CHILD_USERS)
                            .child(listDeliveries.get(getAdapterPosition()).getUserId())
                            .child(Const.CHILD_USER_DELIVERY_STATE)
                            .setValue(stateLastDelivery);
            }
        }
    }
}

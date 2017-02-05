package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import static com.example.gek.pizza.data.Const.db;

public class DeliveryStatus extends AppCompatActivity {

    LinearLayout llContainer;
    TextView tvStep1Num, tvStep2Num, tvStep3Num, tvStep4Num;
    TextView tvStep1Title, tvStep2Title, tvStep3Title, tvStep4Title;
    TextView tvStep1Description, tvStep2Description, tvStep3Description, tvStep4Description;
    ImageView ivStep1, ivStep2, ivStep3, ivStep4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);
        findAllView();

        final ArrayList<Delivery> list = new ArrayList<>();
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    list.add(child.getValue(Delivery.class));
                }

                if (! list.isEmpty()){
                    int num = new Random().nextInt(list.size());
                    Log.d("11111111111", "onDataChange: list.size = " + list.size() + " num = " + num);
                    if (!deliveryClosed(list.get(num))) {
                        showStateDelivery(list.get(num));
                    }


                } else {
                    llContainer.setVisibility(View.GONE);
                    Toast.makeText(getBaseContext(), "In folder no Deliveries", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        db.child(getDeliveryPath()).addListenerForSingleValueEvent(listener);


    }

    /** Fill screen from new data of Delivery. */
    private void showStateDelivery(Delivery d){
        String mes = "";
        // State 3 (Transport)
        if ((d.getDateTransport() != null) && (d.getDateArchive() ==  null)){
            ivStep3.setImageResource(R.drawable.step_circle_normal);
            if (d.getDateArchive() != null){
                tvStep3Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step3_finish)
                        + " " + Utils.formatDate(d.getDateArchive());
                tvStep3Description.setText(mes);
            } else {
                tvStep3Num.setText("3");
                mes = getResources().getString(R.string.delivery_state_description_step3_start)
                        + " " + Utils.formatDate(d.getDateTransport());
                tvStep3Description.setText(mes);
            }
        } else {
            tvStep3Title.setText(R.string.delivery_state_title_step3);
            ivStep3.setImageResource(R.drawable.step_circle_disable);
            tvStep3Num.setText("3");
        }

        // State 2 (Cooking)
        if ((d.getDateCooking() != null) && (d.getDateArchive() ==  null)){
            ivStep2.setImageResource(R.drawable.step_circle_normal);
            if (d.getDateTransport() != null){
                tvStep2Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step2_finish)
                        + " " +  Utils.formatDate(d.getDateTransport());
                tvStep2Description.setText(mes);
            } else {
                tvStep2Num.setText("2");
                mes = getResources().getString(R.string.delivery_state_description_step2_start)
                        + " " + Utils.formatDate(d.getDateCooking());
                tvStep2Description.setText(mes);
            }
        } else {
            tvStep2Title.setText(R.string.delivery_state_title_step2);
            ivStep2.setImageResource(R.drawable.step_circle_disable);
            tvStep2Num.setText("2");
        }

        // State 1 (Receive)
        if ((d.getDateNew() != null) && (d.getDateArchive() == null)){
            if (d.getDateCooking() != null) {
                tvStep1Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step1_finish) +
                        " (" + Utils.formatDate(d.getDateNew()) + ")";
                tvStep1Description.setText(mes);
            } else {
                if (d.getDateArchive() != null) {
                    tvStep1Num.setText("+");
                    tvStep1Description.setText(R.string.delivery_state_title_step1_fail);
                }
                ivStep1.setImageResource(R.drawable.step_circle_disable);
                tvStep1Description.setText(R.string.delivery_state_description_step1_wait);
                tvStep1Num.setText("1");
            }

        } else {
            if (d.getDateArchive() == null){
                llContainer.setVisibility(View.GONE);
            }
        }

    }

    private Boolean deliveryClosed(Delivery d) {
        Boolean result = false;
        String mes = "";
        if (d.getDateArchive() != null) {
            tvStep4Title.setText(R.string.delivery_state_title_step4_finish);
            if (d.getPaid()) {
                ivStep4.setImageResource(R.drawable.step_circle_normal);
                tvStep4Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step4_paid) +
                        " (" + Utils.formatDate(d.getDateArchive()) + ")";
                tvStep4Description.setText(mes);
            } else {
                // Отклонена доставка
                result = true;
                ivStep4.setImageResource(R.drawable.step_circle_fail);
                tvStep4Num.setText("-");
                mes = getResources().getString(R.string.delivery_state_description_step4_fail)
                        + "\n" + d.getCommentShop();
                tvStep4Description.setText(mes);
                if (d.getDateTransport() != null) {
                    ivStep3.setImageResource(R.drawable.step_circle_fail);
                    tvStep3Num.setText("-");
                    ivStep2.setImageResource(R.drawable.step_circle_normal);
                    tvStep2Num.setText("+");
                    ivStep1.setImageResource(R.drawable.step_circle_normal);
                    tvStep1Num.setText("+");
                } else if (d.getDateCooking() != null) {
                    ivStep2.setImageResource(R.drawable.step_circle_fail);
                    tvStep2Num.setText("-");
                    ivStep1.setImageResource(R.drawable.step_circle_normal);
                    tvStep1Num.setText("+");
                } else {
                    tvStep1Title.setText(getResources().getString(R.string.delivery_state_title_step1));
                    tvStep1Description.setText(Utils.formatDate(d.getDateArchive()));
                    ivStep1.setImageResource(R.drawable.step_circle_fail);
                    tvStep1Num.setText("-");
                }
            }
        } else {
            tvStep4Title.setText(R.string.delivery_state_title_step4_finish);
            ivStep4.setImageResource(R.drawable.step_circle_disable);
            tvStep4Num.setText("4");
        }
        return result;
    }

    private void findAllView(){
        llContainer = (LinearLayout) findViewById(R.id.llContainer);

        tvStep1Num = (TextView) findViewById(R.id.tvStep1Num);
        tvStep2Num = (TextView) findViewById(R.id.tvStep2Num);
        tvStep3Num = (TextView) findViewById(R.id.tvStep3Num);
        tvStep4Num = (TextView) findViewById(R.id.tvStep4Num);

        tvStep1Title = (TextView) findViewById(R.id.tvStep1Title);
        tvStep2Title = (TextView) findViewById(R.id.tvStep2Title);
        tvStep3Title = (TextView) findViewById(R.id.tvStep3Title);
        tvStep4Title = (TextView) findViewById(R.id.tvStep4Title);

        tvStep1Description = (TextView) findViewById(R.id.tvStep1Description);
        tvStep2Description = (TextView) findViewById(R.id.tvStep2Description);
        tvStep3Description = (TextView) findViewById(R.id.tvStep3Description);
        tvStep4Description = (TextView) findViewById(R.id.tvStep4Description);

        ivStep1 = (ImageView) findViewById(R.id.ivStep1);
        ivStep2 = (ImageView) findViewById(R.id.ivStep2);
        ivStep3 = (ImageView) findViewById(R.id.ivStep3);
        ivStep4 = (ImageView) findViewById(R.id.ivStep4);
    }


    // Возвращает путь в случайный раздел где хранятся доставки todo ВРЕМЕННОЕ РЕШЕНИЕ ДЛЯ ОТЛАДКИ
    private String getDeliveryPath(){
        String path = "";
        int num = new Random().nextInt(3);
        num = 3;
        switch (num){
            case 0:
                path = Const.CHILD_DELIVERIES_NEW;
                break;
            case 1:
                path = Const.CHILD_DELIVERIES_COOKING;
                break;
            case 2:
                path = Const.CHILD_DELIVERIES_TRANSPORT;
                break;
            case 3:
                path = Const.CHILD_DELIVERIES_ARCHIVE;
                break;
        }
        Log.d("11111111", "getDeliveryPath: num = " + num + " Path = " + path);
        return path;
    }

}

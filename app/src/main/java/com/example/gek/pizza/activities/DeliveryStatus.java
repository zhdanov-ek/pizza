package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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


/** Monitoring state of delivery */

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_delivery);
        setSupportActionBar(toolbar);

        findAllView();

        // На время отладки мы получаем случайный заказ с БД из любого раздела и выводим инфу о нем
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
                    if (list.get(num).getDateArchive() != null){
                        showStateDeliveryClosed(list.get(num));
                    } else {
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

    /**
     * Show state of current not closed delivery
     */
    private void showStateDelivery(Delivery d) {
        String mes = "";
        // State 3 (Transport)
        if (d.getDateTransport() != null) {
            ivStep3.setImageResource(R.drawable.step_circle_current);
            if (d.getDateArchive() != null) {
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
        }

        // State 2 (Cooking)
        if (d.getDateCooking() != null) {
            if (d.getDateTransport() != null) {
                tvStep2Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step2_start) +
                        " (" + Utils.formatDate(d.getDateCooking()) + ")\n" +
                        getResources().getString(R.string.delivery_state_description_step2_finish) +
                        " (" + Utils.formatDate(d.getDateTransport()) + ")\n";
                tvStep2Description.setText(mes);
            } else {
                ivStep2.setImageResource(R.drawable.step_circle_current);
                tvStep2Num.setText("2");
                mes = getResources().getString(R.string.delivery_state_description_step2_start)
                        + " " + Utils.formatDate(d.getDateCooking());
                tvStep2Description.setText(mes);
            }
        }

        // State 1 (Receive)
        if ((d.getDateNew() != null) && (d.getDateArchive() == null)) {
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
            if (d.getDateArchive() == null) {
                llContainer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Show state if delivery closed (paid or fail)
     */
    private void showStateDeliveryClosed(Delivery d) {
        String mes = "";
        if (d.getPaid()) {
            ivStep4.setImageResource(R.drawable.step_circle_current);
            tvStep1Num.setText("+");
            mes = getResources().getString(R.string.delivery_state_description_step1_finish) +
                    " (" + Utils.formatDate(d.getDateCooking()) + ")";
            tvStep1Description.setText(mes);
            tvStep2Num.setText("+");
            mes = getResources().getString(R.string.delivery_state_description_step2_start) +
                    " (" + Utils.formatDate(d.getDateCooking()) + ")\n" +
                    getResources().getString(R.string.delivery_state_description_step2_finish) +
                    " (" + Utils.formatDate(d.getDateTransport()) + ")\n";
            tvStep2Description.setText(mes);
            tvStep3Num.setText("+");
            mes = getResources().getString(R.string.delivery_state_description_step3_start) +
                    " (" + Utils.formatDate(d.getDateTransport()) + ")\n" +
                    getResources().getString(R.string.delivery_state_description_step3_finish) +
                    " (" + Utils.formatDate(d.getDateArchive()) + ")\n";
            tvStep3Description.setText(mes);
            tvStep4Num.setText("+");
            mes = getResources().getString(R.string.delivery_state_description_step4_paid) +
                    " (" + Utils.formatDate(d.getDateArchive()) + ")";
            tvStep4Description.setText(mes);
        } else {
            // Отклонена доставка
            mes = getResources().getString(R.string.delivery_state_description_step4_fail) +
                    "\n" + d.getCommentShop();
            if (d.getDateTransport() != null) {
                ivStep3.setImageResource(R.drawable.step_circle_fail);
                tvStep3Num.setText("-");
                mes = getResources().getString(R.string.delivery_state_description_step4_fail) +
                        "\n" + Utils.formatDate(d.getDateTransport()) +
                        "\n" + d.getCommentShop();
                tvStep3Description.setText(mes);

                ivStep2.setImageResource(R.drawable.step_circle_disable);
                tvStep2Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step2_start) +
                        " (" + Utils.formatDate(d.getDateCooking()) + ")\n" +
                        getResources().getString(R.string.delivery_state_description_step2_finish) +
                        " (" + Utils.formatDate(d.getDateTransport()) + ")\n";
                tvStep2Description.setText(mes);

                ivStep1.setImageResource(R.drawable.step_circle_disable);
                tvStep1Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step1_finish) +
                        "\n" + Utils.formatDate(d.getDateCooking());
                tvStep1Description.setText(mes);
            } else if (d.getDateCooking() != null) {
                ivStep2.setImageResource(R.drawable.step_circle_fail);
                tvStep2Description.setText(mes + "\n" + Utils.formatDate(d.getDateCooking()));
                tvStep2Num.setText("-");
                tvStep1Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step1_finish) +
                        "\n" + Utils.formatDate(d.getDateCooking());
                tvStep1Description.setText(mes);
            } else {
                tvStep1Title.setText(getResources().getString(R.string.delivery_state_title_step1));
                tvStep1Description.setText(mes + "\n" + Utils.formatDate(d.getDateArchive()));
                ivStep1.setImageResource(R.drawable.step_circle_fail);
                tvStep1Num.setText("-");
            }
        }
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
        //num = 1;
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
        return path;
    }

}

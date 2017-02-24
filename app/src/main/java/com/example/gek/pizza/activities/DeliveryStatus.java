package com.example.gek.pizza.activities;

import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.StateLastDelivery;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import static com.example.gek.pizza.data.Const.db;


/**
 * Monitoring state of delivery
 */

public class DeliveryStatus extends BaseActivity {

    public static final String TAG = "STATUS_DELIVERY";
    private ScrollView scrollView;
    private TextView tvStep1Num, tvStep2Num, tvStep3Num, tvStep4Num;
    private TextView tvStep1Title, tvStep2Title, tvStep3Title, tvStep4Title;
    private TextView tvStep1Description, tvStep2Description, tvStep3Description, tvStep4Description;
    private ImageView ivStep1, ivStep2, ivStep3, ivStep4;
    private ValueEventListener mStateListener;
    private Boolean mIsSetListener = false;
    private ProgressBar progressBar;


    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() != Const.AUTH_USER){
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Content inflate in VIEW and put in DrawerLayout
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(this.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_delivery_status, null, false);
        mDrawer.addView(contentView, 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_delivery);
        setSupportActionBar(toolbar);

        // add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        findAllView();
    }


    /**
     * List state of last delivery and initialize fetching delivery from DB
     */
    @Override
    protected void onResume() {
        super.onResume();
        mStateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StateLastDelivery stateLastDelivery = dataSnapshot.getValue(StateLastDelivery.class);
                if (stateLastDelivery == null) {
                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                    Toast.makeText(getBaseContext(), "You don't have deliveries", Toast.LENGTH_SHORT).show();
                } else {
                    String childFolder = "";
                    switch (stateLastDelivery.getDeliveryState()) {
                        case Const.DELIVERY_STATE_NEW:
                            childFolder = Const.CHILD_DELIVERIES_NEW;
                            break;
                        case Const.DELIVERY_STATE_COOKING:
                            childFolder = Const.CHILD_DELIVERIES_COOKING;
                            break;
                        case Const.DELIVERY_STATE_TRANSPORT:
                            childFolder = Const.CHILD_DELIVERIES_TRANSPORT;
                            break;
                        default:
                            childFolder = Const.CHILD_DELIVERIES_ARCHIVE;
                    }
                    loadDeliveryInfo(childFolder, stateLastDelivery.getDeliveryId());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        // На данном моменте авторизации может не быть если окно открывается с сервиса а приложение было уничтожено
        // Log.d(TAG, "onResume: USER "+ Connection.getInstance().getCurrentAuthStatus());
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER){
            db.child(Const.CHILD_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .addValueEventListener(mStateListener);
            mIsSetListener = true;
        }
    }


    /**
     * Fetch delivery from DB and update UI
     */
    private void loadDeliveryInfo(String childFolder, String idDelivery) {
        Query query = db.child(childFolder).orderByKey().limitToFirst(1).equalTo(idDelivery);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    Delivery monitorDelivery = ds.getValue(Delivery.class);
                    if (monitorDelivery != null) {
                        if (monitorDelivery.getDateArchive() != null) {
                            showStateDeliveryClosed(monitorDelivery);
                        } else {
                            showStateDelivery(monitorDelivery);
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getBaseContext(), "You don't have deliveries", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    /**
     * Show state of current not closed delivery
     */
    private void showStateDelivery(Delivery d) {
        String mes = "";
        // State 3 (Transport)
        if (d.getDateTransport() != null) {
            if (d.getDateArchive() != null) {
                tvStep3Num.setText("+");
                mes = getResources().getString(R.string.delivery_state_description_step3_finish)
                        + " " + Utils.formatDate(d.getDateArchive());
                ivStep3.setImageResource(R.drawable.step_circle_disable);
                tvStep3Description.setText(mes);
            } else {
                tvStep3Num.setText("3");
                mes = getResources().getString(R.string.delivery_state_description_step3_start)
                        + " " + Utils.formatDate(d.getDateTransport());
                ivStep3.setImageResource(R.drawable.step_circle_current);
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
                ivStep2.setImageResource(R.drawable.step_circle_disable);
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
            scrollView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            if (d.getDateArchive() == null) {
                scrollView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
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
            ivStep3.setImageResource(R.drawable.step_circle_disable);
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
        scrollView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void findAllView() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

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

    @Override
    protected void onPause() {
        // if listener not set we will retrieve error
        if (mIsSetListener) {
            db.child(Const.CHILD_USERS)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(Const.CHILD_USER_DELIVERY_STATE)
                    .removeEventListener(mStateListener);
        }
        super.onPause();
    }

}

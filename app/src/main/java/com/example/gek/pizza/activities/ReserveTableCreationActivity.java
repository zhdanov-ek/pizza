package com.example.gek.pizza.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.OrderTable;
import com.example.gek.pizza.data.StateTableReservation;
import com.example.gek.pizza.data.Table;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.helpers.Utils;
import com.example.gek.pizza.services.MonitoringYourReservationService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.gek.pizza.R.id.btnCreateDelivery;
import static com.example.gek.pizza.data.Const.db;

/**
 * Class to reserve tables
 */

public class ReserveTableCreationActivity extends AppCompatActivity {

    private Table table;
    private String tableKey;
    private EditText etName, etComment, etPhone;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_table_creation);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Button btnReserveTable;
        TextView tvTableReservation;
        SimpleDateFormat shortenedDateFormat;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_reserve_table);
        setSupportActionBar(toolbar);

        shortenedDateFormat = new SimpleDateFormat("dd.MM.yy", Locale.US);

        if (getIntent().hasExtra(Const.EXTRA_TABLE)) {
            table = getIntent().getParcelableExtra(Const.EXTRA_TABLE);
        }
        if (getIntent().hasExtra(Const.EXTRA_TABLE_KEY)) {
            tableKey = getIntent().getStringExtra(Const.EXTRA_TABLE_KEY);
        }

        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etComment = (EditText) findViewById(R.id.etComment);
        btnReserveTable = (Button) findViewById(btnCreateDelivery);
        tvTableReservation = (TextView) findViewById(R.id.tvTableReservation);
        tvTableReservation.setText(getResources().getString(R.string.text_reservation_evening)
                + " "
                + shortenedDateFormat.format(new Date()));

        btnReserveTable.setOnClickListener(reserveTable);


        etName.setText(sharedPreferences.getString(Const.SETTINGS_USER_NAME, Connection.getInstance().getUserName()));
        etPhone.setText(sharedPreferences.getString(Const.SETTINGS_USER_PHONE, ""));

    }

    View.OnClickListener reserveTable = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkData()) {
                switch (Connection.getInstance().getCurrentAuthStatus()) {
                    case Const.AUTH_NULL:
                        startActivity(new Intent(getBaseContext(), AuthenticationActivity.class));
                        break;
                    default:
                        String id = Connection.getInstance().getUserId();

                        OrderTable orderTable = new OrderTable();
                        orderTable.setClientName(etName.getText().toString());
                        orderTable.setPhoneClient(etPhone.getText().toString());
                        orderTable.setCommentClient(etComment.getText().toString());
                        orderTable.setIsNotificated(0);
                        orderTable.setIsCheckedByAdmin(0);
                        orderTable.setUserId(id);
                        orderTable.setTableKey(tableKey);

                        String numberDelivery = Utils.makeDeliveryNumber(etPhone.getText().toString());
                        db.child(Const.CHILD_RESERVED_TABLES_NEW).child(numberDelivery).setValue(orderTable);

                        StateTableReservation stateTableReservation = new StateTableReservation();
                        stateTableReservation.setReservationKey(numberDelivery);
                        stateTableReservation.setReservationState(0);
                        db.child(Const.CHILD_USERS)
                                .child(id)
                                .child(Const.CHILD_USER_RESERVATION_STATE)
                                .child(numberDelivery)
                                .setValue(stateTableReservation);


                        getIntent().putExtra(Const.EXTRA_TABLE, table);
                        setResult(RESULT_OK, getIntent());

                        editor = sharedPreferences.edit();
                        editor.putString(Const.SETTINGS_USER_NAME, etName.getText().toString()).apply();
                        editor.putString(Const.SETTINGS_USER_PHONE, etPhone.getText().toString()).apply();

                        if (!isServiceRunning()){
                            startService(new Intent(getBaseContext(), MonitoringYourReservationService.class));
                        }
                        finish();

                }
            }
        }
    };

    private boolean isServiceRunning() {
        boolean isServiceRun;
        isServiceRun = false;
        ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MonitoringYourReservationService.class.getName().equals(service.service.getClassName())) {
                isServiceRun = true;
            }
        }
        return isServiceRun;
    }

    private Boolean checkData() {
        Boolean isCorrect = true;
        if (etName.getText().toString().length() < 2) {
            showMessage(getResources().getString(R.string.name_delivery));
            etName.setFocusable(true);
            isCorrect = false;

        } else if (etPhone.getText().toString().length() < 5) {
            showMessage(getResources().getString(R.string.phone_delivery));
            etPhone.setFocusable(true);
            isCorrect = false;

        }

        return isCorrect;
    }

    private void showMessage(String s) {
        String mes = getResources().getString(R.string.mes_check_data) + "\n";
        Toast.makeText(this, mes + s, Toast.LENGTH_SHORT).show();
    }
}

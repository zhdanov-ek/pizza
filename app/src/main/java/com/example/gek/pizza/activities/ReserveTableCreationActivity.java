package com.example.gek.pizza.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.OrderTable;
import com.example.gek.pizza.data.Table;
import com.example.gek.pizza.helpers.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.gek.pizza.R.id.btnCreateDelivery;
import static com.example.gek.pizza.data.Const.db;

/**
 * Created by Ivleshch on 08.02.2017.
 */

public class ReserveTableCreationActivity extends AppCompatActivity {

    private Table table;
    private String tableKey;
    private Button btnReserveTable;
    private EditText etName, etComment, etPhone;
    private TextView tvTableReservation;
    private SimpleDateFormat shortenedDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_table_creation);

        shortenedDateFormat = new SimpleDateFormat("dd.MM.yy");

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
        tvTableReservation.setText(getResources().getString(R.string.text_reservation_evening)+" "+shortenedDateFormat.format(new Date()));

        btnReserveTable.setOnClickListener(reserveTable);

    }

    View.OnClickListener reserveTable = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkData()) {
                OrderTable orderTable = new OrderTable();
                orderTable.setClientName(etName.getText().toString());
                orderTable.setPhoneClient(etPhone.getText().toString());
                orderTable.setCommentClient(etComment.getText().toString());
                orderTable.setIsNotificated(0);
                orderTable.setIsCheckedByAdmin(0);
                orderTable.setTableKey(tableKey);

                String numberDelivery = Utils.makeDeliveryNumber(etPhone.getText().toString());

                db.child(Const.CHILD_RESERVED_TABLES_NEW).child(numberDelivery).setValue(orderTable);
                getIntent().putExtra(Const.EXTRA_TABLE, table);
                setResult(RESULT_OK, getIntent());
                finish();
            }
        }
    };

    private Boolean checkData(){
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

    private void showMessage(String s){
        String mes = getResources().getString(R.string.mes_check_data) + "\n";
        Toast.makeText(this, mes + s, Toast.LENGTH_SHORT).show();
    }
}

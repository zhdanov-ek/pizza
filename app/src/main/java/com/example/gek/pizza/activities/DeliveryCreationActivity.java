package com.example.gek.pizza.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gek.pizza.R;

public class DeliveryCreationActivity extends AppCompatActivity {

    EditText etName, etPhone, etAddress, etComment;
    Button btnCreateDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_creation);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_delivery);
        setSupportActionBar(myToolbar);

        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etComment = (EditText) findViewById(R.id.etComment);
        btnCreateDelivery = (Button) findViewById(R.id.btnCreateDelivery);

        btnCreateDelivery.setOnClickListener(createDelivery);

    }

    // send order to FireBase
    View.OnClickListener createDelivery = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkData()) {
                //todo send data to server
            }
        }
    };

    // validate input data
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

        } else if (etAddress.getText().toString().length() < 10) {
            showMessage(getResources().getString(R.string.address_delivery));
            etAddress.setFocusable(true);
            isCorrect = false;
        }

        return isCorrect;
    }

    private void showMessage(String s){
        String mes = getResources().getString(R.string.mes_check_data) + "\n";
        Toast.makeText(this, mes + s, Toast.LENGTH_SHORT).show();
    }
}

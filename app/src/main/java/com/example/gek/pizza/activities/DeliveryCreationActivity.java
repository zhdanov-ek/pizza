package com.example.gek.pizza.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.data.StateLastDelivery;
import com.example.gek.pizza.helpers.Basket;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.helpers.Utils;
import com.example.gek.pizza.services.MonitoringYourDeliveryService;
import com.google.firebase.auth.FirebaseAuth;

import static com.example.gek.pizza.data.Const.db;

/** Create delivery */

public class DeliveryCreationActivity extends AppCompatActivity {
    private EditText etName, etPhone, etAddress, etComment;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_creation);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_delivery);
        setSupportActionBar(myToolbar);

        etName = (EditText) findViewById(R.id.etName);
        etPhone = (EditText) findViewById(R.id.etPhone);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etComment = (EditText) findViewById(R.id.etComment);
        findViewById(R.id.btnCreateDelivery).setOnClickListener(createDelivery);

        etName.setText(sharedPreferences.getString(Const.SETTINGS_USER_NAME,
                FirebaseAuth.getInstance().getCurrentUser()==null ? "":FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
        );
        etPhone.setText(sharedPreferences.getString(Const.SETTINGS_USER_PHONE, ""));
        etAddress.setText(sharedPreferences.getString(Const.SETTINGS_USER_ADDRESS, ""));

    }

    // send order to FireBase only if user not SHOP or GUEST
    View.OnClickListener createDelivery = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkData()) {
                switch (Connection.getInstance().getCurrentAuthStatus()){

                    // if add dish how guest and after login as SHOP or COURIER
                    case Const.AUTH_SHOP:
                    case Const.AUTH_COURIER:
                        Basket.getInstance().clearOrders();
                        finish();
                        break;
                    case Const.AUTH_NULL:
                        startActivity(new Intent(getBaseContext(), AuthenticationActivity.class));
                        break;
                    case Const.AUTH_USER:
                        String id = Connection.getInstance().getUserId();

                        Delivery delivery = new Delivery();
                        delivery.setNameClient(etName.getText().toString());
                        delivery.setPhoneClient(etPhone.getText().toString());
                        delivery.setAddressClient(etAddress.getText().toString());
                        delivery.setCommentClient(etComment.getText().toString());
                        delivery.setTotalSum(Basket.getInstance().getTotalSum());

                        // Extract custom pizza from list of dishes
                        if (Basket.getInstance().extractMyPizza()){
                            delivery.setTextMyPizza(Basket.getInstance().getTextMyPizza());
                            delivery.setNumbersMyPizza(Basket.getInstance().getNumbersMyPizza());
                        }

                        delivery.setNumbersDishes(Basket.getInstance().getNumberDishes());
                        delivery.setKeysDishes(Basket.getInstance().getKeysDishes());

                        // Create name for key of delivery and send data to server
                        String numberDelivery = Utils.makeDeliveryNumber(etPhone.getText().toString());

                        // Save key in object and write data to DB
                        delivery.setKey(numberDelivery);
                        delivery.setUserId(id);
                        if (FirebaseAuth.getInstance().getCurrentUser().getEmail() != null) {
                            delivery.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        }

                        //todo get location of user and save to delivery
                        delivery.setLongitude("0");
                        delivery.setLatitude("0");
                        db.child(Const.CHILD_DELIVERIES_NEW).child(numberDelivery).setValue(delivery);

                        // Info to user folder for monitoring
                        StateLastDelivery stateLastDelivery = new StateLastDelivery();
                        stateLastDelivery.setDeliveryId(numberDelivery);
                        stateLastDelivery.setDeliveryState(Const.DELIVERY_STATE_NEW);
                        db.child(Const.CHILD_USERS)
                                .child(id)
                                .child(Const.CHILD_USER_DELIVERY_STATE)
                                .setValue(stateLastDelivery);

                        Basket.getInstance().clearOrders();

                        editor = sharedPreferences.edit();
                        editor.putString(Const.SETTINGS_USER_NAME, etName.getText().toString()).apply();
                        editor.putString(Const.SETTINGS_USER_PHONE, etPhone.getText().toString()).apply();
                        editor.putString(Const.SETTINGS_USER_ADDRESS, etAddress.getText().toString()).apply();

                        // Start service for trace delivery.
                        // Id need for correct define current delivery and stop service
                        Intent monitoringDelivery = new Intent(getBaseContext(), MonitoringYourDeliveryService.class);
                        monitoringDelivery.putExtra(Const.EXTRA_DELIVERY_ID, numberDelivery);
                        startService(monitoringDelivery);
                        finish();
                }
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

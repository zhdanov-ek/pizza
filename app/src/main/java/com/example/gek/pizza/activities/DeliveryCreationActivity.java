package com.example.gek.pizza.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Basket;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Delivery;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import static com.example.gek.pizza.data.Const.db;

// Эта активити наследуется не от шаблонной, что бы нельзя было сделать изменения в корзине
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

    // send order to FireBase only if user not SHOP or GUEST
    View.OnClickListener createDelivery = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (checkData()) {
                switch (Connection.getInstance().getCurrentAuthStatus()){
                    case Const.AUTH_SHOP:
                        Connection.getInstance().signOut();
                        finish();
                        break;
                    case Const.AUTH_NULL:
                        startActivity(new Intent(getBaseContext(), AuthenticationActivity.class));
                        break;
                    default:
                        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        Delivery delivery = new Delivery();
                        delivery.setNameClient(etName.getText().toString());
                        delivery.setPhoneClient(etPhone.getText().toString());
                        delivery.setAddressClient(etAddress.getText().toString());
                        delivery.setCommentClient(etComment.getText().toString());
                        delivery.setTotalSum(Basket.getInstance().getTotalSum());
                        delivery.setNumbersDishes(Basket.getInstance().getNumberDishes());
                        delivery.setKeysDishes(Basket.getInstance().getKeysDishes());

                        // Create name for key of delivery and send data to server
                        String numberDelivery = Utils.makeDeliveryNumber(etPhone.getText().toString());

                        // Save key in object and write data to DB
                        delivery.setKey(numberDelivery);
                        delivery.setUserId(id);

                        //todo get location of user and save to delivery
                        delivery.setLongitude("xxxx longitude");
                        delivery.setLatitude("xxxx latitude");
                        db.child(Const.CHILD_DELIVERIES_NEW).child(numberDelivery).setValue(delivery);
                        db.child(Const.CHILD_USERS).child(id).child(Const.CHILD_USER_DELIVERY_ID).setValue(numberDelivery);
                        db.child(Const.CHILD_USERS).child(id).child(Const.CHILD_USER_DELIVERY_STATE).setValue(Const.DELIVERY_STATE_NEW);

                        // очищаем корзину и сохраняем текущий номер заказа
                        Basket.getInstance().makeDelivery(numberDelivery);
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

package com.example.gek.pizza.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.AllTopics;
import com.example.gek.pizza.data.Const;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Data message sending FCM
 */

public class PushActivity extends BaseActivity implements View.OnClickListener {

    private EditText etPushMessage, etPushTitle;
    private String txtPushMessage, txtPushTitle, txtTopic;
    public static final MediaType JSON = MediaType.parse(Const.FCM_JSON);
    private Spinner spinnerTopics;

    @Override
    public void updateUI() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_push);

        Toolbar myToolbar;
        Button btnSendMessage;

        myToolbar = (Toolbar) findViewById(R.id.toolBar);
        myToolbar.setTitle(R.string.title_push);
        setSupportActionBar(myToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        btnSendMessage = (Button) findViewById(R.id.btnSendMessage);
        btnSendMessage.setOnClickListener(this);

        etPushMessage = (EditText) findViewById(R.id.etPushMessage);
        etPushTitle = (EditText) findViewById(R.id.etPushTitle);

        spinnerTopics = (Spinner) findViewById(R.id.spinnerTopics);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, AllTopics.getInstance().topics);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTopics.setAdapter(dataAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_push);
        item.setCheckable(true);
        item.setChecked(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSendMessage:
                txtPushTitle = etPushTitle.getText().toString();
                txtPushMessage = etPushMessage.getText().toString();
                txtTopic = spinnerTopics.getSelectedItem().toString();
                if (!txtPushTitle.equals("")
                        && !txtPushMessage.equals("")
                        && !txtTopic.equals("")) {
                    sendPushNotication();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.mes_check_data), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void sendPushNotication() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String result = "";
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    dataJson.put(Const.FCM_TITLE, txtPushTitle);
                    dataJson.put(Const.FCM_BODY, txtPushMessage);

                    json.put(Const.FCM_DATA, dataJson);


                    json.put(Const.FCM_TO,Const.FCM_TOPICS+ txtTopic);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header(Const.FCM_AUTHORIZATION, Const.FCM_KEY + getResources().getString(R.string.firebase_server_key))
                            .url(Const.FCM_API)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();

                    result = response.body().string();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("")){
                    Toast.makeText(getApplicationContext(), R.string.notification_send_failed, Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), R.string.notification_send_success, Toast.LENGTH_SHORT).show();
                    finish();
                }

                super.onPostExecute(result);
            }
        }.execute();

    }

}
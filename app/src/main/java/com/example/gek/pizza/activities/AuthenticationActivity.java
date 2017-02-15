package com.example.gek.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;


public class AuthenticationActivity extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static int RC_SIGN_IN_GOOGLE = 1;               // request code for auth Google
    private static int RC_SIGN_IN_FACEBOOK = 2;
    private static String TAG = "AUTHENTICATION_ACTIVITY";
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(this);


        // Формируем параметры GoogleApiClient, которые будут переданы при создании намерения при авторизации
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Создание клиента  через который мы и получаем доступ к PlayServices
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnGoogleSignIn:
                // Формируем интент гугл апи, которому передаем подготовленные ранее параметры
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
                break;
        }

    }

    /** Получаем результат работы вызванного интента на авторизацию через GoogleApi */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN_GOOGLE){
            // Изымаем данные с результатом авторизации и проверяем успешно ли авторизировались
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                // в случае успеха изымаем эккаунт и передаем полученный токен в авторизацю файрбейс
                GoogleSignInAccount account = result.getSignInAccount();
                Connection.getInstance().firebaseAuthWithGoogle(account);
            }
            else
                Log.d(TAG, "Google Login Failed " + result.getStatus().toString());

        }
    }




    /** Реализуем интерфейс обработки ошибок соединений при работе с API GS */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed.");
    }
}

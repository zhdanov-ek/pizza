package com.example.gek.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;


public class AuthenticationActivity extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static int RC_SIGN_IN_GOOGLE = 1;               // request code for auth Google
    private static int RC_SIGN_IN_FACEBOOK = 2;
    private static String TAG = "AUTHENTICATION_ACTIVITY";
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Button btnGoogleSignIn;

    @Override
    public void updateUI() {
        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER){
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            Toast.makeText(this, "Firebase auth: current user = " + email, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btnGoogleSignIn = (Button) findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(this);


        // Формируем параметры GoogleApiClient, которые будут переданы при создании намерения при авторизации
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Создание клиента  через который мы и получаем доступ к PlayServices
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnGoogleSignIn:
                btnGoogleSignIn.setVisibility(View.INVISIBLE);
                tvStatus.setText("Connection to Google...\n\n");
                tvStatus.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
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
        progressBar.setVisibility(View.INVISIBLE);
        if(requestCode == RC_SIGN_IN_GOOGLE){
            // Изымаем данные с результатом авторизации и проверяем успешно ли авторизировались
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                // в случае успеха изымаем эккаунт и передаем полученный токен в авторизацю файрбейс
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                tvStatus.append("Google authentication success \n" + account.getEmail() + "\n\n");
            }
            else {
                tvStatus.append("Google authentication failed");
                Log.d(TAG, "Google Login Failed " + result.getStatus().toString());
            }


        }
    }

    /** Изымаем ID токен и подаем его в файрбейс Auth */
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                                tvStatus.append("Result authentication FireBase: \n");
                                if (task.isSuccessful()){
                                    tvStatus.append("Successful: user - " + task.getResult().getUser().getEmail() +
                                            "\n(Provider - " + task.getResult().getUser().getProviderData() + ")");
                                } else {

                                }
                                Log.d(TAG, "signInWithCredentialGoogle:oncomplete: " + task.isSuccessful());
                           }
                       }
                );
    }


    /** Реализуем интерфейс обработки ошибок соединений при работе с API GS */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed.");
    }
}

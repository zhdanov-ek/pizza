package com.example.gek.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gek.pizza.R;
import com.example.gek.pizza.helpers.Connection;
import com.example.gek.pizza.data.Const;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;

import org.json.JSONException;
import org.json.JSONObject;


/**  Authentication */

public class AuthenticationActivity extends BaseActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static int RC_SIGN_IN_GOOGLE = 1;               // request code for auth Google
    private static String TAG = "AUTHENTICATION_ACTIVITY";
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Button btnGoogleSignIn, btnFacebookSignIn;
    private LoginButton btnFacebook;
    private CallbackManager callbackManager;
    private AuthCredential credential;
    private static AuthCredential credentialLink;
    private String currentProvider;
    private static LoginResult loginResultFacebook;

    private final String VISIBILITY_BUTTON_GOOGLE = "visibility_button_google";
    private final String VISIBILITY_BUTTON_FACEBOOK = "visibility_button_facebook";
    private final String VISIBILITY_TV_STATUS = "visibility_tv_status";

    private boolean isBtnGoogleSignInVisible, isBtnFacebookSignInVisible, isTvStatusVisible;


    @Override
    public void updateUI() {
//        if (Connection.getInstance().getCurrentAuthStatus() == Const.AUTH_USER) {
//            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//            Toast.makeText(this, getResources().getString(R.string.firebase_current_user) + email, Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isBtnGoogleSignInVisible || isBtnFacebookSignInVisible) {
            outState.putBoolean(VISIBILITY_BUTTON_GOOGLE, isBtnGoogleSignInVisible);
            outState.putBoolean(VISIBILITY_BUTTON_FACEBOOK, isBtnFacebookSignInVisible);
            outState.putBoolean(VISIBILITY_TV_STATUS, isTvStatusVisible);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            isBtnGoogleSignInVisible = savedInstanceState.getBoolean(VISIBILITY_BUTTON_GOOGLE, true);
            btnGoogleSignIn.setVisibility(isBtnGoogleSignInVisible ? View.VISIBLE : View.GONE);

            isBtnFacebookSignInVisible = savedInstanceState.getBoolean(VISIBILITY_BUTTON_FACEBOOK, true);
            btnFacebookSignIn.setVisibility(isBtnFacebookSignInVisible ? View.VISIBLE : View.GONE);

            isTvStatusVisible = savedInstanceState.getBoolean(VISIBILITY_TV_STATUS, false);
            tvStatus.setVisibility(isTvStatusVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflateLayout(R.layout.activity_authentication);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.title_authentication);
        setSupportActionBar(toolbar);

        //add button for open DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btnGoogleSignIn = (Button) findViewById(R.id.btnGoogleSignIn);
        btnFacebookSignIn = (Button) findViewById(R.id.btnFacebookSignIn);

        btnGoogleSignIn.setOnClickListener(this);
        btnFacebookSignIn.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(this)
                .build();

        callbackManager = CallbackManager.Factory.create();

        btnFacebook = (LoginButton) findViewById(R.id.btnFacebook);
        btnFacebook.setReadPermissions("email", "public_profile");
        btnFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Auth", "facebook:onSuccess:" + loginResult);
                loginResultFacebook = loginResult;

                // get email from facebook
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            String email = "";
                            email = object.getString(getResources().getString(R.string.facebook_email));
                            if (!email.equals("")) {
                                firebaseAuthWithFacebook(loginResultFacebook.getAccessToken(), email);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });

                Bundle params = new Bundle();
                params.putString(getResources().getString(R.string.facebook_fields),getResources().getString(R.string.facebook_permissions));
                request.setParameters(params);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("Auth", "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                Log.d("Auth", "facebook:onError", error);
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGoogleSignIn:
                progressBar.setVisibility(View.VISIBLE);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
                break;
            case R.id.btnFacebookSignIn:
                btnFacebook.performClick();
                break;
        }

    }


    // auth result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        progressBar.setVisibility(View.INVISIBLE);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Google Login Failed " + result.getStatus().toString());
            }
        } else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

//    auth with google
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        currentProvider = Const.GOOGLE_PROVIDER;
        signLinkProvider(acct.getEmail());
    }

    private void firebaseAuthWithFacebook(AccessToken token, String email) {
        credential = FacebookAuthProvider.getCredential(token.getToken());
        currentProvider = Const.FACEBOOK_PROVIDER;
        signLinkProvider(email);
    }

    private void firebaseSignInWithCredential(AuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                       @Override
                       public void onComplete(@NonNull Task<AuthResult> task) {
                           if (task.isSuccessful()) {
                               // check if current email already sign in
                               if (credentialLink != null) {
                                   firebaseLinkWithCredential(credentialLink);
                                   credentialLink = null;
                               } else {
                                   finish();
                               }
                           } else {
                           }
                           Log.d(TAG, "signInWithCredentialGoogle:oncomplete: " + task.isSuccessful());
                       }
                   }
                );
    }

    private void firebaseLinkWithCredential(AuthCredential credential) {
        FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());
                        if (task.isSuccessful()) {
                            finish();
                        }
                    }
                });
    }

    //Sign in or link on previos provider
    public void signLinkProvider(String email) {
        FirebaseAuth.getInstance().fetchProvidersForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                        if (task.isSuccessful()) {
                            boolean providerExist;
                            providerExist = false;

                            if (task.getResult().getProviders() != null) {
                                if (task.getResult().getProviders().size() == 0) {
                                    signIn();
                                } else {
                                    for (String provider : task.getResult().getProviders()) {
                                        if (provider.equals(currentProvider)) {
                                            providerExist = true;
                                            break;
                                        }
                                    }
                                    if (providerExist) {
                                        signIn();
                                    } else {
                                        if (task.getResult().getProviders().size() > 0) {
                                            credentialLink = credential;
                                            btnVisible();
                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void btnVisible() {
        tvStatus.setVisibility(View.VISIBLE);
        isTvStatusVisible = true;
        switch (currentProvider) {
            case Const.GOOGLE_PROVIDER:
                btnFacebookSignIn.setVisibility(View.VISIBLE);
                btnGoogleSignIn.setVisibility(View.GONE);
                isBtnGoogleSignInVisible = false;
                isBtnFacebookSignInVisible = true;
                tvStatus.setText(String.format(getResources().getString(R.string.msg_link_auth)
                        ,getResources().getString(R.string.facebook)
                        ,getResources().getString(R.string.google)));
                break;
            case Const.FACEBOOK_PROVIDER:
                btnFacebookSignIn.setVisibility(View.GONE);
                btnGoogleSignIn.setVisibility(View.VISIBLE);
                isBtnFacebookSignInVisible = false;
                isBtnGoogleSignInVisible = true;
                tvStatus.setText(String.format(getResources().getString(R.string.msg_link_auth)
                        , getResources().getString(R.string.google)
                        , getResources().getString(R.string.facebook)));
                break;
        }
    }

    private void signIn() {
        switch (currentProvider) {
            case Const.GOOGLE_PROVIDER:
                firebaseSignInWithCredential(credential);
                break;
            case Const.FACEBOOK_PROVIDER:
                firebaseSignInWithCredential(credential);
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed.");
    }
}

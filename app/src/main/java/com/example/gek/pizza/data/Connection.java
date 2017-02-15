package com.example.gek.pizza.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.gek.pizza.activities.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Current auth
 */

public class Connection {
    private static final String TAG = "Connection singleton";
    private static Connection instance;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Boolean isAuthenticated;
    public Boolean getAuthenticated() {
        return isAuthenticated;
    }

    // Получаем инстанс через метод, а не конструктор, который скрыт
    public static synchronized Connection getInstance(){
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    // Constructor
    private Connection(){
        auth = FirebaseAuth.getInstance();

        // лисенер следит за изменениями состояния авторизации
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {
                    isAuthenticated = true;
                    Log.d(TAG, "FireBase authentication success " + user.getEmail());
                } else {
                    isAuthenticated = false;
                    Log.d(TAG, "FireBase authentication failed ");
                }
            }
        };
        auth.addAuthStateListener(authListener);
    }

    public void signOut(){
        auth.signOut();
        Log.d(TAG, "sign out FireBase");
    }




    /** Изымаем ID токен и подаем его в файрбейс Auth */
    public void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredentialGoogle:oncomplete: " + task.isSuccessful());
                    }
                });
    }


}

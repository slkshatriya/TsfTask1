package com.technocrats.tsftask1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private static final String EMAIL = "email";
    private static final int GOOGLE_SIGN_IN_REQUEST =112 ;
    SignInButton signIn;
    GoogleSignInOptions gso;
    GoogleSignInClient signInClient;
    LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        InitiallizeGoogleLogin();
        InitiallizeFacebook();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(MainActivity.this, HomePage.class));
            finish();
        }
    }

    private void InitiallizeGoogleLogin() {
        signIn = findViewById(R.id.signIn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DoGoogleLogin();
            }
        });
    }

    private void DoGoogleLogin() {
        GoogleSignInOptions goption=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("877984294526-86gvonljq9p7u900rtg74jdq757kfd9l.apps.googleusercontent.com")
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        //Creating Google Client Object
        GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(MainActivity.this,goption);

        //Launching Google Login Dialog Intent
        Intent intent=googleSignInClient.getSignInIntent();
        startActivityForResult(intent,GOOGLE_SIGN_IN_REQUEST);
    }

    private void InitiallizeFacebook() {
        loginButton =  findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList(EMAIL));
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        facebookHandlerCode(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }


    private void facebookHandlerCode(AccessToken token) {
        AuthCredential cred = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(cred).addOnCompleteListener(new OnCompleteListener
                <AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    loginButton.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(MainActivity.this, HomePage.class));
                    finish();

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check Result come from Google
        if(requestCode==GOOGLE_SIGN_IN_REQUEST){
            Task<GoogleSignInAccount> accountTask=GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=accountTask.getResult(ApiException.class);
                processFirebaseLoginStep(account.getIdToken());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            callbackManager.onActivityResult(requestCode,resultCode,data);
        }
    }
    private void processFirebaseLoginStep(String token){
        AuthCredential authCredential= GoogleAuthProvider.getCredential(token,null);
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user=mAuth.getCurrentUser();
                            SendUserData(user);
                        }
                    }
                });
    }

    private void  SendUserData(FirebaseUser user){
        signIn.setVisibility(View.INVISIBLE);
        startActivity(new Intent(MainActivity.this, HomePage.class));
        finish();
    }
}
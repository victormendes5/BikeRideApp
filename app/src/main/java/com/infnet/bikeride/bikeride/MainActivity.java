package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
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
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "";

    // ---> Customized setContentView with navigation drawer and toolbar
    BikeRideContentViewBuilder mContentViewBuilder;


    private EditText edtEmail;
    private EditText edtPassword;

    private Button btnLogin;
    private Button btnLoginGoogle;
    private Button btnSignUp;
    private Button btnLostLoginData;

    private FirebaseUser user;

    private FirebaseAuth autentication;
    private Users users;

    private CallbackManager callbackManager;
    private LoginButton btnFacebookLogin;

    private SignInButton btnGooglePlus;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autentication = ConfigurationFirebase.getFirebaseAuth();

        mContentViewBuilder = new BikeRideContentViewBuilder(this, R.layout.activity_main);


        user = autentication.getCurrentUser();
        if (user != null){

            Toast.makeText(this,"Usuário Logado",Toast.LENGTH_SHORT).show();

            Redirect();

        } else {

            //Declaração de EdiText
            edtEmail = (EditText) findViewById(R.id.edtLogin);
            edtPassword = (EditText) findViewById(R.id.edtPassword);

            //Declaração de Buttons
            btnLogin = (Button) findViewById(R.id.btnLogin);
            btnFacebookLogin = (LoginButton) findViewById(R.id.login_button);
            btnGooglePlus = (SignInButton) findViewById(R.id.sign_in_button);
            btnSignUp = (Button) findViewById(R.id.btnSignUp);
            btnLostLoginData = (Button) findViewById(R.id.btnLostLoginData);


            // Configuração Login Anonimo
            btnLogin.setOnClickListener(Logar);


            //Configuração Facebook Login
            FacebookSdk.sdkInitialize(getApplicationContext());
            AppEventsLogger.activateApp(this);

            callbackManager = CallbackManager.Factory.create();
            btnFacebookLogin.setReadPermissions(Arrays.asList("email"));


            //Configuração Google Login
            btnGooglePlus.setOnClickListener(LogarGoogle);

            //Configuração Sign Up
            btnSignUp.setOnClickListener(SignUp);

            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();


            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        }
    }



    @Override
    public void onBackPressed() {
        if (mContentViewBuilder.isNavigationDrawerClosed()) {
            super.onBackPressed();
        }
    }



    //Login Anonimo

    // Button Login
    public  View.OnClickListener Logar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Se os Campos de login tiverem Nulos
            if (!edtEmail.getText().toString().equals("") && !edtPassword.getText().toString().equals("")){

                users = new Users();
                users.setEmail(edtEmail.getText().toString());
                users.setPassword(edtPassword.getText().toString());

                ValidarLogin();

            }else {

                Toast.makeText(MainActivity.this,"Preencha os campos de login",Toast.LENGTH_SHORT).show();

            }
        }
    };

    // Função de Validar Login Auth Anonimo
    private void ValidarLogin(){

        autentication.signInWithEmailAndPassword(users.getEmail(), users.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Toast.makeText(MainActivity.this,"Sucesso ao Logar",Toast.LENGTH_SHORT).show();

                    Redirect();

                }else if(!task.isSuccessful()) {
                    Log.w(TAG, "signInWithEmail:failed", task.getException());

                    Toast.makeText(MainActivity.this,"Erroe",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Login Google
    public  View.OnClickListener LogarGoogle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Toast.makeText(MainActivity.this,"Clocou no sign in google" ,Toast.LENGTH_SHORT).show();

            signInGoogle();

        }
    };


    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 1);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        autentication.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(MainActivity.this,"login com google sucesso" ,Toast.LENGTH_SHORT).show();

                            Redirect();

                        } else {

                            Toast.makeText(MainActivity.this, FirebaseAuthException.class.toString(),Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }


    //Login com Facebook


    public void LoginFacebook(View v){

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                handleFacebookToken(loginResult.getAccessToken());

                Toast.makeText(MainActivity.this,"Entrou o login com Facebook",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this,"User Cancelou o login com Facebook",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void handleFacebookToken(AccessToken accessToken) {


        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());

        autentication.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Toast.makeText(MainActivity.this,"Sucesso Login Facebook no Firebase",Toast.LENGTH_SHORT).show();

                    Redirect();

                }else {
                    Toast.makeText(MainActivity.this, FirebaseError.class.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    // Método que pega o resultado do login da api em geral
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }

    }


    // Sign Up


    // Button Login
    public  View.OnClickListener SignUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mContentViewBuilder.navigate(SingUpActivity.class);

        }
    };


    // Redirect Page

    private void Redirect(){


        mContentViewBuilder.navigate(DeliveryMainActivity.class);

    }


}


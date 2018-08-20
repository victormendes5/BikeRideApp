package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.RelativeLayout;



import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
import com.infnet.bikeride.bikeride.activityrequestuser.RequestUserActivity;
import com.infnet.bikeride.bikeride.services.Abstractions;
import com.infnet.bikeride.bikeride.services.Animations;
import com.infnet.bikeride.bikeride.services.ContentViewBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity{

    private TextView mProfileName;
//    private UserManager mUserManager = new UserManager(this);
    Toolbar toolbar;

    private static final String TAG = "ErrorRonan";

    // ---> Customized setContentView with navigation drawer and toolbar
    ContentViewBuilder mContentViewBuilder;

    Intent intent;

    private EditText edtEmail;
    private EditText edtPassword;

    private Button btnLogin;
    private Button btnLoginGoogle;
    private Button btnSignUp;
    private Button btnForgotPassword;

    private FirebaseUser user;

    private FirebaseAuth autentication;
    private Users users = new Users();

    private CallbackManager callbackManager;
    private LoginButton btnFacebookLogin;

    private SignInButton btnGooglePlus;
    private GoogleSignInClient mGoogleSignInClient;

    private UserManager mUserManager = new UserManager();
    private Users mUserNew = new Users();
    private Users usuarioLogado = new Users();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autentication = ConfigurationFirebase.getFirebaseAuth();

        mContentViewBuilder = new ContentViewBuilder(this, R.layout.activity_main);

        setContentView(R.layout.activity_main);

        user = autentication.getCurrentUser();

        //Se usuário já logado
        if (user != null) {

//            Toast.makeText(this, "Usuário Logado", Toast.LENGTH_SHORT).show();

            // Redireciona tela pra Drlivery Main
            Redirect(RequestUserActivity.class);

            // Func de UserManager para retornar os dados do usuário logado
            mUserManager.getPerfil(new UserManager.OnUserComplete() {
                @Override
                public void onUserComplete(Users data) {
                    usuarioLogado = data;
                    Log.v("MainRonanError", usuarioLogado.getEmail());//Para pegar email
                    Log.v("MainRonanError", usuarioLogado.getName());//Para pegar nome

                }
                @Override
                public void onErrorUserComplete(Users data) {
                    Log.v("MainRonanError", data.toString());
                }
            },user.getUid().toString());


        }



        //Declaração de EdiText
        edtEmail = (EditText) findViewById(R.id.edtLogin);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        //Declaração de Buttons
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnFacebookLogin = (LoginButton) findViewById(R.id.login_button);
        btnGooglePlus = (SignInButton) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnForgotPassword = (Button) findViewById(R.id.btnLostLoginData);

        // Configuração Login Anonimo
        btnLogin.setOnClickListener(Logar);


        //Configuração Facebook Login
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();
        btnFacebookLogin.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));


        //Configuração Google Login
        btnGooglePlus.setOnClickListener(LogarGoogle);

        //Configuração Sign Up
        btnSignUp.setOnClickListener(SignUp);

        //Configuração Forgot Password
        btnForgotPassword.setOnClickListener(oc_forgotPassword);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


//        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
//                drawerLayout,
//                toolbar,
//                R.string.drawer_open,
//                R.string.drawer_close);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        NavigationView navigationView = findViewById(R.id.navigation_view);
//        navigationView.setNavigationItemSelectedListener(this);

//        mProfileName = findViewById(R.id.drawer_header_profileName);
//        if (mUserManager.getName() != null) {
//            mProfileName.setText(mUserManager.getName());
//        }


    }


    // Forgot Password
    public View.OnClickListener oc_forgotPassword = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Redirect(ForgotPasswordActivity.class);

        }
    };


//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(Gravity.START)) {
//            drawerLayout.closeDrawer(Gravity.START);
//        } else {
//            super.onBackPressed();
//        }
//    }


    //Login Anonimo

    // Button Login
    public View.OnClickListener Logar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Se os Campos de login tiverem Nulos
            if (!edtEmail.getText().toString().equals("") && !edtPassword.getText().toString().equals("")) {

                users = new Users();
                users.setEmail(edtEmail.getText().toString());
                users.setPassword(edtPassword.getText().toString());

                ValidarLogin();

            } else {

                Toast.makeText(MainActivity.this, "Preencha os campos de login", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Função de Validar Login Auth Anonimo
    private void ValidarLogin() {

        autentication.signInWithEmailAndPassword(users.getEmail(), users.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(MainActivity.this, "Sucesso ao Logar", Toast.LENGTH_SHORT).show();

                    Redirect(RequestUserActivity.class);

                } else if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithEmail:failed", task.getException());

                    Toast.makeText(MainActivity.this, "Erroe", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Login Google
    public View.OnClickListener LogarGoogle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

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

                            Toast.makeText(MainActivity.this, "login com google sucesso", Toast.LENGTH_SHORT).show();

                            FirebaseUser userLogad = autentication.getCurrentUser();

                            users.setId(userLogad.getUid());

                            CriarUser(users);

                            Redirect(RequestUserActivity.class);

                        } else {

                            Toast.makeText(MainActivity.this, FirebaseAuthException.class.toString(), Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }


    //Login com Facebook


    public void LoginFacebook(View v) {

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                Toast.makeText(MainActivity.this, "Entrou o login com Facebook", Toast.LENGTH_SHORT).show();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
//                                Log.v("MainRonan", response.toString());
                                setProfileToView(object);
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,picture,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

                handleFacebookToken(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "User Cancelou o login com Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void handleFacebookToken(AccessToken accessToken) {


        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());

        autentication.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(MainActivity.this, "Sucesso Login Facebook no Firebase", Toast.LENGTH_SHORT).show();

                    Redirect(RequestUserActivity.class);
                    FirebaseUser userLogad = autentication.getCurrentUser();

                    users.setId(userLogad.getUid());

                    CriarUser(users);

                } else {
                    Toast.makeText(MainActivity.this, FirebaseError.class.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setProfileToView(JSONObject jsonObject) {

        try {

            users.setEmail(jsonObject.getString("email").toString());
            users.setLastName(jsonObject.getString("last_name").toString());
            users.setName(jsonObject.getString("first_name").toString());
            users.setUrlPhoto(jsonObject.getString("picture").toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    // Método que pega o resultado do login da api em geral
//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        int itemID = item.getItemId();
//
//        switch (itemID) {
//            case R.id.deliveryman_review:
//                intent = new Intent(this, DeliverymanReviewActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.delivery_tracking:
//                intent = new Intent(this, DeliveryTrackingActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.delivery_main:
//                intent = new Intent(this, RequestUserActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.delivery_quotation:
//                intent = new Intent(this, DeliveryQuotationPriceActivity.class);
//                startActivity(intent);
//                break;
//            case R.id.profile:
//                intent = new Intent(this, ProfileActivity.class);
//                startActivity(intent);
//                break;
//            default:
//                break;
//        }
//        return false;
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                users.setEmail(account.getEmail().toString());
                users.setUrlPhoto(account.getPhotoUrl().toString());
                users.setName(account.getGivenName().toString());
                users.setLastName(account.getFamilyName());

                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                Log.v("MainRonan", e.getMessage());

            }

        }

    }


    // Sign Up


    // Button Login
    public  View.OnClickListener SignUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Redirect(SingUpActivity.class);

        }
    };


    // Redirect Page

    private void Redirect(Class destination){


        Intent newIntent = new Intent(MainActivity.this, destination);
        startActivity(newIntent);

    }

    private void CriarUser(Users u){

        mUserManager.adicionarOuAtualizarPerfil(u);

    }


}


package com.infnet.bikeride.bikeride.Tabbar;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.infnet.bikeride.bikeride.DeliveryMainActivity;
import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.UserManager;
import com.infnet.bikeride.bikeride.Users;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SignInSocialMedia extends Fragment {

    private GoogleSignInClient mGoogleSignInClient;

    private CallbackManager callbackManager;
    private FirebaseAuth autentication;

    private Users users = new Users();
    private UserManager mUserManager = new UserManager();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in_social_media, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LoginButton mFacebookButton = getView().findViewById(R.id.facebookSignInButton);
        SignInButton mGoogleButton = getView().findViewById(R.id.googleSignInButton);

        mFacebookButton.setOnClickListener(FacebookSignIn);
        mGoogleButton.setOnClickListener(GoogleSignIn);

        FacebookSdk.sdkInitialize(getContext());
        AppEventsLogger.activateApp(getContext());

        callbackManager = CallbackManager.Factory.create();
        mFacebookButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));


        mGoogleButton.setOnClickListener(GoogleSignIn);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(getActivity(), gso);

    }

    private View.OnClickListener GoogleSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 1);
        }
    };

    private View.OnClickListener FacebookSignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {


                    Toast.makeText(getActivity(), "Entrou o login com Facebook", Toast.LENGTH_SHORT).show();

                    GraphRequest request = GraphRequest.newMeRequest(
                            loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
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
                    Toast.makeText(getActivity(), "User Cancelou o login com Facebook", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        autentication.signInWithCredential(credential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "login com google sucesso", Toast.LENGTH_SHORT).show();
                    FirebaseUser userLogad = autentication.getCurrentUser();
                    users.setId(userLogad.getUid());
                    mUserManager.adicionarOuAtualizarPerfil(users);
                    Redirect(DeliveryMainActivity.class);
                } else {
                    Toast.makeText(getActivity(), FirebaseAuthException.class.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {

            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                users.setEmail(account.getEmail());
                users.setUrlPhoto(account.getPhotoUrl().toString());
                users.setName(account.getGivenName());
                users.setLastName(account.getFamilyName());

                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Handle error
            }

        }

    }

    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential authCredential = FacebookAuthProvider.getCredential(accessToken.getToken());

        autentication.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Sucesso Login Facebook no Firebase", Toast.LENGTH_SHORT).show();
                    Redirect(DeliveryMainActivity.class);
                    FirebaseUser userLogad = autentication.getCurrentUser();
                    users.setId(userLogad.getUid());
                    mUserManager.adicionarOuAtualizarPerfil(users);
                } else {
                    Toast.makeText(getActivity(), FirebaseError.class.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setProfileToView(JSONObject jsonObject) {
        try {
            users.setEmail(jsonObject.getString("email"));
            users.setLastName(jsonObject.getString("last_name"));
            users.setName(jsonObject.getString("first_name"));
            users.setUrlPhoto(jsonObject.getString("picture"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void Redirect(Class destination){
        Intent newIntent = new Intent(getActivity(), destination);
        startActivity(newIntent);
    }

}

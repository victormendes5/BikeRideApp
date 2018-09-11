package com.infnet.bikeride.bikeride.Tabbar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.infnet.bikeride.bikeride.BRAnimations;
import com.infnet.bikeride.bikeride.ConfigurationFirebase;
import com.infnet.bikeride.bikeride.DeliveryMainActivity;
import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.UserManager;
import com.infnet.bikeride.bikeride.Users;

public class SignIn extends Fragment {

    private EditText mEmail;
    private EditText mPassword;
    private Button mForgotPassword;
    private Button mSignIn;

    private Users users = new Users();

    private FirebaseAuth authentication;

    private UserManager mUserManager = new UserManager();
    private Users usuarioLogado = new Users();

    private View mForgotPasswordModal;

    private BRAnimations mAnimate = new BRAnimations(200);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmail = getView().findViewById(R.id.signInEmailEditText);
        mPassword = getView().findViewById(R.id.signInPasswordEditText);
        mForgotPassword = getView().findViewById(R.id.forgotPasswordButton);
        mSignIn = getView().findViewById(R.id.signInButton);

        mForgotPasswordModal = getActivity().findViewById(R.id.include_frag_forgotPassword);

        authentication = ConfigurationFirebase.getFirebaseAuth();
        FirebaseUser user = authentication.getCurrentUser();

        mSignIn.setOnClickListener(SignIn);
        mForgotPassword.setOnClickListener(ForgotPassword);

        if (user != null) {
            Redirect(DeliveryMainActivity.class);
            mUserManager.getPerfil(new UserManager.OnUserComplete() {
                @Override
                public void onUserComplete(Users data) {
                    usuarioLogado = data;
                }
                @Override
                public void onErrorUserComplete(Users data) {
                    // Handler error

                }
            }, user.getUid());
        }
    }

    private View.OnClickListener SignIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!mEmail.getText().toString().equals("") && !mPassword.getText().toString().equals("")) {
                users = new Users();
                users.setEmail(mEmail.getText().toString());
                users.setPassword(mPassword.getText().toString());

                ValidateLogin();
            } else {
                Toast.makeText(getActivity(), "Preencha os campos de login", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener ForgotPassword = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            mAnimate.translateFromBottomIfInvisible(mForgotPasswordModal);

            // Create new fragment and transaction
//            Fragment newFragment = new ForgotPassword();
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack if needed
            // transaction.replace(R.id.container, newFragment);
//            transaction.addToBackStack(null);

            // Commit the transaction
//            transaction.commit();
        }
    };

    private void ValidateLogin() {
        authentication.signInWithEmailAndPassword(users.getEmail(), users.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Sucesso ao Logar", Toast.LENGTH_SHORT).show();
                    Redirect(DeliveryMainActivity.class);
                } else if (!task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Erroe", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void Redirect(Class destination) {
        Intent newIntent = new Intent(getActivity(), destination);
        startActivity(newIntent);
    }

}
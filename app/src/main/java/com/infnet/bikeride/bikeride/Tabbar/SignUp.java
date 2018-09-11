package com.infnet.bikeride.bikeride.Tabbar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.infnet.bikeride.bikeride.ConfigurationFirebase;
import com.infnet.bikeride.bikeride.DeliveryMainActivity;
import com.infnet.bikeride.bikeride.R;
import com.infnet.bikeride.bikeride.UserManager;
import com.infnet.bikeride.bikeride.Users;

public class SignUp extends Fragment {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mPasswordConfirm;
    private Button mSignUp;

    private Users users;
    private FirebaseAuth autentication;

    private UserManager mUserManager = new UserManager();
    private Users mUsersNew =  new Users();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmail = getView().findViewById(R.id.signUpEmailEditText);
        mPassword = getView().findViewById(R.id.signUpPasswordEditText);
        mPasswordConfirm = getView().findViewById(R.id.signUpConfirmPasswordEditText);
        mSignUp = getView().findViewById(R.id.signUpButton);

        mSignUp.setOnClickListener(SignUp);
    }

    private View.OnClickListener SignUp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!mEmail.getText().toString().equals("") && !mPassword.getText().toString().equals("")) {
                if(!mPassword.getText().toString().equals(mPasswordConfirm.getText().toString())){
                    Toast.makeText(getContext(), "Senhas não conferem!" , Toast.LENGTH_SHORT).show();
                    return;
                }
                users = new Users();
                users.setEmail(mEmail.getText().toString());
                users.setPassword(mPassword.getText().toString());
                ValidateSignUp();
            } else {
                Toast.makeText(getActivity(),"Preencha os campos de login", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void ValidateSignUp() {
        autentication = ConfigurationFirebase.getFirebaseAuth();
        autentication.createUserWithEmailAndPassword(users.getEmail(),
                users.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = autentication.getCurrentUser();

                    mUsersNew.setId(user.getUid());
                    mUsersNew.setEmail(user.getEmail());
                    mUsersNew.setName(users.getName());
                    mUsersNew.setLastName(users.getLastName());

                    mUserManager.adicionarOuAtualizarPerfil(mUsersNew);

                    Toast.makeText(getActivity(),"Sucesso ao Cadastrar", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), DeliveryMainActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(getActivity(),"Erro ao Logar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
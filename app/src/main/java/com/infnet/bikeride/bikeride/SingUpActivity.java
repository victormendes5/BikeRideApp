package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SingUpActivity extends AppCompatActivity  {

    private DrawerLayout drawerLayout;
    private Intent intent;

    private EditText edtEmail;
    private EditText edtPassWord;
    private EditText edtFirstName;
    private EditText edtLastName;
    private Button btnSignUp;

    private FirebaseAuth autentication;
    private Users users;


    private UserManager mUserManager = new UserManager();
    private Users mUsersNew =  new Users();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);


        edtEmail = (EditText) findViewById(R.id.edtEmailSignUp);
        edtPassWord = (EditText) findViewById(R.id.edtPasswordSignUp);
        edtFirstName = (EditText) findViewById(R.id.edtFirstNameSignUp);
        edtLastName = (EditText) findViewById(R.id.edtLastNameSignUp);
        btnSignUp = (Button) findViewById(R.id.btnSignUpCad);

        btnSignUp.setOnClickListener(SignUp);

    }


    // Button Login
    public  View.OnClickListener SignUp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Se os Campos de login tiverem Nulos
            if (!edtEmail.getText().toString().equals("") && !edtPassWord.getText().toString().equals("")
                    && !edtFirstName.getText().toString().equals("") && !edtLastName.getText().toString().equals("")){

                users = new Users();
                users.setEmail(edtEmail.getText().toString());
                users.setPassword(edtPassWord.getText().toString());
                users.setName(edtFirstName.getText().toString());
                users.setLastName(edtLastName.getText().toString());

                ValidarSignUp();

            }else {

                Toast.makeText(SingUpActivity.this,"Preencha os campos de login",Toast.LENGTH_SHORT).show();

            }
        }
    };

    private void ValidarSignUp() {

        autentication = ConfigurationFirebase.getFirebaseAuth();

        autentication.createUserWithEmailAndPassword(users.getEmail(), users.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Toast.makeText(SingUpActivity.this,"Sucesso ao Cadastrar",Toast.LENGTH_SHORT).show();

                    FirebaseUser user = autentication.getCurrentUser();

                    mUsersNew.setId(user.getUid());
                    mUsersNew.setEmail(user.getEmail());
                    mUsersNew.setName(users.getName());
                    mUsersNew.setLastName(users.getLastName());

                    CriarUser(mUsersNew);

                    intent = new Intent(SingUpActivity.this, DeliveryMainActivity.class);
                    startActivity(intent);

                }else {

                    Toast.makeText(SingUpActivity.this,"Erro ao Logar",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    private void CriarUser(Users u){

        mUserManager.adicionarOuAtualizarPerfil(u);

    }

}

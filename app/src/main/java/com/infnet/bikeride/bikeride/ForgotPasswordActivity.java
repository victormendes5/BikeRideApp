package com.infnet.bikeride.bikeride;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnSendEmail;

    private FirebaseAuth autentication;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = (EditText) findViewById(R.id.edtEmailForgotPs);
        btnSendEmail = (Button) findViewById(R.id.btnSendEmail);

        btnSendEmail.setOnClickListener(oc_sendEmail);

    }

    // Forgot Password
    public View.OnClickListener oc_sendEmail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            sendEmailFg();

        }
    };


    private void sendEmailFg(){


        autentication = ConfigurationFirebase.getFirebaseAuth();


        if (!edtEmail.getText().toString().equals("")){

            autentication.sendPasswordResetEmail(edtEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                Toast.makeText(ForgotPasswordActivity.this, "Email enviado", Toast.LENGTH_SHORT).show();

                                Redirect(MainActivity.class);

                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Email Inválido", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }else {

            Toast.makeText(ForgotPasswordActivity.this, "Por Favor digite seu email", Toast.LENGTH_SHORT).show();

        }




    }


    private void Redirect(Class destination){


        Intent newIntent = new Intent(ForgotPasswordActivity.this, destination);
        startActivity(newIntent);

    }

}

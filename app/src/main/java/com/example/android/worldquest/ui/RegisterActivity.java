package com.example.android.worldquest.ui;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.worldquest.R;

/**
 * Created by SterlingRyan on 8/16/2017.
 */

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_email_layout);

        mAuth = FirebaseAuth.getInstance();

        // Get view references
        final EditText emailAddressEditText = (EditText) findViewById(R.id.email);
        EditText nameAndSurnameEditText = (EditText) findViewById(R.id.name);
        final EditText passwordEditText = (EditText) findViewById(R.id.password);
        Button registerButton = (Button) findViewById(R.id.button_create);

        //set view functionality
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = emailAddressEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if(emailAddress.isEmpty()|| password.isEmpty()){
                    showToast("Email or Password is incorrect");
                }
                else{
                    registerWithEmailAndPassword(emailAddress, password);
                }
            }
        });
    }

    private void registerWithEmailAndPassword(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // Sign in success therefore sign in
                            showToast("Sign in successful");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {
                            showToast("Sign in unsuccessful");
                        }
                    }
                });
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

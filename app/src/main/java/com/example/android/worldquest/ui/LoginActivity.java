package com.example.android.worldquest.ui;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.worldquest.R;

import org.w3c.dom.Text;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get Firebase instance
        mAuth = FirebaseAuth.getInstance();

        // Get views references
        final AutoCompleteTextView emailAddressTextView = (AutoCompleteTextView) findViewById(R.id.email);
        final EditText passwordEditTextView = (EditText) findViewById(R.id.password);
        Button signInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button registerButton = (Button) findViewById(R.id.register_button);

        // Set views functionality
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = emailAddressTextView.getText().toString();
                String password = passwordEditTextView.getText().toString();

                if(emailAddress.isEmpty()|| password.isEmpty()){
                    showSnackBar("Email or Password is incorrect");
                }
                else{
                    signInWithEmailAndPassword(emailAddress, password);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Show a message
     * @param message
     */
    private void showSnackBar(String message){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    private void signInWithEmailAndPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // Sign in successful
                            showSnackBar("Sign in successful");
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            // Sign in unsuccessful
                            showSnackBar("Sign in unsuccessful");
                        }
                    }
                });
    }
}


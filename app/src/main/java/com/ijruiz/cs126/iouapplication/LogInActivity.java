package com.ijruiz.cs126.iouapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

// Sources and references:
// Android Studio Firebase Assistant
// Firebase Authentication Docs: https://firebase.google.com/docs/auth/
// Firebase Authentication Manage Users Docs: https://firebase.google.com/docs/auth/android/manage-users
// YouTube: Android Studio Tutorial - Firebase Authentication by EDMT Dev: https://www.youtube.com/watch?v=9ARoMRd1kXo
public class LogInActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button logInButton;
    private TextView forgotLink;
    private TextView signUpLink;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailEditText = (EditText) findViewById(R.id.suEmailEditText);
        passwordEditText = (EditText) findViewById(R.id.suPasswordEditText);
        logInButton = (Button) findViewById(R.id.registerButton);
        forgotLink = (TextView) findViewById(R.id.forgotLink);
        signUpLink = (TextView) findViewById(R.id.signUpLink);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(LogInActivity.this, DashboardActivity.class));
                    finish();
                }
            }
        };

        forgotLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, ForgotPasswordActivity.class));
                finish();
            }
        });

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
                finish();
            }
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener);
        }
    }

    /**
     * Logs in user using FirebaseAuth.
     */
    private void loginUser() {

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (Validator.validateCredentials(email, password)) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LogInActivity.this, R.string.nonexistent, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(LogInActivity.this, R.string.invalid_creds, Toast.LENGTH_LONG).show();
        }
    }





}
package com.ijruiz.cs126.iouapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

// Sources and references:
// Android Studio Firebase Assistant
// Firebase Authentication Docs: https://firebase.google.com/docs/auth/
// Firebase Authentication Manage Users Docs: https://firebase.google.com/docs/auth/android/manage-users
// Android Studio Tutorial - Firebase Authentication by EDMT Dev: https://www.youtube.com/watch?v=9ARoMRd1kXo
public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText displayNameEditText;
    private Button registerButton;
    private TextView logInLink;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = (EditText) findViewById(R.id.suEmailEditText);
        passwordEditText = (EditText) findViewById(R.id.suPasswordEditText);
        displayNameEditText = (EditText) findViewById(R.id.displayNameEditText);
        registerButton = (Button) findViewById(R.id.registerButton);
        logInLink = (TextView) findViewById(R.id.logInLink);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (auth.getCurrentUser() != null) {
                    startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                    finish();
                }
            }
        };

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        logInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
                finish();
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
     * Signs up user using FirebaseAuth and adds user to Firebase Realtime Database. User is logged
     * upon successful registration.
     */
    private void signUpUser() {

        final String displayName = displayNameEditText.getText().toString();
        final String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (Validator.validateCredentials(email, password)) {
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, R.string.sign_up_fail, Toast.LENGTH_LONG).show();
                            } else {
                                User newUser = new User(auth.getCurrentUser().getUid(), displayName, email);
                                FirebaseOperations.addUser(newUser);
                                setDisplayName(); //FirebaseAuth's method doesn't seem to be working.
                                Toast.makeText(SignUpActivity.this, R.string.sign_up_success, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
        else {
            if (!Validator.validatePassword(password)) {
                Toast.makeText(SignUpActivity.this, R.string.invalid_password, Toast.LENGTH_LONG).show();
            }
            else if (!Validator.validateDisplayName(displayName)) {
                Toast.makeText(SignUpActivity.this, R.string.invalid_display_name, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SignUpActivity.this, R.string.invalid_email, Toast.LENGTH_LONG).show();
            }
        }


    }

    /**
     * Set's user's display name using FirebaseAuth.
     * 05/02/2017: updateProfile() method doesn't seem to be working properly
     */
    private void setDisplayName() {
        final String displayName = displayNameEditText.getText().toString();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName).build();
        auth.getCurrentUser().updateProfile(profileUpdates);
    }
}

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
import com.google.firebase.auth.FirebaseAuth;

// Sources and references:
// Android Studio Firebase Assistant
// Firebase Authentication Docs: https://firebase.google.com/docs/auth/
// Firebase Authentication Manage Users Docs: https://firebase.google.com/docs/auth/android/manage-users
// Android Studio Tutorial - Firebase Authentication by EDMT Dev: https://www.youtube.com/watch?v=9ARoMRd1kXo
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private TextView logInLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        resetPasswordButton = (Button) findViewById(R.id.resetPasswordButton);
        logInLink = (TextView) findViewById(R.id.logInLink);

        auth = FirebaseAuth.getInstance();

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        logInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPasswordActivity.this, LogInActivity.class));
                finish();
            }
        });


    }

    /**
     * Sends a reset password instructions to user's email.
     */
    private void resetPassword() {

        String email = emailEditText.getText().toString();
        if (Validator.validateEmail(email)) {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, R.string.pw_reset_success, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, R.string.pw_reset_fail, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }
}

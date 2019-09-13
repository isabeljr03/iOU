package com.ijruiz.cs126.iouapplication;

import android.app.Dialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EditProfileActivity extends AppCompatActivity {

    //Log.d() TAG
    private final String EDIT_PROFILE = "EDIT_PROFILE";

    //Keywords for constructing Firebase Database paths
    public static final String USERS_STARTPOINT = "users/";
    public static final String UIDS_STARTPOINT = "uids/";
    public static final String EMAIL_ENDPOINT = "/email";

    private CheckBox emailCheckBox;
    private CheckBox passwordCheckBox;

    private EditText nEmailEditText;
    private EditText nPasswordEditText;

    private Button saveChangesButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        emailCheckBox = (CheckBox) findViewById(R.id.emailCheckBox);
        passwordCheckBox = (CheckBox) findViewById(R.id.passwordCheckBox);

        nEmailEditText = (EditText) findViewById(R.id.nEmailEditText);
        nPasswordEditText = (EditText) findViewById(R.id.nPasswordEditText);

        saveChangesButton = (Button) findViewById(R.id.saveChangesButton);
        cancelButton = (Button) findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfileActivity.this, DashboardActivity.class));
                finish();
            }
        });

        //TODO: buggy when changing email and password simultaneously
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (passwordCheckBox.isChecked() || emailCheckBox.isChecked()) {

                    CountDownLatch cdl = new CountDownLatch(1);

                    if (passwordCheckBox.isChecked()) {
                        updatePassword();
                    }

                    if (emailCheckBox.isChecked()) {
                        updateEmail();
                    }

                    try {
                        cdl.await(2, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        Log.d(EDIT_PROFILE, "CountDownLatch exception when saving changes." + e);
                    }

                    startActivity(new Intent(EditProfileActivity.this, DashboardActivity.class));
                    finish();


                } else {
                    Toast.makeText(EditProfileActivity.this, "You must fill in the fields you want to change and check their boxes.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updatePassword() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = auth.getCurrentUser();

        String newPassword = nPasswordEditText.getText().toString();
        if (Validator.validatePassword(newPassword)) {

            fbUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(EDIT_PROFILE, "Updated password.");
                        Toast.makeText(EditProfileActivity.this, "Updated password successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(EDIT_PROFILE, "Failed to update password. Exception: " + task.getException());
                        Toast.makeText(EditProfileActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            Toast.makeText(EditProfileActivity.this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmail() {

        final String newEmail = nEmailEditText.getText().toString();

        if (Validator.validateEmail(newEmail)) {

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser fbUser = auth.getCurrentUser();

            //updates email information in Firebase Authentication
            fbUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(EDIT_PROFILE, "Updated auth email to: " + newEmail);
                        Toast.makeText(EditProfileActivity.this, "Email update successful.", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(EDIT_PROFILE, "Did not update auth email." + task.getException());
                        Toast.makeText(EditProfileActivity.this, "Failed to update email.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            //updates email information in Firebase Database
            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + fbUser.getUid());
            uidRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String displayName = dataSnapshot.getValue(String.class);
                    Log.d(EDIT_PROFILE, "Retrieved display name: " + displayName);

                    DatabaseReference emailRef = firebaseDatabase.getReference(USERS_STARTPOINT + displayName + EMAIL_ENDPOINT);
                    emailRef.setValue(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(EDIT_PROFILE, "Updated firebase email to: " + newEmail);
                            } else {
                                Log.d(EDIT_PROFILE, "Did not update firebase email.");
                            }
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "Email must be of the form: 'name@domain.ext'", Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: Erases the user without saving friends or transaction history for the sake of getting a new display name...
//    private void updateDisplayName() {
//        final String newDisplayName = nDisplayNameEditText.getText().toString();
//        if (Validator.validateDisplayName(newDisplayName)) {
//
//            FirebaseAuth auth = FirebaseAuth.getInstance();
//            FirebaseUser fbUser = auth.getCurrentUser();
//
//            final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//            DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + fbUser.getUid());
//            uidRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    final String displayName = dataSnapshot.getValue(String.class);
//                    Log.d(EDIT_PROFILE, "Retrieved display name: " + displayName);
//
//                    //removes user from Firebase Database
//                    if (!displayName.equals(newDisplayName)) {
//                        DatabaseReference userRef = firebaseDatabase.getReference(USERS_STARTPOINT + displayName);
//                        userRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    Log.d(EDIT_PROFILE, "Successfully removed old user: " + displayName);
//                                } else {
//                                    Log.d(EDIT_PROFILE, "Failed to remove old user: " + displayName + " " + task.getException());
//                                }
//                            }
//                        });
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//            //replaces user with new display name in Firebase Database
//            User updatedUser = new User(fbUser.getUid(), newDisplayName, fbUser.getEmail());
//            FirebaseOperations.addUser(updatedUser);
//
//            uidRef.setValue(newDisplayName);
//
//        }
//        else {
//            Toast.makeText(EditProfileActivity.this, R.string.invalid_display_name, Toast.LENGTH_SHORT).show();
//        }
//
//    }
}

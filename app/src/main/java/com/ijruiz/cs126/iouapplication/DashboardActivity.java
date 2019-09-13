package com.ijruiz.cs126.iouapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity {

    //Log.d() TAG
    public static final String DASHBOARD = "DASHBOARD";

    //Keywords for constructing Firebase Database paths
    public static final String USERS_STARTPOINT = "users/";
    public static final String UIDS = "uids";
    public static final String UIDS_STARTPOINT = "uids/";
    public static final String TRANSACTION_HISTORY_ENDPOINT = "/transactionHistory";

    private String userDisplayName = "";

    private TextView welcomeTextView;
    private TextView emailTextView;
    private TextView owedByTextView;
    private TextView owedToTextView;

    private Button editProfileButton;
    private Button logOutButton;
    private Button transactionHistoryButton;
    private Button friendListButton;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        welcomeTextView = (TextView) findViewById(R.id.welcomeTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);

        editProfileButton = (Button) findViewById(R.id.editProfileButton);
        logOutButton = (Button) findViewById(R.id.logOutButton);
        transactionHistoryButton = (Button) findViewById(R.id.transactionHistoryButton);
        friendListButton = (Button) findViewById(R.id.friendListButton);

        owedByTextView = (TextView) findViewById(R.id.fdOwedTextView);
        owedToTextView = (TextView) findViewById(R.id.fdOwedToTextView);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (auth.getCurrentUser() == null) {
                    startActivity(new Intent(DashboardActivity.this, LogInActivity.class));
                    finish();
                }
                else {
                    FirebaseUser fbUser = auth.getCurrentUser();
                    welcomeUser(fbUser);
                    calculateTotalOwed();
                }
            }

        };

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, EditProfileActivity.class));
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
            }
        });

        transactionHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, HistoryActivity.class));
                finish();
            }
        });

        friendListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, FriendListActivity.class));
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
     * Reading user information from database to personalize welcome messages
     * @param fbUser user that's logged in
     */
    private void welcomeUser(final FirebaseUser fbUser) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference uidsReference = firebaseDatabase.getReference(UIDS);

        CountDownLatch cdl1 = new CountDownLatch(1);
        uidsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(fbUser.getUid())) {
                    String userDisplayName = dataSnapshot.getValue(String.class);
                    welcomeTextView.setText("Welcome, " + userDisplayName + ", to your Dashboard!");
                    Log.d(DASHBOARD, "Retreived display name: " + userDisplayName);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        try {
            cdl1.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(DASHBOARD, "CountDownLatch1 error.");
        }

        String email = fbUser.getEmail();
        emailTextView.setText(email);
    }

    /**
     * Populates owedBy and owedTo TextViews with calculated totals from user's transaction history.
     */
    private void calculateTotalOwed() {

        final ArrayList<Transaction> history = new ArrayList<>();

        //getting current user's display name
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String cUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + cUserUID);
        uidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDisplayName = dataSnapshot.getValue(String.class);
                Log.d(DASHBOARD, "Calculating total owed - Retrieved user: " + userDisplayName);

                //reading user's transaction history
                DatabaseReference userHistoryRef = firebaseDatabase.getReference(USERS_STARTPOINT + userDisplayName + TRANSACTION_HISTORY_ENDPOINT);
                userHistoryRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Transaction transaction = dataSnapshot.getValue(Transaction.class);
                        history.add(transaction);
                        Log.d(DASHBOARD, "Added transaction to history: " + transaction.getDescription());
                        calculate(history);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Calculates grand total amount owed by user to all friends and grand total owed to user by
     * all friends then populates the owedBy and owedTo TextViews.
     */
    private void calculate(ArrayList<Transaction> history) {

        double owedTo = 0.0;
        double owedBy = 0.0;

        for (Transaction t: history) {
            double amount = Double.parseDouble(t.getAmount());
            if (t.getLenderDisplayName().equals(userDisplayName)) {
                owedTo += amount;
            } else {
                owedBy += amount;
            }
        }

        NumberFormat formatter = new DecimalFormat("#0.00");
        owedByTextView.setText("$" + formatter.format(owedBy));
        owedToTextView.setText("$" + formatter.format(owedTo));
    }

}

package com.ijruiz.cs126.iouapplication;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
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

public class FriendDetailActivity extends AppCompatActivity {

    //Log.d() TAG
    public static final String FRIEND_DETAIL = "FRIEND_DETAIL";

    //Keywords for constructing Firebase Database paths
    public static final String UIDS_STARTPOINT = "uids/";
    public static final String USERS_STARTPOINT = "users/";
    public static final String TRANSACTION_HISTORY_ENDPOINT = "/transactionHistory";

    private String userDisplayName = "";

    TextView fdDisplayName;
    TextView fdEmail;
    TextView fdOwedHeader;
    TextView fdOwedTextView;

    Button removeFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        fdDisplayName = (TextView) findViewById(R.id.fdDisplayNameTextView);
        fdEmail = (TextView) findViewById(R.id.fdEmailTextView);

        fdOwedHeader = (TextView) findViewById(R.id.fdOwedHeader);
        fdOwedTextView = (TextView) findViewById(R.id.fdOwedTextView);

        removeFriendButton = (Button) findViewById(R.id.fdRemoveFriendButton);

        Intent intent = getIntent();
        final User friend = intent.getParcelableExtra(FriendListViewAdapter.USER);
        final String friendDisplayName = friend.getDisplayName();

        fdDisplayName.setText(friendDisplayName);
        fdEmail.setText(friendDisplayName);

        calculateTotalOwed(friendDisplayName);

        removeFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popRemoveFriendDialog(friendDisplayName);
            }
        });
    }

    private void popRemoveFriendDialog(final String friendDisplayName) {

        final Dialog confirmationDialog = new Dialog(FriendDetailActivity.this);
        confirmationDialog.setContentView(R.layout.dialog_confirm_layout);
        confirmationDialog.show();

        final Button yesButton = (Button) confirmationDialog.findViewById(R.id.yesButton);
        final Button noButton = (Button) confirmationDialog.findViewById(R.id.noButton);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                String cUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + cUserUID);
                uidRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userDisplayName = dataSnapshot.getValue(String.class);
                        FirebaseOperations.removeFriend(userDisplayName, friendDisplayName);
                        Toast.makeText(FriendDetailActivity.this, friendDisplayName + " was successfully unfriended.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                confirmationDialog.cancel();
                startActivity(new Intent(FriendDetailActivity.this, FriendListActivity.class));
                finish();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmationDialog.cancel();
            }
        });

    }

    /**
     * Calculates the total owed by the user to a friend and the total owed to the user by the same friend
     * and populates the owedTo and owedBy TextViews.
     * @param friendDisplayName user's friend
     */
    private void calculateTotalOwed(final String friendDisplayName) {

        //downloading transaction history
        final ArrayList<Transaction> history = new ArrayList<>();

        String cUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + cUserUID);

        uidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDisplayName = dataSnapshot.getValue(String.class);
                Log.d(FRIEND_DETAIL, "Calculating total owed - Retrieved user: " + userDisplayName);

                CountDownLatch cdl = new CountDownLatch(1);

                DatabaseReference userHistoryRef = firebaseDatabase.getReference(USERS_STARTPOINT + userDisplayName + TRANSACTION_HISTORY_ENDPOINT);
                userHistoryRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Transaction transaction = dataSnapshot.getValue(Transaction.class);
                        history.add(transaction);
                        Log.d(FRIEND_DETAIL, "Added transaction to history: " + transaction.getDescription());
                        calculate(history, friendDisplayName);
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
                    cdl.await(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    Log.d(FRIEND_DETAIL, "CountDownLatch error when calculating owed.");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Calculates the total owed by the user to a friend and the total owed to the user by the same friend
     * and populates the owedTo and owedBy TextViews.
     * @param history user's transaction history
     * @param friendDisplayName user's friend
     */
    private void calculate(ArrayList<Transaction> history, String friendDisplayName) {

        double owedTo = 0.0;
        double owedBy = 0.0;

        for (Transaction t: history) {
            if (t.getLenderDisplayName().equals(friendDisplayName) || t.getBorrowerDisplayName().equals(friendDisplayName)) {
                double amount = Double.parseDouble(t.getAmount());
                if (t.getLenderDisplayName().equals(userDisplayName)) {
                    owedTo += amount;
                } else {
                    owedBy += amount;
                }
            }
        }


        NumberFormat formatter = new DecimalFormat("#0.00");
        double difference = 0.00;


        if (owedTo > owedBy) {
            //FRIEND owes you difference
            fdOwedHeader.setText(friendDisplayName + " owes you:");
            difference = owedTo - owedBy;
            fdOwedTextView.setText("$" + formatter.format(difference));
        } else if (owedBy > owedTo) {
            //You owe friend difference
            fdOwedHeader.setText("You owe " + friendDisplayName + " :");
            difference = owedBy - owedTo;
            fdOwedTextView.setText("$" + formatter.format(difference));
        } else {
            fdOwedHeader.setText("You and " + friendDisplayName + " are even!");
            fdOwedTextView.setText("$" + formatter.format(difference));
        }



    }
}

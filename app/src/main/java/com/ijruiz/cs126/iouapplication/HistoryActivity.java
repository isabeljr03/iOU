package com.ijruiz.cs126.iouapplication;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    //Log.d() TAG
    public static final String HISTORY = "HISTORY";

    //Keywords for constructing Firebase Database paths.
    public static final String USERS_STARTPOINT = "users/";
    public static final String UIDS_STARTPOINT = "uids/";
    public static final String TRANSACTION_HISTORY_ENDPOINT = "/transactionHistory";
    public static final String FRIENDS_ENDPOINT = "/friends";

    private FloatingActionButton hAddFAB;
    private Button hDashboardButton;
    private Button hFriendListButton;


    private ListView listView;
    private HistoryListViewAdapter historyListViewAdapter;
    private List<Transaction> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        hAddFAB = (FloatingActionButton) findViewById(R.id.fAddFAB);
        hDashboardButton = (Button) findViewById(R.id.hDashboardButton);
        hFriendListButton = (Button) findViewById(R.id.hFriendListButton);

        listView = (ListView) findViewById(R.id.hListView);

        history = new ArrayList<>();

        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + currentUserUID);

        //getting user's display name to download user's transaction history
        uidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String cUserDisplayName = dataSnapshot.getValue(String.class);
                Log.d(HISTORY, "Getting history - Retrieved displayName: " + cUserDisplayName);

                //reading user's transaction history
                DatabaseReference userHistoryRef = firebaseDatabase.getReference(USERS_STARTPOINT + cUserDisplayName + TRANSACTION_HISTORY_ENDPOINT);
                userHistoryRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        history.add(dataSnapshot.getValue(Transaction.class));
                        Log.d(HISTORY, "Getting history - Added transaction: " + dataSnapshot.getKey());
                        historyListViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        historyListViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        historyListViewAdapter.notifyDataSetChanged();
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


        historyListViewAdapter = new HistoryListViewAdapter(HistoryActivity.this, history);
        listView.setAdapter(historyListViewAdapter);

        hAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(HistoryActivity.this);
                dialog.setContentView(R.layout.dialog_add_transaction_layout);
                dialog.show();

                final EditText dateEditText = (EditText) dialog.findViewById(R.id.dateEditText);
                final EditText amountEditText = (EditText) dialog.findViewById(R.id.amountEditText);
                final EditText friendEditText = (EditText) dialog.findViewById(R.id.friendEditText);
                final EditText payByEditText = (EditText) dialog.findViewById(R.id.payByEditText);
                final EditText locationEditText = (EditText) dialog.findViewById(R.id.locationEditText);
                final EditText notesEditText = (EditText) dialog.findViewById(R.id.notesEditText);
                final CheckBox lenderCheckBox = (CheckBox) dialog.findViewById(R.id.lenderCheckBox);


                final Button addTransactionButton = (Button) dialog.findViewById(R.id.dbAddTransactionButton);
                addTransactionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //TODO: Validate transaction fields for dialog boxes.
                        //All fields must be filled, notes can't have symbols, dates should be valid,
                        //TODO: Provide Toast messages for incorrect output.

                        final String dateOfTransaction = dateEditText.getText().toString();
                        final String payByDate = payByEditText.getText().toString();
                        final String friendDisplayName = friendEditText.getText().toString();
                        final String amount = amountEditText.getText().toString();
                        final String location = locationEditText.getText().toString();
                        final String notes = notesEditText.getText().toString();

                        final boolean validInputTransaction = Validator.validateInputTransaction(dateOfTransaction,
                                payByDate, friendDisplayName, amount, location, notes);

                        //getting user's display name to add transaction
                        uidRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String cUserDisplayName = dataSnapshot.getValue(String.class);
                                Log.d(HISTORY, "Adding transaction - Retrieved displayName: " + cUserDisplayName);

                                //must validate transaction fields and make sure friendDisplayName is in user's friendList

                                DatabaseReference userFriendsRef = firebaseDatabase.getReference(USERS_STARTPOINT
                                        + cUserDisplayName + FRIENDS_ENDPOINT);
                                userFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (validInputTransaction && dataSnapshot.hasChild(friendDisplayName)) {

                                            if (lenderCheckBox.isChecked()) {
                                                Transaction t = new Transaction(amount, dateOfTransaction,
                                                        payByDate, location, notes, cUserDisplayName, friendDisplayName);
                                                FirebaseOperations.addTransaction(t);
                                            } else {
                                                Transaction t = new Transaction(amount, dateOfTransaction,
                                                        payByDate, location, notes, friendDisplayName, cUserDisplayName);
                                                FirebaseOperations.addTransaction(t);
                                            }

                                            Toast.makeText(HistoryActivity.this, "Transaction successfully added.", Toast.LENGTH_SHORT).show();
                                            dialog.cancel();

                                        } else {

                                            if (!validInputTransaction) {
                                                Toast.makeText(HistoryActivity.this, "All fields must be filled correctly.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(HistoryActivity.this, "Friend not found.", Toast.LENGTH_SHORT).show();
                                            }

                                        }
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
                });

                final Button cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }
        });

        hDashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, DashboardActivity.class));
                finish();
            }
        });

        hFriendListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, FriendListActivity.class));
                finish();
            }
        });

    }
}

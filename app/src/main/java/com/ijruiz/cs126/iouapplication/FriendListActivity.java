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

public class FriendListActivity extends AppCompatActivity {

    //Log.d() TAG
    public static final String FRIEND_LIST = "FRIEND_LIST";

    //Keywords for constructing Firebase Database paths
    public static final String UIDS = "uids";
    public static final String UIDS_STARTPOINT = "uids/";
    public static final String USERS = "users";
    public static final String USERS_STARTPOINT = "users/";
    public static final String FRIENDS_ENDPOINT = "/friends";

    private FloatingActionButton fAddFAB;
    private Button fDashboardButton;
    private Button fHistoryButton;

    private ListView listView;
    private FriendListViewAdapter friendListViewAdapter;
    private List<User> friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        fAddFAB = (FloatingActionButton) findViewById(R.id.fAddFAB);
        fDashboardButton = (Button) findViewById(R.id.fDashboardButton);
        fHistoryButton = (Button) findViewById(R.id.fHistoryButton);

        listView = (ListView) findViewById(R.id.fListView);

        friends = new ArrayList<>();

        //downloading friend's list
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + currentUserUID);

        uidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String cUserDisplayName = dataSnapshot.getValue(String.class);
                Log.d(FRIEND_LIST, "Getting history - Retrieved displayName: " + cUserDisplayName);

                //reading user's friend list
                DatabaseReference userFriendRef = firebaseDatabase.getReference(USERS_STARTPOINT + cUserDisplayName + FRIENDS_ENDPOINT);
                userFriendRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String friendDisplayName = dataSnapshot.getValue(String.class);

                        //adds each friend as a user object to friends array list
                        DatabaseReference friendRef = firebaseDatabase.getReference(USERS_STARTPOINT + friendDisplayName);
                        friendRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                friends.add(user);
                                Log.d(FRIEND_LIST, "Getting friends - Added friend: " + dataSnapshot.getKey());
                                friendListViewAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        friendListViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        friendListViewAdapter.notifyDataSetChanged();
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

        friendListViewAdapter = new FriendListViewAdapter(FriendListActivity.this, friends);
        listView.setAdapter(friendListViewAdapter);

        fAddFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popAddFriendDialog();
            }
        });

        fDashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FriendListActivity.this, DashboardActivity.class));
                finish();
            }
        });

        fHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FriendListActivity.this, HistoryActivity.class));
                finish();
            }
        });
    }

    private void popAddFriendDialog() {

        final Dialog dialog = new Dialog(FriendListActivity.this);
        dialog.setContentView(R.layout.dialog_add_friend_layout);
        dialog.show();

        final Button fAddFriendButton = (Button) dialog.findViewById(R.id.fAddFriendButton);
        final Button fCancelButton = (Button) dialog.findViewById(R.id.fCancelButton);

        fAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(dialog);
            }
        });

        fCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }

    private void addFriend(final Dialog dialog) {

        final EditText fDisplayNameEditText = (EditText) dialog.findViewById(R.id.fDisplayNameEditText);
        final String friendDisplayName = fDisplayNameEditText.getText().toString();

        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        DatabaseReference usersRef = firebaseDatabase.getReference(USERS);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //validates that display name is a valid Firebase Database path and exists as a user
                if (Validator.validateDisplayName(friendDisplayName) && dataSnapshot.hasChild(friendDisplayName)) {

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    String currentUserUID = auth.getCurrentUser().getUid();

                    //getting user display name to add friend to friend list
                    DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + currentUserUID);
                    uidRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String userDisplayName = dataSnapshot.getValue(String.class);
                            Log.d(FRIEND_LIST, "Adding friend - Retrieved current user: " + userDisplayName);

                            //adding friend to friend list
                            FirebaseOperations.addFriend(userDisplayName, friendDisplayName);
                            Toast.makeText(FriendListActivity.this, "Added friend successfully.", Toast.LENGTH_SHORT).show();

                            dialog.cancel();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    if (!Validator.validateDisplayName(friendDisplayName)) {
                        Toast.makeText(FriendListActivity.this, "Invalid display name.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FriendListActivity.this, "Friend not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

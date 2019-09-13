package com.ijruiz.cs126.iouapplication;

import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by isabelruiz on 5/1/17.
 * Methods that read, write, and process data from the Firebase Realtime Database
 */
public class FirebaseOperations {

    //Lod.d() TAG
    public static final String FIRE_OPS = "FIRE_OPS";

    //Keywords for constructing Firebase Database paths.
    public static final String USERS = "users";
    public static final String USERS_STARTPOINT = "users/";
    public static final String UIDS = "uids";
    public static final String TRANSACTION_HISTORY = "transactionHistory";
    public static final String TRANSACTION_HISTORY_MIDPOINT = "/transactionHistory/";
    public static final String FRIENDS = "friends";
    public static final String FRIENDS_MIDPOINT = "/friends/";

    /**
     * Adds a new user child to the Firebase Realtime Database.
     * @param user user being added
     */
    public static void addUser(final User user) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //adds value: user pojo with key: user's display name
        DatabaseReference usersReference = firebaseDatabase.getReference(USERS);
        usersReference.child(user.getDisplayName()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Added to /users/: " + user.getDisplayName());
                } else {
                    Log.d(FIRE_OPS, "Failed to add to /users/: " + user.getDisplayName());
                }
            }
        });

        //adds value: user's display name with key: user's uid
        DatabaseReference uidReference = firebaseDatabase.getReference(UIDS);
        uidReference.child(user.getUid()).setValue(user.getDisplayName()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Added to /uids/: " + user.getUid());
                } else {
                    Log.d(FIRE_OPS, "Failed to add to /uids/: " + user.getUid());
                }
            }
        });

        Log.d(FIRE_OPS, "User added: " + user.getDisplayName());
    }

    /**
     * Writes a transaction to the Firebase Realtime Database for both involved users.
     * @param t transaction to be written to the database
     */
    public static void addTransaction(final Transaction t) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //add transaction to lender's transactionHistory
        DatabaseReference lenderRef = firebaseDatabase.getReference(USERS_STARTPOINT + t.getLenderDisplayName());
        lenderRef.child(TRANSACTION_HISTORY).child(t.getNotes()).setValue(t).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Added transaction for: " + t.getLenderDisplayName());
                } else {
                    Log.d(FIRE_OPS, "Failed to add transaction for: " + t.getLenderDisplayName());
                }
            }
        });

        //add transaction to borrower's transactionHistory
        DatabaseReference borrowerRef = firebaseDatabase.getReference(USERS_STARTPOINT + t.getBorrowerDisplayName());
        borrowerRef.child(TRANSACTION_HISTORY).child(t.getNotes()).setValue(t).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Added transaction for: " + t.getBorrowerDisplayName());
                } else {
                    Log.d(FIRE_OPS, "Failed to add transaction for: " + t.getBorrowerDisplayName());
                }
            }
        });
    }

    /**
     * Removes a transaction from lender and borrower's transactionHistory.
     * @param t transaction being removed
     */
    public static void removeTransaction(final Transaction t) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //remove from lender's transactionHistory
        DatabaseReference lenderTransactionRef = firebaseDatabase.getReference(USERS_STARTPOINT +
                t.getLenderDisplayName() + TRANSACTION_HISTORY_MIDPOINT + t.getNotes());
        lenderTransactionRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Removed transaction from lender's history: " + t.getDescription());
                } else {
                    Log.d(FIRE_OPS, "Did not remove transaction from lender's history.");

                }
            }
        });

        //remove from borrower's transactionHistory
        DatabaseReference borrowerTransactionRef = firebaseDatabase.getReference(USERS_STARTPOINT +
                t.getBorrowerDisplayName() + TRANSACTION_HISTORY_MIDPOINT + t.getNotes());
        borrowerTransactionRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Removed transaction from borrower's history: " + t.getDescription());
                } else {
                    Log.d(FIRE_OPS, "Did not remove transaction from borrower's history.");
                }
            }
        });
    }

    /**
     * Mutually adds the user and friend to each other's friend list in the Firebase Realtime Database
     * @param userDisplayName current user who is adding friend
     * @param friendDisplayName friend who is added
     */
    public static void addFriend(final String userDisplayName, final String friendDisplayName) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //add friend to user's friends
        DatabaseReference userRef = firebaseDatabase.getReference(USERS_STARTPOINT + userDisplayName);
        userRef.child(FRIENDS).child(friendDisplayName).setValue(friendDisplayName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Added friend: " + friendDisplayName + " for: " + userDisplayName);
                } else {
                    Log.d(FIRE_OPS, "Failed to added friend: " + friendDisplayName + " for: " + userDisplayName);
                }
            }
        });


        //add user to friend's friends
        DatabaseReference friendRef = firebaseDatabase.getReference(USERS_STARTPOINT + friendDisplayName);
        friendRef.child(FRIENDS).child(userDisplayName).setValue(userDisplayName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Added friend: " + userDisplayName + " for: " + friendDisplayName);
                } else {
                    Log.d(FIRE_OPS, "Failed to added friend: " + userDisplayName + " for: " + friendDisplayName);
                }
            }
        });
    }

    /**
     * Mutually removes user and friend from each other's friends list in the Firebase Realtime Database
     * @param userDisplayName user removing friend
     * @param friendDisplayName friend being removed
     */
    public static void removeFriend(final String userDisplayName, final String friendDisplayName) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        //remove friend from user's friends
        final DatabaseReference userFriendRef = firebaseDatabase.getReference(USERS_STARTPOINT +
                userDisplayName + FRIENDS_MIDPOINT + friendDisplayName);
        userFriendRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Successfully removed friend: " + friendDisplayName + " for: " + userDisplayName);
                } else {
                    Log.d(FIRE_OPS, "Failed to remove friend: " + friendDisplayName + " for: " + userDisplayName);
                }
            }
        });

        //remove user from friend's friends
        DatabaseReference friendUserRef = firebaseDatabase.getReference(USERS_STARTPOINT +
                friendDisplayName + FRIENDS_MIDPOINT + userDisplayName);
        friendUserRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(FIRE_OPS, "Successfully removed friend: " + userDisplayName + " for: " + friendDisplayName);
                } else {
                    Log.d(FIRE_OPS, "Failed to remove friend: " + userDisplayName + " for: " + friendDisplayName);
                }

            }
        });

    }


}

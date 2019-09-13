package com.ijruiz.cs126.iouapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by isabelruiz on 5/1/17.
 * Source: tori san on YouTube - https://www.youtube.com/watch?v=YMJSBHAZsso
 * Class for custom ListView adapter for Transaction History items.
 */
public class HistoryListViewAdapter extends BaseAdapter {

    //Log.d() tag
    public static final String HISTORY_ADAPTER = "HISTORY_ADAPTER";

    //keyword for constructing Firebase Database paths
    public static final String UIDS_STARTPOINT = "uids/";

    public static final String PAY_BY = "Pay by: ";
    public static final String LOCATION = "Location: ";
    public static final String NOTES = "Notes: ";

    private Context context;
    private List<Transaction> history;

    //constructor

    public HistoryListViewAdapter(Context context, List<Transaction> history) {
        this.context = context;
        this.history = history;
    }

    //override methods

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public Object getItem(int position) {
        return history.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {

        View view = View.inflate(context, R.layout.transaction_list_item, null);

        final TextView descriptionTextView = (TextView) view.findViewById(R.id.descriptionTextView);
        TextView payByTextView = (TextView) view.findViewById(R.id.payByTextView);
        TextView locationTextView = (TextView) view.findViewById(R.id.locationTextView);
        TextView notesTextView = (TextView) view.findViewById(R.id.notesTextView);

        ImageButton deleteImageButton = (ImageButton) view.findViewById(R.id.deleteImageButton);

        final Transaction cTransaction = history.get(position);

        //getting user to personalize description
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String cUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference uidRef = firebaseDatabase.getReference(UIDS_STARTPOINT + cUserUID);
        uidRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userDisplayName = dataSnapshot.getValue(String.class);
                Log.d(HISTORY_ADAPTER, "Personalizing description - retrieved: " + userDisplayName);
                descriptionTextView.setText(cTransaction.getDescription(userDisplayName));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        payByTextView.setText(PAY_BY + cTransaction.getPayByDate());
        locationTextView.setText(LOCATION + cTransaction.getLocation());
        notesTextView.setText(NOTES + cTransaction.getNotes());

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseOperations.removeTransaction(cTransaction);
                Toast.makeText(context, "Transaction: " + cTransaction.getDescription() + " successfully resolved.", Toast.LENGTH_LONG).show();
                HistoryListViewAdapter.this.notifyDataSetChanged();
                context.startActivity(new Intent(context, HistoryActivity.class));
            }
        });

        return view;
    }
}

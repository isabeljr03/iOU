package com.ijruiz.cs126.iouapplication;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by isabelruiz on 5/2/17.
 * Source: tori san on YouTube - https://www.youtube.com/watch?v=YMJSBHAZsso
 * Class for custom ListView adapter for Friend (User) items.
 */
public class FriendListViewAdapter extends BaseAdapter {

    public static final String USER = "USER";

    private Context context;
    private List<User> friends;

    //constructor

    public FriendListViewAdapter(Context context, List<User> friends) {
        this.context = context;
        this.friends = friends;
    }

    //override methods

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = View.inflate(context, R.layout.friend_list_item, null);

        TextView displayNameTextView = (TextView) view.findViewById(R.id.fDisplayName);

        final User friend = friends.get(position);

        displayNameTextView.setText(friend.getDisplayName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), FriendDetailActivity.class);
                intent.putExtra(USER, friend);
                v.getContext().startActivity(intent);
            }
        });

        return view;
    }
}

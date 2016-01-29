package com.delta.pragyanoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rb on 19/1/16.
 */
public class ContactAdapter extends ArrayAdapter{
    ArrayList<User> users;
    public ContactAdapter(Context context, ArrayList<User> users) {
        super(context,R.layout.adapter,users);
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.adapter,parent,false);
        TextView name = (TextView)view.findViewById(R.id.textView_task_name);
        String year;
        if(users.get(position).user_type.equals("0")||users.get(position).user_type.equals("1"))
            year = "4th Year";
        else if(users.get(position).user_type.equals("2"))
            year = "3rd Year";
        else
            year = "2nd Year";
        name.setText(users.get(position).user_name+" - "+year);
        return view;
    }
}


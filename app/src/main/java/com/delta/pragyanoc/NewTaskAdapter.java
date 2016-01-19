package com.delta.pragyanoc;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by rahul on 19/1/16.
 */
public class NewTaskAdapter extends ArrayAdapter<User> {
    Context context;
    public NewTaskAdapter(Context context, User[] objects) {
        super(context, R.layout.adapter_layout, objects);
        this.context = context;
    }
}

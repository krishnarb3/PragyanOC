package com.delta.pragyanoc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by rb on 16/1/16.
 */
public class CustomTaskAdapter extends ArrayAdapter<String> {

    ArrayList<String> tasknames;
    ArrayList<String> taskassignees;
    ArrayList<String> task_status;
    public CustomTaskAdapter(Context context, ArrayList<String> tasknames, ArrayList<String> assignees,ArrayList<String> task_status) {
        super(context,R.layout.adapter,tasknames);
        this.tasknames = tasknames;
        this.taskassignees = assignees;
        this.task_status = task_status;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.adapter,parent,false);
        TextView name = (TextView)view.findViewById(R.id.textView_task_name);
        name.setText(tasknames.get(position));
        TextView assignees = (TextView)view.findViewById(R.id.textView_task_assignees);
        assignees.setText(taskassignees.get(position));
        if(task_status.get(position).equals("0"))
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorTaskIncomplete));
        else if(task_status.get(position).equals("1"))
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorTaskInProgress));
        else
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorTaskCompleted));
        return view;
    }
}

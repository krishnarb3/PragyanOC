package com.delta.pragyanoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TaskAcc2TeamActivity extends AppCompatActivity {
    String allTasks;
    String team_id;
    String team_name;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_acc2_team);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        team_id = bundle.getString("team_id");
        team_name = bundle.getString("team_name");
        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        try {
            allTasks = prefs.getString("allTasks","");
            JSONObject alltasksObject = new JSONObject(allTasks);
            JSONArray message = alltasksObject.getJSONArray("message");
            ArrayList<Task> tasksArray = new ArrayList<>();
            ArrayList<String> taskDisplay = new ArrayList<>();
            for(int i=0;i<message.length();i++) {
                JSONObject taskObject = message.getJSONObject(i);
                Task task = new Task();
                task.team_name = taskObject.getString("team_name");
                task.team_id = taskObject.getString("team_id");
                task.task_completed = taskObject.getString("task_completed");
                task.task_id = taskObject.getString("task_id");
                task.task_name = taskObject.getString("task_name");
                task.task_assigned = taskObject.getString("task_assigned");
                tasksArray.add(task);
                taskDisplay.add(task.task_name+ " - "+task.task_assigned);
            }
            ListView listView = (ListView)findViewById(R.id.listview);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,taskDisplay);
            listView.setAdapter(adapter);
        }catch(Exception e) {
            Log.d(Utilities.LOGGING,e+"");
            Toast.makeText(this,"Error occurred",Toast.LENGTH_SHORT).show();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public class Task {
        public String task_id;
        public String task_name;
        public String task_completed;
        public String team_id;
        public String team_name;
        public String task_assigned;
    }


}

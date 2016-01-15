package com.delta.pragyanoc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
            String allTasks = new AsyncGetAllTasks().execute().get();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("allTasks",allTasks);
            editor.commit();
            allTasks = prefs.getString("allTasks","");
            JSONObject alltasksObject = new JSONObject(allTasks);
            JSONArray message = alltasksObject.getJSONArray("message");
            Log.d(Utilities.LOGGING,message.toString());
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
                tasksArray.add(task);
                taskDisplay.add(task.task_name+ " - ");
            }
            ListView listView = (ListView)findViewById(R.id.listview);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,taskDisplay);
            listView.setAdapter(adapter);
            try {
                JSONObject tasksJSON = new JSONObject(allTasks);
                JSONArray tasks = tasksJSON.getJSONArray("message");
                for(int i=0;i<tasks.length();i++) {
                    JSONObject task = tasks.getJSONObject(i);
                    View view;
                    Log.d(Utilities.LOGGING,task.toString());
                    switch(Integer.parseInt(task.getString("task_completed"))) {
                        case 0 : view = listView.getAdapter().getView(i,null,null);
                            Log.d(Utilities.LOGGING,"INside case 0");
                            view.setBackgroundColor(getResources().getColor(R.color.colorTaskIncomplete));
                            break;
                        case 1 : view = listView.getAdapter().getView(i,null,null);
                            view.setBackgroundColor(getResources().getColor(R.color.colorTaskInProgress));
                            break;
                        case 2 : view = listView.getAdapter().getView(i,null,null);
                            view.setBackgroundColor(getResources().getColor(R.color.colorTaskCompleted));
                            break;
                    }
                }
            }catch(Exception e) {
                Log.e(Utilities.LOGGING,e+"");
            }
        }catch(Exception e) {
            Log.d(Utilities.LOGGING,e+"");
            Toast.makeText(this,"Error occurred",Toast.LENGTH_SHORT).show();
        }
        FloatingActionButton fabNewMessage = (FloatingActionButton) findViewById(R.id.fab_new_message);
        fabNewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TaskAcc2TeamActivity.this,ChatActivity.class);
                startActivity(intent);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        FloatingActionButton fabNewTask = (FloatingActionButton) findViewById(R.id.fab_new_task);
        fabNewTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TaskAcc2TeamActivity.this,NewTaskActivity.class);
                intent.putExtra("team_id",team_id);
                startActivity(intent);
            }
        });
    }

    public class Task {
        public String task_id;
        public String task_name;
        public String task_completed;
        public String team_id;
        public String team_name;
    }

    public class AsyncGetAllTasks extends AsyncTask<Void,Void,String> {

        String result = "";
        URL url;
        @Override
        protected String doInBackground(Void... voids) {
            try {
                url = new URL(Utilities.getAllTasksUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            String user_roll = prefs.getString("user_roll","");
            String user_secret = prefs.getString("user_secret","");
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_secret",user_secret);
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, String> param = iterator.next();
                bodyBuilder.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    bodyBuilder.append('&');
                }
            }
            String body = bodyBuilder.toString();
            Log.v(Utilities.LOGGING,"Posting '"+body+"' to "+url);
            byte[] bytes = body.getBytes();
            HttpURLConnection conn = null;
            try {
                Log.d("URL", "> " + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setFixedLengthStreamingMode(bytes.length);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                CharSequence charSequence = "status";
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        result = result + line;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            catch(Exception e) {
                Log.e(Utilities.LOGGING, e + "");
            }
            Log.d(Utilities.LOGGING,result);
            return result;
        }
    }


}

package com.delta.pragyanoc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    protected void onCreate(final Bundle savedInstanceState) {
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
            allTasks = new AsyncGetAllTasks().execute().get();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("allTasks",allTasks);
            editor.apply();
            allTasks = prefs.getString("allTasks","");
            JSONObject alltasksObject = new JSONObject(allTasks);
            final JSONArray message = alltasksObject.getJSONArray("message");
            Log.d(Utilities.LOGGING,message.toString());
            final ArrayList<Task> tasksArray = new ArrayList<>();
            ArrayList<String> taskDisplay = new ArrayList<>();
            ArrayList<String> taskNames = new ArrayList<>();
            ArrayList<String> taskAssignees = new ArrayList<>();
            ArrayList<String> taskStatus = new ArrayList<>();
            for(int i=0;i<message.length();i++) {
                JSONObject taskObject = message.getJSONObject(i);
                Task task = new Task();
                task.team_name = taskObject.getString("team_name");
                task.team_id = taskObject.getString("team_id");
                task.task_completed = taskObject.getString("task_completed");
                task.task_id = taskObject.getString("task_id");
                task.task_name = taskObject.getString("task_name");
                JSONArray task_assignees_json = taskObject.getJSONArray("assigned");
                taskAssignees.add(task_assignees_json.toString());
                if(team_id.equals(task.team_id)) {
                    taskNames.add(task.task_name);
                    taskStatus.add(task.task_completed);
                    tasksArray.add(task);
                    taskDisplay.add(task.task_name + " - ");
                }
            }
            ListView listView = (ListView)findViewById(R.id.listview);   //TODO MAKE CUSTOM ADAPTER TO HANDLE BACKGROUND COLOR CHANGE
            CustomTaskAdapter customTaskAdapter = new CustomTaskAdapter(this,taskNames,taskAssignees,taskStatus);
            listView.setAdapter(customTaskAdapter);
            final String user_roll = prefs.getString("user_roll","");
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,final int i, long l) {
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(TaskAcc2TeamActivity.this);
                    builderSingle.setIcon(R.drawable.ic_media_route_off_mono_dark);
                    builderSingle.setTitle("Select Task Status");
                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            TaskAcc2TeamActivity.this,
                            android.R.layout.select_dialog_singlechoice);
                    arrayAdapter.add("Not started");
                    arrayAdapter.add("In progress");
                    arrayAdapter.add("Completed");
                    arrayAdapter.add("See Messages");
                    builderSingle.setNegativeButton(
                            "cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builderSingle.setAdapter(
                            arrayAdapter,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String strName = arrayAdapter.getItem(which);
                                    try {
                                        Boolean isPresent = false;
                                        String res_target_users = new AsyncgetTargetUserTasks().execute(user_roll).get();
                                        JSONObject object = new JSONObject(res_target_users);
                                        JSONArray msg = object.getJSONArray("message");
                                        for(int i=0;i<msg.length();i++) {
                                            if(((JSONObject)message.get(i)).getString("task_id").equals(tasksArray.get(i).task_id))
                                                isPresent = true;
                                        }
                                        String user_type = prefs.getString("user_type","");
                                        String task_status;
                                        switch(which) {
                                            case 0: task_status = "0";break;
                                            case 1: task_status = "1";break;
                                            case 2: task_status = "2";break;
                                            case 3: Intent intent = new Intent(TaskAcc2TeamActivity.this,ChatActivity.class);
                                                    intent.putExtra("task_id",tasksArray.get(i).task_id);
                                                    startActivity(intent);
                                            default: task_status = "1";break;
                                        }
                                        if(isPresent||user_type.equals("0")||user_type.equals("1")) {
                                            Log.d(Utilities.LOGGING,"Updating"+tasksArray.get(i).task_id+"withstatus"+task_status);
                                            String res = new AsyncUpdateTaskStatus().execute(tasksArray.get(i).task_id,task_status,team_id,prefs.getString("user_roll",""),prefs.getString("user_secret","")).get();
                                        }
                                        else {
                                            Toast.makeText(TaskAcc2TeamActivity.this, "You dont have Permissions", Toast.LENGTH_LONG).show();
                                            dialog.dismiss();
                                        }
                                    }catch(Exception e) {
                                        Log.e(Utilities.LOGGING,""+e);
                                        Toast.makeText(TaskAcc2TeamActivity.this,"No Permissions or Bad Internet",Toast.LENGTH_LONG);
                                    }
                                    AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                            TaskAcc2TeamActivity.this);
                                    builderInner.setMessage(strName);
                                    if(which!=3) {
                                        builderInner.setTitle("Your Selected Item is");
                                        builderInner.setPositiveButton(
                                                "Ok",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(
                                                            DialogInterface dialog,
                                                            int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        builderInner.show();
                                    }
                                }
                            });
                    builderSingle.show();
                }
            });

        }catch(Exception e) {
            Log.d(Utilities.LOGGING,e+"");
            Toast.makeText(this,"Error occurred",Toast.LENGTH_SHORT).show();
        }
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


    public class AsyncgetTargetUserTasks extends AsyncTask<String,Void,String> {

        String result = "";
        URL url;
        @Override
        protected String doInBackground(String... strings) {
            try {
                url = new URL(Utilities.getTargetUserTasksUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            String user_roll = prefs.getString("user_roll","");
            String user_secret = prefs.getString("user_secret","");
            String user_target_roll = strings[0];
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_secret",user_secret);
            params.put("user_target_roll",user_target_roll);
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

package com.delta.pragyanoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NewTaskActivity extends AppCompatActivity {

    SharedPreferences prefs;
    String team_id,type,task_id,task_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final EditText editText = (EditText) findViewById(R.id.edit_text);
        final EditText editTextAssignees = (EditText) findViewById(R.id.edit_text_assignees);
        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("intentType","-1");
        task_id=bundle.getString("task_id","-1");
        task_name = bundle.getString("task_name","-1");
        team_id = bundle.getString("team_id");
        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        final String user_roll = prefs.getString("user_roll","");
        final String user_secret = prefs.getString("user_secret","");
        if(type.equals("2")){
            try {
                String result = new AsyncCreateNewTask().execute(user_roll, user_secret, team_id, task_id,"").get();
                Log.d("cruz",result);
                JSONObject success = new JSONObject(result);
                String status = success.getString("status_code");
                if(status.equals("200")){
                    Toast.makeText(this,"Successfully Deleted",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,"Deletion Failed. Try Again Later.",Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }
            finish();
        }
        if(type.equals("1")){

                editText.setText(task_name);
                editTextAssignees.setVisibility(View.GONE);
                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String task = editText.getText().toString();
                        String result;
                        try {
                            result = new AsyncCreateNewTask().execute(user_roll, user_secret, team_id, task_id, task).get();
                            JSONObject success = new JSONObject(result);
                            String status = success.getString("status_code");
                            if (status.equals("200")) {
                                Toast.makeText(NewTaskActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(NewTaskActivity.this, "Updation Failed. Try Again Later.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (InterruptedException | ExecutionException | JSONException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }
                });


        }
        else {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String task = editText.getText().toString();
                    String rollnos = editTextAssignees.getText().toString();
                    try {
                        String result = new AsyncCreateNewTask().execute(user_roll, user_secret, team_id, task,"").get();
                        JSONObject resultJSON = new JSONObject(result.substring(4,result.length()));
                        String task_id = ((JSONObject)resultJSON.get("message")).getString("task_id");
                        String result2 = new AsyncAssigntoTask().execute(user_roll,user_secret,task_id,rollnos).get();
                        if(result!=null&&!result.equals("")&&result2!=null&&!result2.equals(""))
                            finish();
                    }catch(Exception e) {
                        Log.e(Utilities.LOGGING,e+"");
                        Toast.makeText(NewTaskActivity.this,"Failed to create , try later",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
    public class AsyncCreateNewTask extends AsyncTask<String,Void,String> {
        URL url;
        String result;
        @Override
        protected String doInBackground(String... strings) {
            String user_roll = strings[0];
            String user_secret = strings[1];
            String team_id = strings[2];
            String task = strings[3];
            String task_name = strings[4];
            try {
                switch (type){
                    case "-1":
                        url = new URL(Utilities.getCreateNewTaskUrl());
                        break;
                    case "1" :
                        url = new URL(Utilities.UPDATE_TASK_URL);
                        break;
                    case "2":
                        url = new URL(Utilities.DELETE_TASK_URL);
                        break;
                }

            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_secret",user_secret);
            if(type.equals("-1")){
                params.put("team_id",team_id);
                params.put("task_name",task);
            }else if(type.equals("2") || type.equals("1")){
                params.put("task_id",task);
            }
            if (type.equals("1")){
                params.put("team_id",team_id);
                params.put("task_name",task_name);
            }

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
            return result;
        }
    }
    public class AsyncAssigntoTask extends AsyncTask<String,Void,String> {
        String user_roll;
        String user_secret;
        String task_id;
        String task_assignees;
        String result;
        URL url;
        @Override
        protected String doInBackground(String... strings) {
            user_roll = strings[0];
            user_secret = strings[1];
            task_id = strings[2];
            task_assignees = strings[3];
            try {
                url = new URL(Utilities.getAssignToTaskUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_secret",user_secret);
            params.put("task_id",task_id);
            params.put("assigned_list",task_assignees);
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
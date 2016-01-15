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

public class NewTaskActivity extends AppCompatActivity {

    SharedPreferences prefs;
    String team_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        team_id = bundle.getString("team_id");
        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        final String user_roll = prefs.getString("user_roll","");
        final String user_secret = prefs.getString("user_secret","");
        final EditText editText = (EditText) findViewById(R.id.edit_text);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editText.getText().toString();
                try {
                    String result = new AsyncCreateNewTask().execute(user_roll, user_secret, team_id, task).get();
                    if(result!=null&&!result.equals(""))
                        finish();
                }catch(Exception e) {
                    Log.e(Utilities.LOGGING,e+"");
                    Toast.makeText(NewTaskActivity.this,"Failed to create , try later",Toast.LENGTH_SHORT).show();
                }
            }
        });
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
            try {
                url = new URL(Utilities.getCreateNewTaskUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_secret",user_secret);
            params.put("team_id",team_id);
            params.put("task_name",task);
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

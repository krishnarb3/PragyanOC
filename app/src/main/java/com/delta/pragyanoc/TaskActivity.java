package com.delta.pragyanoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class TaskActivity extends AppCompatActivity {

    User member;
    String user_type;
    String result;
    ArrayList<String> tasksArrayList;
    ArrayAdapter<String> adapter;
    ListView listViewTasks;
    SharedPreferences prefs;
    private String newTask;
    String profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        member = bundle.getParcelable("memberName");

        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        profile = prefs.getString("profileDetails","");
        user_type = prefs.getString("user_type","");
        try {
            result = new AsyncGetAllTasks().execute().get();
            if(result!=null&&!result.equals("")) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("allTasks",result);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = prefs.getString("allTasks","");
        }
        Log.d(Utilities.LOGGING,result);

        listViewTasks = (ListView)findViewById(R.id.listView_tasks);
        try {
            JSONObject tasksJSON = new JSONObject(result);
            JSONArray tasks = tasksJSON.getJSONArray("message");
            for(int i=0;i<tasks.length();i++) {
                JSONObject task = tasks.getJSONObject(i);
                tasksArrayList.add(task.getString("task_name"));
            }
        } catch(Exception e) {
            Log.e(Utilities.LOGGING,e+"");
        }
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,tasksArrayList);
        listViewTasks.setAdapter(adapter);
        try {
            JSONObject tasksJSON = new JSONObject(result);
            JSONArray tasks = tasksJSON.getJSONArray("message");
            for(int i=0;i<tasks.length();i++) {
                JSONObject task = tasks.getJSONObject(i);
                View view;
                switch(Integer.parseInt(task.getString("task_completed"))) {
                    case 0 : view = listViewTasks.getAdapter().getView(i,null,null);
                             view.setBackgroundColor(getResources().getColor(R.color.colorTaskIncomplete));
                             break;
                    case 1 : view = listViewTasks.getAdapter().getView(i,null,null);
                             view.setBackgroundColor(getResources().getColor(R.color.colorTaskInProgress));
                             break;
                    case 2 : view = listViewTasks.getAdapter().getView(i,null,null);
                             view.setBackgroundColor(getResources().getColor(R.color.colorTaskCompleted));
                             break;
                }
            }
        }catch(Exception e) {
            Log.e(Utilities.LOGGING,e+"");
        }
        /*if(user_type.equals("1")|| user_type.equals("0")) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);
                    builder.setTitle("Add Task to this Member");
                    final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            newTask = input.getText().toString();

                            //String response = new Async
                            //TODO Api call
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });
        }*/
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

    public class AsyncChangeStatus extends AsyncTask<Void,Void,String> {
        URL url;
        @Override
        protected String doInBackground(Void... voids) {
            try {
                url = new URL(Utilities.getLoginUrl());
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

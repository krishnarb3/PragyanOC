package com.delta.pragyanoc;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TaskActivity extends AppCompatActivity {

    ArrayList<User> member;
    String user_target_roll;
    String user_type;
    String result;
    JSONArray message;
    ArrayList<String> tasksArrayList,taskIDArrayList;
    ArrayAdapter<String> adapter;
    ListView listViewTasks;
    SharedPreferences prefs;
    private String newTask;
    String profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        final ArrayList<Task> tasksArray = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        user_target_roll = bundle.getString("user_target_roll");
        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        profile = prefs.getString("profileDetails","");
        user_type = prefs.getString("user_type","");
        try {
            result = new AsyncgetTargetUserTasks().execute(user_target_roll).get();
            if(result!=null&&!result.equals("")) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("targetUserTasks",result);
                editor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = prefs.getString("targetUserTasks","");
        }
        Log.d(Utilities.LOGGING,result);

        listViewTasks = (ListView)findViewById(R.id.listView_tasks);
        try {
            JSONObject tasksJSON = new JSONObject(result);
            JSONArray tasks = tasksJSON.getJSONArray("message");
            message = tasks;
            tasksArrayList = new ArrayList<>();
            taskIDArrayList = new ArrayList<>();
            Task task;
            for(int i=0;i<message.length();i++) {
                JSONObject taskObject = message.getJSONObject(i);
                task = new Task();
                task.team_name = taskObject.getString("team_name");
                task.team_id = taskObject.getString("team_id");
                task.task_completed = taskObject.getString("task_completed");
                task.task_id = taskObject.getString("task_id");
                task.task_name = taskObject.getString("task_name");
                tasksArray.add(task);
            }

            for(int i=0;i<tasks.length();i++) {
                JSONObject taskItem = tasks.getJSONObject(i);
                tasksArrayList.add(taskItem.getString("task_name"));
                taskIDArrayList.add(taskItem.getString("task_id"));
            }
        } catch(Exception e) {
            Log.e(Utilities.LOGGING,e+"");
        }
        if(tasksArrayList!=null) {
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tasksArrayList){
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view;
                    try {
                        view = super.getView(position, convertView, parent);
                        JSONObject tasksJSON = new JSONObject(result);
                        JSONArray tasks = tasksJSON.getJSONArray("message");
                        for(int i=0;i<tasks.length();i++) {
                            JSONObject task = tasks.getJSONObject(i);
                            Log.d(Utilities.LOGGING,"task_completed"+task.getString("task_completed"));
                            switch(Integer.parseInt(task.getString("task_completed"))) {
                                case 0 : view.setBackgroundColor(getResources().getColor(R.color.colorTaskIncomplete));
                                    break;
                                case 1 : view.setBackgroundColor(getResources().getColor(R.color.colorTaskInProgress));
                                    break;
                                case 2 :view.setBackgroundColor(getResources().getColor(R.color.colorTaskCompleted));
                                    break;
                            }
                        }
                    }catch(Exception e) {
                        Log.e(Utilities.LOGGING,e+"");
                        view = super.getView(position, convertView, parent);
                    }
                    return view;
                }
            };
            listViewTasks.setAdapter(adapter);
            listViewTasks.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,final int i , long id) {
                            String taskID = taskIDArrayList.get(i);
                            try {
                                if(isEditable(taskID)){
                                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(TaskActivity.this);
                                    builderSingle.setIcon(R.drawable.ic_media_route_off_mono_dark);
                                    builderSingle.setTitle("Select Task Status");
                                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                            TaskActivity.this,
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
                                                        String res_target_users = new AsyncgetTargetUserTasks().execute(prefs.getString("user_roll","")).get();
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
                                                            case 3: Intent intent = new Intent(TaskActivity.this,ChatActivity.class);
                                                                intent.putExtra("task_id",tasksArray.get(i).task_id);
                                                                startActivity(intent);
                                                            default: task_status = "1";break;
                                                        }
                                                        if(isPresent||user_type.equals("0")||user_type.equals("1")) {
                                                            Log.d(Utilities.LOGGING,"Updating"+tasksArray.get(i).task_id+"withstatus"+task_status);
                                                            String res = new AsyncUpdateTaskStatus().execute(tasksArray.get(i).task_id,task_status,tasksArray.get(i).team_id,prefs.getString("user_roll",""),prefs.getString("user_secret","")).get();
                                                        }
                                                        else {
                                                            Toast.makeText(TaskActivity.this, "You dont have Permissions", Toast.LENGTH_LONG).show();
                                                            dialog.dismiss();
                                                        }
                                                    }catch(Exception e) {
                                                        Log.e(Utilities.LOGGING,""+e);
                                                        Toast.makeText(TaskActivity.this,"No Permissions or Bad Internet",Toast.LENGTH_LONG);
                                                    }
                                                    AlertDialog.Builder builderInner = new AlertDialog.Builder(
                                                            TaskActivity.this);
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
                            } catch (ExecutionException | InterruptedException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
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
    public boolean isEditable(String id) throws ExecutionException, InterruptedException, JSONException {
        if(user_type.equals("0"))
            return true;
        else if (user_type.equals("1")){
            String result = new AsyncgetTargetUserTasks().execute(prefs.getString("user_roll","")).get();
            JSONObject tasksJSON = new JSONObject(result);
            JSONArray tasks = tasksJSON.getJSONArray("message");
            boolean flag=false;
            for(int i=0;i<tasks.length();i++) {
                JSONObject task = tasks.getJSONObject(i);
                if(task.getString("task_id").equals(id)){
                    flag = true;
                    break;
                }
            }
            return flag;
        }
        return false;
    }
    public class AsyncgetTargetUserTasks extends AsyncTask<String,Void,String> {

        String result = "";
        URL url;
        @Override
        protected String doInBackground(String... target) {
            try {
                url = new URL(Utilities.getTargetUserTasksUrl());
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
            params.put("user_target_roll",target[0]);
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
                Toast.makeText(TaskActivity.this,"Connection Error",Toast.LENGTH_SHORT).show();
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

package com.delta.pragyanoc;

import android.content.Context;
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
import android.widget.EditText;
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

public class ChatActivity extends AppCompatActivity {

    SharedPreferences prefs;
    ArrayList<String> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        arrayList = new ArrayList<>();
        final String user_roll = prefs.getString("user_roll","");
        final String user_secret = prefs.getString("user_secret","");
        final EditText editText = (EditText) findViewById(R.id.newchat);
        try {
            String messagesJSONString = new AsyncGetMessages().execute(user_roll, user_secret, "1").get();    //TODO TO Change taskID
            JSONObject messagesJSON = new JSONObject(messagesJSONString);
            JSONArray messageArrayJSON = messagesJSON.getJSONArray("message");
            for(int i=0;i<messageArrayJSON.length();i++) {
                JSONObject messageJSON = (JSONObject) messageArrayJSON.get(i);
                arrayList.add(messageJSON.getString("user_name")+"\n\n"+messageJSON.getString("msg_data"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
            ListView listView = (ListView) findViewById(R.id.chatlist);
            listView.setAdapter(adapter);
        }catch (Exception e) {
            Log.d(Utilities.LOGGING,e+"");
            Toast.makeText(this,"Couldnt fetch messages,TRy later",Toast.LENGTH_SHORT).show();
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editText.getText().toString();
                try {
                    String res = new AsyncSendMessage().execute(user_roll, user_secret, "1", msg).get(); //TODO TO CHANGE taskID
                } catch (Exception e) {
                    Snackbar.make(view, "Error occurred , Try later", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }
    public class AsyncSendMessage extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String user_roll = strings[0];
            String user_secret = strings[1];
            String task_id = strings[2];
            String msg = strings[3];
            URL url;
            String result="";
                try {
                    url = new URL(Utilities.getCreateChatUrl());
                }
                catch(MalformedURLException e) {
                    throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
                }
                StringBuilder bodyBuilder = new StringBuilder();
                Map<String, String> params = new HashMap<>();
                params.put("user_roll",user_roll);
                params.put("user_secret",user_secret);
                params.put("task_id",task_id);
                params.put("user_msg",msg);
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

    public class AsyncGetMessages extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            String user_roll = strings[0];
            String user_secret = strings[1];
            String task_id = strings[2];
            URL url;
            String result="";
            try {
                url = new URL(Utilities.getGetChatUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_secret",user_secret);
            params.put("task_id",task_id);
            params.put("from_id","-1");
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


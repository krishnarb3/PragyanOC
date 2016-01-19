package com.delta.pragyanoc;

import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by rahul on 17/1/16.
 */
public class AsyncUpdateTaskStatus extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... strings) {
        String user_roll = strings[3];
        String user_secret = strings[4];
        String task_id = strings[0];
        String task_completed = strings[1];
        URL url;
        String result="";
        try {
            url = new URL(Utilities.getUpdateTaskStatusUrl());
        }
        catch(MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Map<String, String> params = new HashMap<>();
        params.put("user_roll",user_roll);
        params.put("user_secret",user_secret);
        params.put("team_id",strings[2]);
        params.put("task_id",task_id);
        params.put("task_status",task_completed);
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
        Log.v(Utilities.LOGGING, "Posting '" + body + "' to " + url);
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
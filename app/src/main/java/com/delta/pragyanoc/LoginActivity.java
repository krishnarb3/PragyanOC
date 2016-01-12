package com.delta.pragyanoc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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

public class LoginActivity extends AppCompatActivity {

    GoogleCloudMessaging gcmObj;
    EditText username,password;
    Button button;
    Context context;
    String regId;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        username = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        button = (Button)findViewById(R.id.submit);
        context = getApplicationContext();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterUser(username.getText().toString(),password.getText().toString());
            }
        });
    }

    // When Register Me button is clicked
    public void RegisterUser(String userRoll,String userPassword) {
        if (!TextUtils.isEmpty(userRoll)) {
            if (checkPlayServices()) {
                new AsyncRegisterWithGCM().execute(userRoll);
            }
        }
        // When Email is invalid
        else {
            Toast.makeText(context, "Please enter valid email",
                    Toast.LENGTH_LONG).show();
        }
    }

    public class AsyncRegisterWithGCM extends AsyncTask<String,Void,String> {
        String emailId;
        @Override
        protected String doInBackground(String... strings) {
            emailId = strings[0];
            String msg = "";
            try {
                if (gcmObj == null) {
                    gcmObj = GoogleCloudMessaging
                            .getInstance(context);
                }
                regId = gcmObj
                        .register(Utilities.getGoogleProjId());
                msg = "Registration ID :" + regId;

            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            if (!TextUtils.isEmpty(regId)) {
                storeRegIdinSharedPref(context, regId,emailId);
                Toast.makeText(
                        LoginActivity.this,
                        "Registered with GCM Server successfully.nn"
                                + msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                        LoginActivity.this,
                        "Reg ID Creation Failed.nnEither you haven't enabled Internet or GCM server is busy right now. Make sure you enabled Internet and try registering again after some time."
                                + msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void storeRegIdinSharedPref(Context context, String user_gcmid,String user_roll) {
        SharedPreferences prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_roll", user_roll);
        editor.putString("user_gcmid", user_gcmid);
        editor.commit();
        storeRegIdinServer(user_roll, user_gcmid);
    }
    private void storeRegIdinServer(String user_roll,String user_gcmid) {

        class AsyncStoreGCMtoServer extends AsyncTask<String, Void, Void> {
            String result;
            String user_roll,user_gcmid,user_secret;
            @Override
            protected Void doInBackground(String... strings) {
                user_roll = strings[0];
                user_gcmid = strings[1];
                user_secret = strings[2];
            URL url;
            try {
                url = new URL(Utilities.getGcmUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
            StringBuilder bodyBuilder = new StringBuilder();
            Map<String, String> params = new HashMap<>();
            params.put("user_roll",user_roll);
            params.put("user_gcmid",user_gcmid);
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
                Log.e("URL", "> " + url);
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
                return null;
        }
    }
    }
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(
                        context,
                        "This device doesn't support Play services, App will not work normally",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        } else {
            Toast.makeText(
                    context,
                    "This device supports Play services, App will work normally",
                    Toast.LENGTH_LONG).show();
        }
        return true;
    }
}

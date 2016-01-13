package com.delta.pragyanoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

public class AddressBookActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String user_roll;
    String user_secret;
    ProgressDialog pd;
    ArrayList<String> arrayList;
    ListView listView;
    String profileDetails;
    String allprofileDetails;
    ArrayList<User> userArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        arrayList = new ArrayList<>();
        userArray = new ArrayList<>();
        pd = ProgressDialog.show(this,"Loading",null,true,false);
        listView = (ListView) findViewById(R.id.listView_address);

        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        editor = prefs.edit();
        user_roll = prefs.getString("user_roll","");
        user_secret = prefs.getString("user_secret","");

        try {

            try {
                profileDetails = new AsyncgetProfile().execute().get();
                profileDetails = profileDetails.substring(4, profileDetails.length());
                Log.d(Utilities.LOGGING + "profileDetails", profileDetails);
            }catch(Exception e) {
                profileDetails = prefs.getString("profileDetails","");
            }
            if(profileDetails!=null&&!profileDetails.equals(""))
                editor.putString("profileDetails",profileDetails);
            editor.commit();

            try {
                allprofileDetails = new AsyncgetallProfile().execute().get();

                Log.d(Utilities.LOGGING, allprofileDetails);
            } catch(Exception e) {
                allprofileDetails = prefs.getString("allprofileDetails","");
            }
            if(allprofileDetails!=null&&!allprofileDetails.equals(""))
                editor.putString("allprofileDetails",allprofileDetails);
            editor.commit();

            String allprofile = prefs.getString("allprofileDetails", "");
            JSONObject allprofileJSON = new JSONObject(allprofile);
            Log.d(Utilities.LOGGING,allprofileJSON.toString());
            JSONArray profileArrays = allprofileJSON.getJSONArray("message");
            for(int i=0;i<profileArrays.length();i++) {
                JSONObject jsonObject = (JSONObject)profileArrays.get(i);
                User user = new User();
                user.user_name = jsonObject.getString("user_name");
                user.user_roll = jsonObject.getString("user_roll");
                user.user_phone = jsonObject.getString("user_phone");
                user.user_type = jsonObject.getString("user_type");
                userArray.add(user);
                String year;
                if(user.user_type.equals("0"))
                    year = "4th Year";
                else if(user.user_type.equals("1"))
                    year = "3rd year";
                else
                    year = "2nd year";
                arrayList.add(user.user_name+" | "+user.user_phone+" | "+year);
            }

            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(adapter);
        }catch (Exception e) {
            Log.d(Utilities.LOGGING,e+"");
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(AddressBookActivity.this,TaskActivity.class);
                intent.putParcelableArrayListExtra("memberName",userArray);
                startActivity(intent);
            }
        });
    }
    public class AsyncgetProfile extends AsyncTask<Void,Void,String>
    {
        String result="";
        @Override
        protected String doInBackground(Void... voids) {
            URL url;
            try {
                url = new URL(Utilities.getProfileUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getGcmUrl());
            }
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
            return result;
        }
    }
    public class AsyncgetallProfile extends AsyncTask<Void,Void,String> {

        String result="";
        @Override
        protected String doInBackground(Void... voids) {
            URL url;
            try {
                url = new URL(Utilities.getAllProfileUrl());
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("invalid url: " + Utilities.getAllProfileUrl());
            }
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
            Log.d(Utilities.LOGGING+"Result",result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
        }
    }
}

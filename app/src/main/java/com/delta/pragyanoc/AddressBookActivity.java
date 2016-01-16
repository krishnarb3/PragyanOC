package com.delta.pragyanoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;

public class AddressBookActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String user_roll;
    String user_secret;
    String user_type;
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
        listView = (ListView) findViewById(R.id.listView_address);

        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        editor = prefs.edit();
        user_roll = prefs.getString("user_roll","");
        user_secret = prefs.getString("user_secret","");
        user_type = prefs.getString("user_type","");
        try {
             profileDetails = prefs.getString("profileDetails","");
             //profileDetails = profileDetails.substring(4, profileDetails.length());
             Log.d(Utilities.LOGGING + "profileDetails", profileDetails);
             JSONObject profileJSON = new JSONObject(profileDetails);
             String user_type =((JSONObject)profileJSON.get("message")).getString("user_type");
             editor.putString("user_type",user_type);
             editor.commit();
            if(profileDetails!=null&&!profileDetails.equals(""))
                editor.putString("profileDetails",profileDetails);
            editor.commit();
            allprofileDetails = prefs.getString("allprofileDetails","");
            if(!allprofileDetails.equals(""))
                editor.putString("allprofileDetails",allprofileDetails);
            editor.commit();
            JSONObject allprofileJSON = new JSONObject(allprofileDetails);
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
            Log.d(Utilities.LOGGING,arrayList.toString());
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(adapter);
        }catch(Exception e) {
            Log.d(Utilities.LOGGING,e+"");
                profileDetails = prefs.getString("profileDetails","");
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Intent intent = new Intent(AddressBookActivity.this, TaskActivity.class);
                    intent.putExtra("user_target_roll", userArray.get(position).user_roll);
                    startActivity(intent);
            }
        });
    }

}

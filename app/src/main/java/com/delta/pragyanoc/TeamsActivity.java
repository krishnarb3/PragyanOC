package com.delta.pragyanoc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class TeamsActivity extends AppCompatActivity {

    ArrayList<String> teamnames;
    ArrayList<String> teamids;
    ArrayList<String> team_show;
    SharedPreferences prefs;
    public String profileDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ListView listView = (ListView) findViewById(R.id.teams);

        teamnames = new ArrayList<>();
        team_show = new ArrayList<>();
        teamids = new ArrayList<>();
        prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        try {
            profileDetails = prefs.getString("profileDetails", "");
            Log.d(Utilities.LOGGING,profileDetails);
            JSONObject profileJSON = new JSONObject(profileDetails);
            JSONArray teams = ((JSONObject)profileJSON.get("message")).getJSONArray("user_teams");
            for(int i=0;i<teams.length();i++) {
                JSONObject team = (JSONObject) teams.get(i);
                teamnames.add(team.getString("team_name"));
                teamids.add(team.getString("team_id"));
                team_show.add(team.getString("team_name")+ " - "+team.getString("team_id"));
            }
        }catch(Exception e) {
            Log.e(Utilities.LOGGING,e+"");
        }
        ArrayAdapter<String> adapter;
        if(team_show!=null) {
            adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, team_show);
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(TeamsActivity.this,TaskAcc2TeamActivity.class);
                intent.putExtra("team_id",teamids.get(i));
                intent.putExtra("team_name",teamnames.get(i));
                startActivity(intent);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}

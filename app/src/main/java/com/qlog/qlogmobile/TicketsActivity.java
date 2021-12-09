package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.Ticket;
import com.qlog.qlogmobile.adapters.TicketAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Ticket> ticketList;
    public TicketAdapter ticketAdapter;
    public RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private SharedPreferences logPref;
    private SharedPreferences.Editor logEditor;

    private FloatingActionButton doneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        init();
        setToolBar();
    }

    private void init() {
        ticketList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.ticketsRecyclerView);
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        logEditor = logPref.edit();
        ProgressDialog dialog = new ProgressDialog(TicketsActivity.this);
        dialog.setMessage("Processing");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_LOG, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("log"));
                    for (int i=0; i<array.length(); i++) {
                        JSONObject logObject = array.getJSONObject(i);
                        Ticket ticket = new Ticket();
                        ticket.setName(logObject.getString("name"));
                        ticket.setFacility(logObject.getString("facility"));
                        ticket.setQueue_no(logObject.getInt("queue_no"));
                        ticket.setPurpose(logObject.getString("purpose"));

                        ticketList.add(ticket);
                    }
                    ticketAdapter = new TicketAdapter(ticketList, this);
                    layoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(ticketAdapter);

                    Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
            }
        }, Throwable::printStackTrace) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("user_id", logPref.getString("user_id", null));
                map.put("purposes", logPref.getString("purposes_id", null));
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(TicketsActivity.this);
        queue.add(request);

    }


    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton doneBtn = toolbar.findViewById(R.id.right_icon);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logEditor.clear();
                logEditor.apply();
                finish();
                startActivity(new Intent(TicketsActivity.this, MainActivity.class));
            }
        });

//        print.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                doneBtn.setVisibility(View.GONE);
//
////                takeScreenShot();
//
//                doneBtn.setVisibility(View.VISIBLE);
//            }
//        });
        back.setOnClickListener(v -> onBackPressed());
    }
}
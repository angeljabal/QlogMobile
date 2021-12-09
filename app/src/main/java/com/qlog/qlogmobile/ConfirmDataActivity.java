package com.qlog.qlogmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.qlog.qlogmobile.adapters.MultiAdapter;
import com.qlog.qlogmobile.adapters.SelectedAdapter;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.Facility;
import com.qlog.qlogmobile.model.Purpose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConfirmDataActivity extends AppCompatActivity {
    private SharedPreferences logPref, userPref;
    private SharedPreferences.Editor logEditor;
    private TextView facilityText, facilityLabel, purposeLabel, purposeText;
    private ProgressDialog dialog;
    private MultiAdapter multiAdapter;
    private ArrayList<Purpose> purposeList;
    private ArrayList<Facility> facilityList;
    private RecyclerView purposes_rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_data);
        init();
        setToolBar();
    }

    private void init() {
        dialog = new ProgressDialog(ConfirmDataActivity.this);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        logEditor = logPref.edit();

        TextView nameText = (TextView) findViewById(R.id.nameText);
        TextView addressText = (TextView) findViewById(R.id.addressText);
        TextView phoneNumberText = (TextView) findViewById(R.id.phoneNumberText);
        purposeLabel = (TextView) findViewById(R.id.purposeLabel);
        Button confirmBtn = (Button) findViewById(R.id.confirm_btn);
        nameText.setText(logPref.getString("name", null));
        addressText.setText(logPref.getString("address", null));
        phoneNumberText.setText(logPref.getString("phone_number", null));

        loadPurposes();
        confirmBtn.setOnClickListener(view -> {
            if(logPref.getBoolean("isWalkin", true)){
                addQueue();
            }else{
                startActivity(new Intent(ConfirmDataActivity.this, TicketsActivity.class));
                finish();
            }

        });
    }

    private void loadPurposes(){
        purposeList = new ArrayList<>();
        if (logPref.getBoolean("isWalkin", true)) {
            Purpose purpose = new Purpose();
            purpose.setTitle("Walk-in");
            purposeList.add(purpose);
        }else{
            String[] id = logPref.getString("purposes_id", null).split(",");
            String[] title = logPref.getString("purposes_title", null).split(",");
            String[] hasRequiredFacility = logPref.getString("purposes_hasRequiredFacility", null).split(",");
            for(int i=0; i<id.length; i++){
                Purpose purpose = new Purpose();
                purpose.setId(Integer.parseInt(id[i]));
                purpose.setTitle(title[i]);
                purpose.setHasRequiredFacility(hasRequiredFacility[i].equals("true"));
                purposeList.add(purpose);
            }
        }
        purposes_rv = (RecyclerView) findViewById(R.id.purposes_facilities_rv);
        multiAdapter = new MultiAdapter(purposeList, ConfirmDataActivity.this);
        LinearLayoutManager selected_layoutManager = new LinearLayoutManager(ConfirmDataActivity.this);
        purposes_rv.setLayoutManager(selected_layoutManager);
        purposes_rv.setAdapter(multiAdapter);
    }

    private void addQueue() {
        StringRequest request = new StringRequest(Request.Method.POST, Constant.WALK_IN, response -> {
            try {
                JSONObject object = new JSONObject(response);
                Toast.makeText(this, object.getString("message"), Toast.LENGTH_SHORT).show();
                logEditor.clear();
                logEditor.apply();
                dialog.dismiss();

                startActivity(new Intent(ConfirmDataActivity.this, HomeActivity.class));
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();
                Snackbar.make(findViewById(android.R.id.content), error.toString(), Snackbar.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }) {
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
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ConfirmDataActivity.this);
        queue.add(request);
    }

    private void getFacilities() {
        dialog.show();
        SharedPreferences.Editor logEditor = logPref.edit();
        purposeList = new ArrayList<>();
        facilityList = new ArrayList<>();
//        multiAdapter = new MultiAdapter(purposeList, ConfirmDataActivity.this);

        StringRequest purposesRequest = new StringRequest(Request.Method.POST, Constant.FACILITIES, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    StringBuilder idSb = new StringBuilder();
                    StringBuilder nameSb = new StringBuilder();
                    String[] purposeIds = logPref.getString("purposes_id", null).split(",");
                    JSONArray facilitiesArray = new JSONArray(object.getString("facilities"));
                    for (int i = 0; i < facilitiesArray.length(); i++) {
                        JSONObject facilitiesObject = facilitiesArray.getJSONObject(i);
                        idSb.append(facilitiesObject.getString("id"));
                        nameSb.append(facilitiesObject.getString("name"));

                        if (i != facilitiesArray.length() - 1) {
                            nameSb.append(",");
                            idSb.append(",");
                        }
                    }

//                    if(logPref.getString("facilities_id",null)!=null){
//                        idSb.append(","+logPref.getString("facilities_id",null));
//                        nameSb.append(","+logPref.getString("facilities_name",null));
//                    }

                    logEditor.putString("facilities_id", idSb.toString());
                    logEditor.putString("facilities_name", nameSb.toString());
                    logEditor.apply();

                    if (logPref.getString("facilities_name", null) == null) {
                        facilityLabel.setText("");
                    } else {
                        facilityText.setText(bulletList(logPref.getString("facilities_name", null)));
                        if (logPref.getString("facilities_name", null).contains(",")) {
                            facilityLabel.setText("Facilities:");
                        }
                    }
                }
                dialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
            }
        }, error -> {
            error.printStackTrace();
            dialog.dismiss();
        }) {
            // provide token in header
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
                map.put("id", logPref.getString("purposes_id", null));
                return map;
            }
        };

        RequestQueue purposeQueue = Volley.newRequestQueue(this);
        purposeQueue.add(purposesRequest);
    }

    private String bulletList(String list) {
        String[] arr = list.split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i != arr.length - 1) {
                stringBuilder.append("\u2022 ").append(arr[i]).append("\n");
            } else {
                stringBuilder.append("\u2022 " + arr[i]);
            }
        }
        return stringBuilder.toString();
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton doneBtn = toolbar.findViewById(R.id.right_icon);
        doneBtn.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        logEditor.remove("facilities_id");
        logEditor.remove("facilities_name");
        logEditor.apply();
        super.onBackPressed();
    }
}
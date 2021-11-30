package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.qlog.qlogmobile.constants.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FacilityActivity extends AppCompatActivity {
    private View view;
    private Toolbar toolbar;
    public static String[] facilitiesArray;
    public static ArrayList<Integer> facilitiesList;
    private SharedPreferences userPref, logPref, facilitiesPref;
    private TextView facilityText, othersText, othersLabel, facilitiesLabel;
    private boolean[] selectedFacilities;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facility);
        init();
        setToolBar();
    }

    private void init() {
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        facilitiesPref = getApplicationContext().getSharedPreferences("facility", Context.MODE_PRIVATE);
        SharedPreferences.Editor facilitiesEditor = facilitiesPref.edit();
        SharedPreferences.Editor logEditor = logPref.edit();

        facilitiesList = new ArrayList<>();
        facilityText = findViewById(R.id.facilityText);
        othersText = findViewById(R.id.othersText);
        othersLabel = findViewById(R.id.othersLabel);
        facilitiesLabel = findViewById(R.id.facilityLabel);
        othersText.setVisibility(View.INVISIBLE);
        othersLabel.setVisibility(View.INVISIBLE);

        nextBtn = findViewById(R.id.next_btn);

        //FACILITIES REQUEST
        StringRequest facilitiesRequest = new StringRequest(Request.Method.POST, Constant.FACILITIES, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("facilities"));
                    if (array.length() == 0) {
                        facilityText.setVisibility(View.INVISIBLE);
                        facilitiesLabel.setVisibility(View.INVISIBLE);
                        othersText.setVisibility(View.VISIBLE);
                        othersLabel.setVisibility(View.VISIBLE);
                        othersLabel.setText("Facility");
                    }
                    facilitiesArray = new String[array.length() + 1];
                    selectedFacilities = new boolean[array.length() + 1];
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject facilitiesObject = array.getJSONObject(i);
                        facilitiesArray[i] = facilitiesObject.getString("name");
                    }
                    facilitiesArray[array.length()] = "Others";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
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
                map.put("title", logPref.getString("purpose", null));
                return map;
            }
        };

        RequestQueue facilityQueue = Volley.newRequestQueue(this);
        facilityQueue.add(facilitiesRequest);

        facilityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FacilityActivity.this);
                builder.setTitle("Select Facilities");
                builder.setCancelable(false);

                builder.setMultiChoiceItems(facilitiesArray, selectedFacilities, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b) {
                            facilitiesList.add(i);
                            Collections.sort(facilitiesList);
                        } else {
                            facilitiesList.remove(i);
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for (int j = 0; j < facilitiesList.size(); j++) {
                            stringBuilder.append(facilitiesArray[facilitiesList.get(j)]);
                            if (facilitiesArray[facilitiesList.get(j)] == "Others") {
                                othersText.setVisibility(View.VISIBLE);
                                othersLabel.setVisibility(View.VISIBLE);
                            }
                            if (j != facilitiesList.size() - 1) {
                                stringBuilder.append(",");
                            }
                        }
                        facilityText.setText(stringBuilder.toString());
                        logEditor.putString("facilities", stringBuilder.toString());
                        logEditor.apply();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < selectedFacilities.length; j++) {
                            selectedFacilities[j] = false;
                            facilitiesList.clear();
                            facilityText.setText("");
                        }
                    }
                });
                builder.show();
            }
        });

        othersText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FacilityActivity.this, ScanQR.class));
            }
        });

    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        SharedPreferences.Editor logEditor = logPref.edit();
        logEditor.putString("facilities", "");
        logEditor.apply();

        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton search = toolbar.findViewById(R.id.right_icon);
        search.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> onBackPressed());
    }
}
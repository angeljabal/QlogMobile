package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qlog.qlogmobile.constants.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BookAppointment extends AppCompatActivity {
    private Toolbar toolbar;
    public static ArrayList<String> purposeList;
    public static String[] facilitiesList = {"Registrar", "Cashier"};
    public static ArrayList<Integer> facilitiesIdList;
    private SharedPreferences userPref, logPref;
    private TextView purposeText, facilityText;
    private Dialog purposeDialog;
    private boolean[] selectedFacilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);

        showPurposes();
        showFacilities();
        setToolBar();
    }

    private void showPurposes() {
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        SharedPreferences.Editor logEditor = logPref.edit();

        purposeList = new ArrayList<>();
        purposeText = findViewById(R.id.purposeText);

        //PURPOSES REQUEST
        StringRequest purposesRequest = new StringRequest(Request.Method.GET, Constant.PURPOSES, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("purposes"));
                    facilitiesList = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject purposeObject = array.getJSONObject(i);
                        purposeList.add(purposeObject.getString("title"));
                    }
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
        };

        RequestQueue purposeQueue = Volley.newRequestQueue(this);
        purposeQueue.add(purposesRequest);

        purposeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                purposeDialog = new Dialog(BookAppointment.this);
                purposeDialog.setContentView(R.layout.purpose_searchable_spinner);
                purposeDialog.getWindow().setLayout(800, 800);
                purposeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                purposeDialog.show();

                EditText editPurpose = purposeDialog.findViewById(R.id.edit_purpose);
//                ListView purposeListView = purposeDialog.findViewById(R.id.purpose_list);

                ArrayAdapter<String> purposeAdapter = new ArrayAdapter<>(BookAppointment.this, android.R.layout.simple_spinner_dropdown_item, purposeList);

//                purposeListView.setAdapter(purposeAdapter);

                editPurpose.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        purposeAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

//                purposeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                        purposeText.setText(purposeAdapter.getItem(i));
//                        logEditor.putString("purpose", String.valueOf(purposeText.getText()));
//                        purposeDialog.dismiss();
//                    }
//                });
            }
        });
    }

    private void showFacilities(){
        facilitiesIdList = new ArrayList<>();
        facilityText = findViewById(R.id.facilityText);

        //FACILITIES REQUEST

        StringRequest facilitiesRequest = new StringRequest(Request.Method.GET, Constant.FACILITIES, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("facilities"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject facilitiesObject = array.getJSONObject(i);
                        facilitiesList[i] = facilitiesObject.getString("name");
                    }
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
        for(int i=0 ; i<facilitiesList.length; i++){
            Log.d("facilities", facilitiesList[i]);
        }

        RequestQueue facilityQueue = Volley.newRequestQueue(this);
        facilityQueue.add(facilitiesRequest);

        selectedFacilities = new boolean[facilitiesList.length];
        facilityText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BookAppointment.this);
                builder.setTitle("Select Facilities");
                builder.setCancelable(false);

                builder.setMultiChoiceItems(facilitiesList, selectedFacilities, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if(b){
                            facilitiesIdList.add(i);
                            Collections.sort(facilitiesIdList);
                        }else{
                            facilitiesIdList.remove(i);
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder();

                        for(int j=0; j<facilitiesIdList.size(); j++){
                            if (j != facilitiesIdList.size()-1){
                                stringBuilder.append(",");
                            }
                        }
                        facilityText.setText(stringBuilder.toString());
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
                        for (int j=0; j<selectedFacilities.length; j++){
                            selectedFacilities[j] = false;
                            facilitiesIdList.clear();
                            facilityText.setText("");
                        }
                    }
                });
                builder.show();
            }
        });


    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton search = toolbar.findViewById(R.id.right_icon);
        search.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> onBackPressed());
    }
}
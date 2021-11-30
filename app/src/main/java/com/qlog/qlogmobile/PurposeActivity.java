package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.Map;

public class PurposeActivity extends AppCompatActivity {
    private Toolbar toolbar;
    public static ArrayList<String> purposeList;
    public static String[] facilitiesList = {"Registrar", "Cashier"};
    private SharedPreferences userPref, logPref;
    private TextView purposeText, othersText, othersLabel;
    private Dialog purposeDialog;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purpose);
        init();
        setToolBar();
    }

    private void init(){
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        logPref = this.getApplicationContext().getSharedPreferences("log",Context.MODE_PRIVATE);
        SharedPreferences.Editor logEditor = logPref.edit();

        purposeList = new ArrayList<>();
        purposeText = findViewById(R.id.purposeText);
        othersText = findViewById(R.id.othersText);
        othersLabel = findViewById(R.id.othersLabel);
        othersText.setVisibility(View.INVISIBLE);
        othersLabel.setVisibility(View.INVISIBLE);

        nextBtn = findViewById(R.id.next_btn);

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
                    purposeList.add("Others");
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
                purposeDialog = new Dialog(PurposeActivity.this);
                purposeDialog.setContentView(R.layout.purpose_searchable_spinner);
                purposeDialog.getWindow().setLayout(800, 800);
                purposeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                purposeDialog.show();

                EditText editPurpose = purposeDialog.findViewById(R.id.edit_purpose);
                ListView purposeListView = purposeDialog.findViewById(R.id.purpose_list);

                ArrayAdapter<String> purposeAdapter = new ArrayAdapter<>(PurposeActivity.this, android.R.layout.simple_spinner_dropdown_item, purposeList);

                purposeListView.setAdapter(purposeAdapter);

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

                purposeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(purposeAdapter.getItem(i)=="Others"){
                            purposeText.setText(purposeAdapter.getItem(i));
                            othersText.setVisibility(View.VISIBLE);
                            othersLabel.setVisibility(View.VISIBLE);
                            if(!othersText.getText().toString().isEmpty()){
                                logEditor.putString("purpose", othersText.getText().toString());
                                logEditor.apply();
                                nextBtn.setEnabled(true);
                            }
                        }else{
                            purposeText.setText(purposeAdapter.getItem(i));
                            othersText.setVisibility(View.INVISIBLE);
                            othersLabel.setVisibility(View.INVISIBLE);
                            logEditor.putString("purpose", String.valueOf(purposeText.getText()));
                            logEditor.apply();
                            nextBtn.setEnabled(true);
                        }
                        purposeDialog.dismiss();
                    }
                });
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PurposeActivity.this, FacilityActivity.class));
            }
        });
    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        SharedPreferences.Editor logEditor = logPref.edit();
        logEditor.putString("purpose", "");
        logEditor.apply();
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton search = toolbar.findViewById(R.id.right_icon);
        search.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> onBackPressed());
    }
}
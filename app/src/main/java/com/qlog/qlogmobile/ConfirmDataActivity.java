package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qlog.qlogmobile.constants.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ConfirmDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_data);

        init();
        setToolBar();
    }

    private void init() {
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        SharedPreferences.Editor logEditor = logPref.edit();

        if(logPref.getString("user_id",null)==null){
            StringRequest purposesRequest = new StringRequest(Request.Method.POST, Constant.FIND_USER, response -> {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")) {
                        JSONObject userObj = object.getJSONObject("users");
                        JSONObject profileObj = userObj.getJSONObject("profile");
                        logEditor.putString("user_id", userObj.getString("id"));
                        logEditor.putString("name", userObj.getString("name"));
                        logEditor.putString("address", profileObj.getString("address"));
                        logEditor.putString("phone_number", profileObj.getString("phone_number"));
                        logEditor.apply();

                        startActivity(new Intent(ConfirmDataActivity.this, ConfirmDataActivity.class));
                    } else {
                        Toast.makeText(ConfirmDataActivity.this, "No Results", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ConfirmDataActivity.this, "No results found. Please try again.", Toast.LENGTH_LONG).show();
                }
            }, error -> {
                error.printStackTrace();
                Toast.makeText(ConfirmDataActivity.this, "No results found. Please try again.", Toast.LENGTH_LONG).show();

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
                    map.put("name", logPref.getString("name", null));
                    return map;
                }

            };

            RequestQueue purposeQueue = Volley.newRequestQueue(ConfirmDataActivity.this);
            purposeQueue.add(purposesRequest);
        }

        TextView nameText = (TextView) findViewById(R.id.nameText);
        TextView addressText = (TextView) findViewById(R.id.addressText);
        TextView phoneNumberText = (TextView) findViewById(R.id.phoneNumberText);
        TextView purposeText = (TextView) findViewById(R.id.purposeText);
        TextView facilityText = (TextView) findViewById(R.id.facilityText);
        TextView facilityTitle = (TextView) findViewById(R.id.facilityTitle);
        CardView confirmBtn = (CardView) findViewById(R.id.confirm_btn);

        nameText.setText(logPref.getString("name", null));
        addressText.setText(logPref.getString("address", null));
        phoneNumberText.setText(logPref.getString("phone_number", null));
        purposeText.setText(logPref.getString("purpose", null));
        facilityText.setText(logPref.getString("facilities", null));

        if (logPref.getString("facilities", null) == null) {
            facilityTitle.setText("");
        }
        confirmBtn.setOnClickListener(view -> {
            if (logPref.getString("facilities", null) == null) {
                StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_LOG, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")) {
                            Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
                            logEditor.clear();
                            logEditor.apply();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                        map.put("purpose", logPref.getString("purpose", null));
                        if (logPref.getString("facilities", null) != null) {
                            map.put("facilities", logPref.getString("facilities", null));
                        }
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(ConfirmDataActivity.this);
                queue.add(request);

                startActivity(new Intent(ConfirmDataActivity.this, HomeActivity.class));
                finish();
            }else{
                startActivity(new Intent(ConfirmDataActivity.this, TicketsActivity.class));
                finish();
            }
        });

    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton doneBtn = toolbar.findViewById(R.id.right_icon);
        doneBtn.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v -> onBackPressed());
    }
}
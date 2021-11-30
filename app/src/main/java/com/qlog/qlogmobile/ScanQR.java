package com.qlog.qlogmobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.qlog.qlogmobile.constants.CaptureAct;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScanQR extends AppCompatActivity implements View.OnClickListener {
    private Button scanBtn, findBtn;
    Toolbar toolbar;
    private Dialog usersDialog;
    public static ArrayList<User> userList;
    private SharedPreferences userPref, logPref;
    private SharedPreferences.Editor logEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        init();
        setToolBar();
    }

    private void init() {
        scanBtn = findViewById(R.id.scan_qr);
        findBtn = findViewById(R.id.find_by_name);
        scanBtn.setOnClickListener(this);
        findBtn.setOnClickListener(this);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        logPref = this.getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        logEditor = logPref.edit();
    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        ImageButton search = toolbar.findViewById(R.id.right_icon);
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String facility = userPref.getString("facility", null);

        if (facility == null) {
            ImageButton back = toolbar.findViewById(R.id.back);
            back.setOnClickListener(v -> onBackPressed());
        } else {
            ImageButton back = toolbar.findViewById(R.id.back);
            back.setVisibility(View.INVISIBLE);
        }
        search.setVisibility(View.INVISIBLE);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));
    }

    private boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.queue) {
//            showAddStudentDialog();
        } else if (menuItem.getItemId() == R.id.logout) {
            logout();
        }
        return true;
    }

    private void logout() {
        SharedPreferences.Editor userEditor = userPref.edit();
        userEditor.clear();
        userEditor.apply();
        finish();
        startActivity(new Intent(ScanQR.this, LoginActivity.class));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.scan_qr) {
            scanCode();
        } else if (view.getId() == R.id.find_by_name) {
            findUser();
        }
    }

    private void findUser() {
        userList = new ArrayList<>();

        usersDialog = new Dialog(ScanQR.this);
        usersDialog.setContentView(R.layout.search_user_dialog);
        usersDialog.getWindow().setLayout(800, 800);
        usersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        usersDialog.show();

        EditText editUser = usersDialog.findViewById(R.id.edit_user);
        Button findBtn = usersDialog.findViewById(R.id.find_button);
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringRequest purposesRequest = new StringRequest(Request.Method.POST, Constant.USERS, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")) {
                            JSONObject userObj = object.getJSONObject("user");
                            JSONObject profileObj = userObj.getJSONObject("profile");
                            logEditor.putString("user_id", userObj.getString("id"));
                            logEditor.putString("name", userObj.getString("name"));
                            logEditor.putString("address", profileObj.getString("address"));
                            logEditor.putString("phone_number", profileObj.getString("phone_number"));
                            logEditor.apply();

                            startActivity(new Intent(ScanQR.this, ConfirmDataActivity.class));
                        } else {
                            Toast.makeText(ScanQR.this, "No Results", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_LONG).show();
                    }
                }, error -> {
                    error.printStackTrace();
                    Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_LONG).show();

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
                        map.put("name", String.valueOf(editUser.getText()));
                        return map;
                    }

                };

                RequestQueue purposeQueue = Volley.newRequestQueue(ScanQR.this);
                purposeQueue.add(purposesRequest);
            }
        });
    }

    private void scanCode() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                String json = result.getContents();

                try {
                    JSONObject results = new JSONObject(json);

                    logEditor.putString("user_id", results.getString("id"));
                    logEditor.putString("name", results.getString("name"));
                    logEditor.putString("address", results.getString("address"));
                    logEditor.putString("phone_number", results.getString("phone_number"));
                    logEditor.apply();

                    startActivity(new Intent(ScanQR.this, ConfirmDataActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
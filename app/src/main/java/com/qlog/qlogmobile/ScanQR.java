package com.qlog.qlogmobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.qlog.qlogmobile.adapters.UserAdapter;
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
    private ProgressDialog dialog;
    private static ArrayList<User> usersList;
    private EditText searchBox;
    private UserAdapter userAdapter;
    private RecyclerView user_rv;
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
        if (menuItem.getItemId() == R.id.logout) {
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
            dialog = new ProgressDialog(ScanQR.this);
            loadUsers();
        }
    }

    private void loadUsers() {
        dialog.setMessage("Processing");
        dialog.show();
        usersList = new ArrayList<>();
        usersDialog = new Dialog(ScanQR.this);
        StringRequest usersRequest = new StringRequest(Request.Method.GET, Constant.USERS, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("users"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject usersObject = array.getJSONObject(i);
                        JSONObject profileObject = usersObject.getJSONObject("profile");
                        if (!usersList.contains(usersObject.getString("name"))) {
                            User user = new User();
                            user.setId(usersObject.getInt("id"));
                            user.setName(usersObject.getString("name"));
                            user.setType(usersObject.getString("type"));
                            user.setAddress(profileObject.getString("address"));
                            user.setPhone_number(profileObject.getString("phone_number"));
                            usersList.add(user);
                        }

                        dialog.dismiss();
                        usersDialog.setContentView(R.layout.search_user_dialog);
                        usersDialog.getWindow().setLayout(1000, 900);
                        usersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        usersDialog.show();

                        searchBox = usersDialog.findViewById(R.id.search_box);
                        searchBox.requestFocus();
                        user_rv = usersDialog.findViewById(R.id.users_rv);
                        LinearLayoutManager purposes_layoutManager = new LinearLayoutManager(ScanQR.this);
                        user_rv.setLayoutManager(purposes_layoutManager);
                        user_rv.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
                        userAdapter = new UserAdapter(usersList, this);
                        user_rv.setAdapter(userAdapter);

                        searchBox.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                userAdapter.getFilter().filter(charSequence);
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(ScanQR.this, "No results found. Please try again", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
            } catch (NullPointerException ex){
                ex.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
            dialog.dismiss();
            Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_SHORT).show();

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

        };

        RequestQueue usersQueue = Volley.newRequestQueue(ScanQR.this);
        usersQueue.add(usersRequest);

    }

    private void showDialog() {
        usersDialog.setContentView(R.layout.search_user_dialog);
        usersDialog.getWindow().setLayout(1000, 900);
        usersDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        usersDialog.show();

        searchBox = usersDialog.findViewById(R.id.search_box);
        searchBox.requestFocus();
        user_rv = usersDialog.findViewById(R.id.users_rv);
        LinearLayoutManager purposes_layoutManager = new LinearLayoutManager(ScanQR.this);
        user_rv.setLayoutManager(purposes_layoutManager);
        user_rv.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        userAdapter = new UserAdapter(usersList, this);
        user_rv.setAdapter(userAdapter);

        ProgressDialog dialog = new ProgressDialog(ScanQR.this);

//        findBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.setMessage("Searching");
//                dialog.show();
////                StringRequest usersRequest = new StringRequest(Request.Method.POST, Constant.USERS, response -> {
////                    try {
////                        JSONObject object = new JSONObject(response);
////                        if (object.getBoolean("success")) {
////                            JSONArray array = new JSONArray(object.getString("users"));
////                            for (int i = 0; i < array.length(); i++) {
////                                JSONObject usersObject = array.getJSONObject(i);
////                                JSONObject profileObject = usersObject.getJSONObject("profile");
////                                if (!usersList.contains(usersObject.getString("name"))) {
////                                    User user = new User();
////                                    user.setId(usersObject.getInt("id"));
////                                    user.setName(usersObject.getString("name"));
////                                    user.setType(usersObject.getString("type"));
////                                    user.setAddress(profileObject.getString("address"));
////                                    user.setPhone_number(profileObject.getString("phone_number"));
////                                    usersList.add(user);
////                                    userAdapter.notifyDataSetChanged();
////                                }
////                            }
////                            if(usersList.size()==0){
////                                Toast.makeText(ScanQR.this, "No results found.", Toast.LENGTH_SHORT).show();
////                            }
////                            dialog.dismiss();
////                        } else {
////                            dialog.dismiss();
////                            Toast.makeText(ScanQR.this, "No Results", Toast.LENGTH_SHORT).show();
////                        }
////                    } catch (JSONException e) {
////                        dialog.dismiss();
////                        e.printStackTrace();
////                        Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_SHORT).show();
////                    }
////                }, error -> {
////                    dialog.dismiss();
////                    error.printStackTrace();
////                    Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_SHORT).show();
////
////                }) {
////                    // provide token in header
////                    @Override
////                    public Map<String, String> getHeaders() throws AuthFailureError {
////                        String token = userPref.getString("token", "");
////                        HashMap<String, String> map = new HashMap<>();
////                        map.put("Authorization", "Bearer " + token);
////                        return map;
////                    }
////
////                    @Override
////                    protected Map<String, String> getParams() throws AuthFailureError {
////                        HashMap<String, String> map = new HashMap<>();
////                        map.put("name", String.valueOf(searchBox.getText()));
////                        return map;
////                    }
////
////                };
////
////                RequestQueue usersQueue = Volley.newRequestQueue(ScanQR.this);
////                usersQueue.add(usersRequest);
//
//                searchBox.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                        userAdapter.getFilter().filter(charSequence);
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable editable) {
//
//                    }
//                });
////
////                userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////                    @Override
////                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
////                        String selected = usersAdapter.getItem(i);
////                        logEditor.putString("name", selected);
////                        logEditor.apply();
////                        confirm();
////                        Toast.makeText(ScanQR.this, selected + " selected successfully.", Toast.LENGTH_SHORT).show();
////                    }
////                });
//            }
//        });
    }

//    private void loadUsers(){
//        StringRequest usersRequest = new StringRequest(Request.Method.GET, Constant.USERS, response -> {
//            try {
//                JSONObject object = new JSONObject(response);
//                if (object.getBoolean("success")) {
//                    JSONArray array = new JSONArray(object.getString("users"));
//                    for (int i = 0; i < array.length(); i++) {
//                        JSONObject usersObject = array.getJSONObject(i);
////                        JSONObject profileObject = usersObject.getJSONObject("profile");
//                        if (!usersList.contains(usersObject.getString("name"))) {
//                            User user = new User();
//                            user.setId(usersObject.getInt("id"));
//                            user.setName(usersObject.getString("name"));
//                            user.setType(usersObject.getString("type"));
////                            user.setAddress(profileObject.getString("address"));
////                            user.setPhone_number(profileObject.getString("phone_number"));
//                            usersList.add(user);
//                        }
//                    }
//                    if(usersList.size()==0){
//                        Toast.makeText(ScanQR.this, "No results found.", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(ScanQR.this, "No Results", Toast.LENGTH_SHORT).show();
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_SHORT).show();
//            }
//        }, error -> {
//            error.printStackTrace();
//            Toast.makeText(ScanQR.this, "No results found. Please try again.", Toast.LENGTH_SHORT).show();
//
//        }) {
//            // provide token in header
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                String token = userPref.getString("token", "");
//                HashMap<String, String> map = new HashMap<>();
//                map.put("Authorization", "Bearer " + token);
//                return map;
//            }
//
//        };
//
//        RequestQueue usersQueue = Volley.newRequestQueue(ScanQR.this);
//        usersQueue.add(usersRequest);
//
//        searchBox.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                userAdapter.getFilter().filter(charSequence);
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//    }

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
                String json = result.getContents();
                try {
                    JSONObject results = new JSONObject(json);

                    logEditor.putString("user_id", results.getString("id"));
                    logEditor.putString("name", results.getString("name"));
                    logEditor.putString("address", results.getString("address"));
                    logEditor.putString("phone_number", results.getString("phone_number"));
                    logEditor.apply();
                    confirm();
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

    private void confirm() {
        if (logPref.getBoolean("isWalkin", true)) {
            startActivity(new Intent(ScanQR.this, ConfirmDataActivity.class));
        } else {
            startActivity(new Intent(ScanQR.this, PurposeActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        logEditor.remove("user_id");
        logEditor.remove("name");
        logEditor.remove("address");
        logEditor.remove("phone_number");
        logEditor.apply();
        super.onBackPressed();
    }
}
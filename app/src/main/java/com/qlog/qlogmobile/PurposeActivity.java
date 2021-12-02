package com.qlog.qlogmobile;

import android.app.AlertDialog;
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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qlog.qlogmobile.adapters.MultiAdapter;
import com.qlog.qlogmobile.adapters.SelectedAdapter;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.Purpose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PurposeActivity extends AppCompatActivity implements MyInterface {
    private Toolbar toolbar;
    private ArrayList<Purpose> purposes, selectedList;
    private SharedPreferences userPref, logPref;
    private TextView purposeText;
    private Dialog purposeDialog;
    private Button clear_btn, others_btn;
    private FloatingActionButton done_btn;
    private RecyclerView selected_rv, purposes_rv;
    public SelectedAdapter selectedAdapter;
    public MultiAdapter multiAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purpose);
        init();
        setToolBar();
    }

    private void init() {
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        logPref = this.getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        SharedPreferences.Editor logEditor = logPref.edit();

        purposes = new ArrayList<>();
        selectedList = new ArrayList<>();
        purposeText = findViewById(R.id.purposeText);

        done_btn = findViewById(R.id.done_btn);

        purposeDialog = new Dialog(PurposeActivity.this);
        selected_rv = (RecyclerView) findViewById(R.id.selectedPurposesRecyclerView);

        createListofData();
        purposeText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showPurposeDialog();
            }
        });

        done_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder stringBuilder = new StringBuilder();

                for(int i=0; i<selectedList.size(); i++){
                    stringBuilder.append(selectedList.get(i).getTitle());

                    if (i != selectedList.size() - 1) {
                        stringBuilder.append(",");
                    }
                }

                logEditor.putString("purposes", stringBuilder.toString());
                logEditor.apply();
                startActivity(new Intent(PurposeActivity.this, FacilityActivity.class));
            }
        });
    }

    private void showPurposeDialog() {
        purposeDialog.setContentView(R.layout.purpose_searchable_spinner);
        purposeDialog.getWindow().setLayout(1000, 1200);
        purposeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        purposeDialog.show();

//        doneBtn = purposeDialog.findViewById(R.id.done_btn);
        EditText editPurpose = purposeDialog.findViewById(R.id.edit_purpose);
        purposes_rv = purposeDialog.findViewById(R.id.purposes_rv);
        LinearLayoutManager purposes_layoutManager = new LinearLayoutManager(PurposeActivity.this);
        purposes_rv.setLayoutManager(purposes_layoutManager);
        purposes_rv.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

        others_btn = purposeDialog.findViewById(R.id.others_btn);
        clear_btn = purposeDialog.findViewById(R.id.clear_btn);

        others_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOthersDialogInput();
                purposeDialog.dismiss();
            }
        });

        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelected();
            }
        });

        multiAdapter = new MultiAdapter(purposes, this);
        purposes_rv.setAdapter(multiAdapter);


        editPurpose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                multiAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        editPurpose.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i==keyEvent.KEYCODE_ENTER){
                    editPurpose.setText("");
                    purposeDialog.dismiss();
                }
                return false;
            }
        });
    }

    private void showOthersDialogInput() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PurposeActivity.this);
        alertDialog.setTitle("Others:");
        final EditText input = new EditText(PurposeActivity.this);

        input.setHint("Please state other purpose...");
        input.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Add",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().isEmpty()) {
                            purposes.add(new Purpose(input.getText().toString(), true));
                            getSelected();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(PurposeActivity.this, "Others field is empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        alertDialog.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void clearSelected() {
        for (Purpose purpose : purposes) {
            purpose.setSelected(false);
            getSelected();
            purposeDialog.dismiss();
        }
    }

    private void createListofData() {
        //PURPOSES REQUEST
        StringRequest purposesRequest = new StringRequest(Request.Method.GET, Constant.PURPOSES, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("purposes"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject purposeObject = array.getJSONObject(i);
                        Purpose purpose = new Purpose(purposeObject.getString("title"), false);

                        purposes.add(purpose);
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
    }

    @Override
    public void getSelected() {

        selectedList = multiAdapter.getSelected();

        selectedAdapter = new SelectedAdapter(selectedList, PurposeActivity.this);
        LinearLayoutManager selected_layoutManager = new LinearLayoutManager(PurposeActivity.this);
        selected_rv.setLayoutManager(selected_layoutManager);
        selected_rv.setAdapter(selectedAdapter);
        selectedAdapter.notifyDataSetChanged();
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
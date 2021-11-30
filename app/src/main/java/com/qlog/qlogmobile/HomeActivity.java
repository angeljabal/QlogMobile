package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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


public class HomeActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setToolBar();
        init();
    }

    private void init(){
        Button walkinBtn = findViewById(R.id.walkin);
        Button bookAppointmentBtn = findViewById(R.id.bookAppointment);
        SharedPreferences logPref = this.getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = logPref.edit();

        walkinBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                editor.putString("purpose", "Walk-in");
                editor.apply();
                startActivity(new Intent(HomeActivity.this, ScanQR.class));
            }
        });

        bookAppointmentBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, PurposeActivity.class));
            }
        });
    }

    private void setToolBar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton search = toolbar.findViewById(R.id.right_icon);
        search.setVisibility(View.INVISIBLE);
        back.setVisibility(View.INVISIBLE);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));
    }

    private boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.queue) {
            
        } else if (menuItem.getItemId() == R.id.logout) {
            logout();
        }
        return true;
    }

    private void logout(){
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        StringRequest request = new StringRequest(Request.Method.POST, Constant.LOGOUT, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.clear();
                    editor.apply();
                    finish();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = userPref.getString("token","");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization","Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

}
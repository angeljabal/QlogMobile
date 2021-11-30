package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                boolean isLoggedIn = userPref.getBoolean("isLoggedIn", false);
                String facility = userPref.getString("facility", null);
                float current_time= Calendar.getInstance().getTimeInMillis();
                float refresh_time = userPref.getFloat("refresh_time", 0);
                float newTime=current_time-refresh_time/1000;
                float bufferTime=100;

                if(newTime<(84000-bufferTime)){
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.clear();
                    editor.apply();
                    finish();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                else{
                    if (isLoggedIn) {
                        if (facility==null) {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(MainActivity.this, ScanQR.class));
                            finish();
                        }
                    } else {
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }

                }
            }
        },1500);


    }

}
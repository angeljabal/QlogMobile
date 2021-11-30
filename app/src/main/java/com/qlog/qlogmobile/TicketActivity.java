package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.qlog.qlogmobile.constants.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TicketActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView nameText, purposeText, facilityText, queueNumberText;
    private Display mDisplay;
    private Uri uri;
    private String imagesUri;
    private String path;
    private Bitmap bitmap;

    int totalHeight, totalWidth;

    public static final int READ_PHONE = 110;
    private String file_name = "LogScreenshot";
    private File myPath;
    private Button doneBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        init();
        setToolBar();
    }

    private void init() {
        doneBtn = findViewById(R.id.done_btn);

        SharedPreferences logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        SharedPreferences.Editor logEditor = logPref.edit();

        nameText = (TextView) findViewById(R.id.nameText);
        purposeText = (TextView) findViewById(R.id.purposeText);
        facilityText = (TextView) findViewById(R.id.facilityText);

        nameText.setText(logPref.getString("name", null));
        purposeText.setText(logPref.getString("purpose", null));
        facilityText.setText(logPref.getString("facility", null));

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logEditor.clear();
                logEditor.apply();
                finish();
                startActivity(new Intent(TicketActivity.this, MainActivity.class));
            }
        });
    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton search = toolbar.findViewById(R.id.search_button);
//        print.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                doneBtn.setVisibility(View.GONE);
//
////                takeScreenShot();
//
//                doneBtn.setVisibility(View.VISIBLE);
//            }
//        });
        back.setOnClickListener(v -> onBackPressed());
    }

}
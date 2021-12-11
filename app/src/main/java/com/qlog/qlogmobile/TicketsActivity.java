package com.qlog.qlogmobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.anggastudio.printama.Printama;
import com.qlog.qlogmobile.adapters.TicketAdapter;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.Ticket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Ticket> ticketList;
    public TicketAdapter ticketAdapter;
    public RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private SharedPreferences logPref;
    private SharedPreferences.Editor logEditor;
    private Button findPrinterBtn, printBtn;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        init();
        setToolBar();
    }

    private void init() {
        ticketList = new ArrayList<>();
        recyclerView = findViewById(R.id.ticketsRecyclerView);
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        logPref = getApplicationContext().getSharedPreferences("log", Context.MODE_PRIVATE);
        logEditor = logPref.edit();
        findPrinterBtn = findViewById(R.id.find_btn);
        printBtn = findViewById(R.id.print_btn);
        dialog = new ProgressDialog(TicketsActivity.this);
        dialog.setMessage("Processing");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_LOG, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONArray array = new JSONArray(object.getString("log"));
                    for (int i=0; i<array.length(); i++) {
                        JSONObject logObject = array.getJSONObject(i);
                        Ticket ticket = new Ticket();
                        ticket.setName(logObject.getString("name"));
                        ticket.setFacility(logObject.getString("facility"));
                        ticket.setQueue_no(logObject.getInt("queue_no"));
                        ticket.setPurpose(logObject.getString("purpose"));

                        ticketList.add(ticket);
                    }
                    ticketAdapter = new TicketAdapter(ticketList, this);
                    layoutManager = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(ticketAdapter);

                    Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
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
                map.put("purposes", logPref.getString("purposes_id", null));
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(TicketsActivity.this);
        queue.add(request);

        findPrinterBtn.setOnClickListener(v -> showPrinterList());
        printBtn.setOnClickListener(view -> {
            dialog.setMessage("Printing...");
            dialog.show();
            printTicket();
        });
        getSavedPrinter();
    }

    private void getSavedPrinter() {
        BluetoothDevice connectedPrinter = Printama.with(this).getConnectedPrinter();
        if (connectedPrinter != null) {
            String text = "Connected to : " + connectedPrinter.getName();
            findPrinterBtn.setText(text);
        }
    }

    private void printTicket() {
        for(Ticket ticket: ticketList){
            Printama.with(this).connect(printama -> {
                printama.printDoubleDashedLine();
                printama.printTextln("QUEUE NUMBER", Printama.CENTER);
                printama.printTextlnWideTallBold(String.valueOf(ticket.getQueue_no()), Printama.CENTER);
                printama.printDashedLine();
                printama.printTextln("(" + ticket.getFacility() + ")", Printama.CENTER);
                printama.addNewLine();
                printama.printTextln("Name: " + ticket.getName(), Printama.LEFT);
                printama.printTextln("Purpose(s): " + ticket.getPurpose(), Printama.LEFT);
                printama.printDoubleDashedLine();
                printama.feedPaper();
                printama.close();
            }, this::showToast);
        }
        dialog.dismiss();
    }

    private void showPrinterList() {
        Printama.showPrinterList(this, R.color.colorBlue, printerName -> {
            Toast.makeText(this, printerName, Toast.LENGTH_SHORT).show();
            String text = "Connected to : " + printerName;
            if (printerName.contains("failed")) {
                text = printerName;
            }
            findPrinterBtn.setText(text);
        });
    }


    private void setToolBar() {
        toolbar = findViewById(R.id.toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton doneBtn = toolbar.findViewById(R.id.right_icon);
        doneBtn.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
//                    .setTitle("")
                    .setMessage("Go back to main menu?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            logEditor.clear();
                            logEditor.apply();
                            finish();
                            startActivity(new Intent(TicketsActivity.this, MainActivity.class));
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
        });
        back.setOnClickListener(v -> onBackPressed());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
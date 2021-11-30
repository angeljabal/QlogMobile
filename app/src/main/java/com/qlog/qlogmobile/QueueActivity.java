package com.qlog.qlogmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

//import com.qlog.qlogmobile.adapters.QueueAdapter;
import com.qlog.qlogmobile.adapters.TicketAdapter;
import com.qlog.qlogmobile.model.Queue;
import com.qlog.qlogmobile.model.Ticket;

import java.util.ArrayList;
import java.util.List;

public class QueueActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<Queue> queueList;
//    public QueueAdapter queueAdapter;
    public RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        init();
    }

    private void init(){
        queueList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.queueRecyclerView);
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

//        queueAdapter = new QueueAdapter(queueList, this);
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(queueAdapter);
    }
}
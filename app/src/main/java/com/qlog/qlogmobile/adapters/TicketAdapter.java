package com.qlog.qlogmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.model.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

    public List<Ticket> ticketList;
    public Context context;

    public TicketAdapter(List<Ticket> ticketList, Context context) {
        this.ticketList = ticketList;
        this.context = context;
    }

    @NonNull
    @Override
    public TicketAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View view = inflater.inflate(R.layout.recyclerview_ticket, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketAdapter.ViewHolder holder, int position) {
        final Ticket ticket = ticketList.get(position);

        holder.queueNumberText.setText(String.valueOf(ticket.getQueue_no()));
        holder.facilityText.setText(ticket.getFacility());
        holder.nameText.setText(ticket.getName());


        String[] arr = ticket.getPurpose().split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i != arr.length - 1) {
                stringBuilder.append("\u2022 ").append(arr[i]).append("\n");
            } else {
                stringBuilder.append("\u2022 ").append(arr[i]);
            }
        }
        holder.purposeText.setText(stringBuilder.toString());


    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView queueNumberText, facilityText, nameText, purposeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            queueNumberText = itemView.findViewById(R.id.queueNumberText);
            facilityText = itemView.findViewById(R.id.facilityText);
            nameText = itemView.findViewById(R.id.nameText);
            purposeText = (TextView) itemView.findViewById(R.id.purposeText);
        }

    }
}
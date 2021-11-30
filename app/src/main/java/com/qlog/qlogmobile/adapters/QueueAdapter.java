//package com.qlog.qlogmobile.adapters;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.qlog.qlogmobile.QueueActivity;
//import com.qlog.qlogmobile.R;
//import com.qlog.qlogmobile.model.Queue;
//import com.qlog.qlogmobile.model.Ticket;
//
//
//import java.util.List;
//
//public class QueueAdapter extends  RecyclerView.Adapter<QueueAdapter.ViewHolder> {
//    public List<Queue> queueList;
//    public Context context;
//
//    public QueueAdapter(List<Queue> queueList, Context context) {
//        this.queueList = queueList;
//        this.context = context;
//    }
//
//    @NonNull
//    @Override
//    public QueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        context = parent.getContext();
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        // Inflate the custom layout
//        View view = inflater.inflate(R.layout.recyclerview_ticket, parent, false);
//
//        // Return a new holder instance
//        TicketAdapter.ViewHolder viewHolder = new QueueAdapter().ViewHolder(view);
//        return viewHolder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TicketAdapter.ViewHolder holder, int position) {
//        final Ticket ticket = ticketList.get(position);
//
//        holder.queueNumberText.setText(String.valueOf(ticket.getQueue_no()));
//        holder.facilityText.setText(ticket.getFacility());
//        holder.nameText.setText(ticket.getName());
//        holder.purposeText.setText(ticket.getPurpose());
//    }
//
//    @Override
//    public int getItemCount() {
//        return queueList.size();
//    }
//
//    public class ViewHolder extends RecyclerView.ViewHolder {
//
//        public TextView queueNumberText, facilityText, nameText, purposeText;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            queueNumberText = (TextView) itemView.findViewById(R.id.queueNumberText);
//            facilityText = (TextView) itemView.findViewById(R.id.facilityText);
//            nameText = (TextView) itemView.findViewById(R.id.nameText);
//            purposeText = (TextView) itemView.findViewById(R.id.purposeText);
//        }
//    }
//}

package com.qlog.qlogmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.model.Purpose;

import java.util.ArrayList;

public class SelectedAdapter extends RecyclerView.Adapter<SelectedAdapter.ViewHolder> {

    public ArrayList<Purpose> selectedList;
    public Context context;

    public SelectedAdapter(ArrayList<Purpose> selectedList, Context context) {
        this.selectedList = selectedList;
        this.context = context;
    }

    @NonNull
    @Override
    public SelectedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View view = inflater.inflate(R.layout.recyclerview_selected, parent, false);

        // Return a new holder instance
        SelectedAdapter.ViewHolder viewHolder = new SelectedAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedAdapter.ViewHolder holder, int position) {
        final Purpose purpose = selectedList.get(position);
        holder.purposeText.setText("\u2022 " + purpose.getTitle());
    }


    @Override
    public int getItemCount() {
        return selectedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView  purposeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            purposeText = (TextView) itemView.findViewById(R.id.purposeText);
        }
    }
}

package com.qlog.qlogmobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.model.Facility;

import java.util.ArrayList;

public class FacilityAdapter extends RecyclerView.Adapter<FacilityAdapter.ViewHolder> {
    public ArrayList<Facility> facilityList;
    public Context context;

    public FacilityAdapter(ArrayList<Facility> facilityList, Context context){
        this.facilityList = facilityList;
        this.context = context;
    }

    @NonNull
    @Override
    public FacilityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_facility, parent, false);

        FacilityAdapter.ViewHolder viewHolder = new FacilityAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FacilityAdapter.ViewHolder holder, int position) {
        holder.bind(facilityList.get(position));
    }

    @Override
    public int getItemCount() {
        return facilityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView facilityText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(Facility facility){
            facilityText.setText(facility.getName());
        }
    }
}

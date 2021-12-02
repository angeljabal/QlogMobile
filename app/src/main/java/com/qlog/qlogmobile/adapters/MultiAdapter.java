package com.qlog.qlogmobile.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.qlog.qlogmobile.MyInterface;
import com.qlog.qlogmobile.PurposeActivity;
import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.model.Purpose;

import java.util.ArrayList;
import java.util.Locale;

public class MultiAdapter extends RecyclerView.Adapter<MultiAdapter.MultiViewHolder> implements Filterable {

    public ArrayList<Purpose> purposes;
    public ArrayList<Purpose> purposeListAll;
    public Context context;
    private final MyInterface listener;

    public MultiAdapter(ArrayList<Purpose> purposes, Context context) {
        this.purposes = purposes;
        this.context = context;
        this.purposeListAll = new ArrayList<>(purposes);
        this.listener = ((MyInterface) context);
    }

    @NonNull
    @Override
    public MultiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.item_purposes, parent, false);
       return new MultiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiViewHolder holder, int position) {
        holder.bind(purposes.get(position));
    }

    @Override
    public int getItemCount() {
        return purposes.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Purpose> filteredList = new ArrayList<>();
            if(charSequence == null || charSequence.length()==0){
                filteredList.addAll(purposeListAll);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(Purpose purpose: purposeListAll){
                    if(purpose.getTitle().toLowerCase().contains(filterPattern)){
                        filteredList.add(purpose);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            purposes.clear();
            purposes.addAll((ArrayList)filterResults.values);
            notifyDataSetChanged();
        }
    };


    public class MultiViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private CardView purpose_cv;

        public MultiViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.purpose_name);
            purpose_cv = itemView.findViewById(R.id.purpose_cv);
        }

        @SuppressLint("ResourceAsColor")
        void bind(final Purpose purpose){
            purpose_cv.setCardBackgroundColor(purpose.isSelected() ? Color.parseColor("#0891B2") : Color.WHITE);
            textView.setTextColor(purpose.isSelected() ? Color.WHITE : Color.DKGRAY);
            textView.setText(purpose.getTitle());

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    purpose.setSelected(!purpose.isSelected());
                    purpose_cv.setCardBackgroundColor(purpose.isSelected() ? Color.parseColor("#0891B2") : Color.WHITE);
                    textView.setTextColor(purpose.isSelected() ? Color.WHITE : Color.DKGRAY);
                    listener.getSelected();
                }
            });

        }
    }
//
//    public ArrayList<Purpose> getAll() {return purposes;}

    public ArrayList<Purpose> getSelected(){

        ArrayList<Purpose> selected = new ArrayList<>();
        for(int i=0; i < purposes.size() ; i++){

            if(purposes.get(i).isSelected()){
                selected.add(purposes.get(i));
            }
        }
        return selected;
    }

}

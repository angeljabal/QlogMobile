package com.qlog.qlogmobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qlog.qlogmobile.ConfirmDataActivity;
import com.qlog.qlogmobile.PurposeActivity;
import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.model.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>  implements Filterable {

    public ArrayList<User> usersList;
    public ArrayList<User> usersListAll;
    public Context context;

    public UserAdapter(ArrayList<User> usersList, Context context) {
        this.usersList = usersList;
        this.usersListAll = new ArrayList<>(usersList);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_users, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(usersList.get(position));
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<User> filteredList = new ArrayList<>();
            if(charSequence == null || charSequence.length()==0){
                filteredList.addAll(usersListAll);
            } else {
                String filterPattern = charSequence.toString().toLowerCase().trim();
                for(User user: usersListAll){
                    if(user.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            usersList.clear();
            usersList.addAll((ArrayList)filterResults.values);
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userType = itemView.findViewById(R.id.user_type);
        }

        void bind(final User user){
            userName.setText(user.getName());
            userType.setText(user.getType());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences logPref = context.getSharedPreferences("log", Context.MODE_PRIVATE);
                    SharedPreferences.Editor logEditor = logPref.edit();

                    logEditor.putString("user_id", String.valueOf(user.getId()));
                    logEditor.putString("name", user.getName());
                    logEditor.putString("address", user.getAddress());
                    logEditor.putString("phone_number", user.getPhone_number());
                    logEditor.apply();

                    Toast.makeText(context, user.getName() + " selected successfully.", Toast.LENGTH_SHORT).show();
                    if(logPref.getBoolean("isWalkin", true)){
                        context.startActivity(new Intent(context, ConfirmDataActivity.class));
                    }else{
                        context.startActivity(new Intent(context, PurposeActivity.class));
                    }
                }
            });
        }
    }
}

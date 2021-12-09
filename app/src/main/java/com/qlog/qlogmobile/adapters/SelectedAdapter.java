package com.qlog.qlogmobile.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.Purpose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
        View view = inflater.inflate(R.layout.recyclerview_selected, parent, false);
        SelectedAdapter.ViewHolder viewHolder = new SelectedAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedAdapter.ViewHolder holder, int position) {
        holder.bind(selectedList.get(position));
    }


    @Override
    public int getItemCount() {
        return selectedList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView purposeText;
        private TextView selectFacility;
        private RelativeLayout selected_rl;
        private RelativeLayout.LayoutParams params;
        private Dialog facilityDialog;
        private ArrayList<String> departmentList;
        private SharedPreferences userPref;
         public ViewHolder(@NonNull View itemView) {
            super(itemView);
            selected_rl = itemView.findViewById(R.id.selected_layout);
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            purposeText = (TextView) itemView.findViewById(R.id.purposeText);
            selectFacility = new TextView(context);
            facilityDialog = new Dialog(context);
            userPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        }

        void bind(final Purpose purpose){
            purposeText.setText(purpose.getTitle());
            if(purpose.hasRequiredFacility()){
                params.addRule(RelativeLayout.BELOW, purposeText.getId());
                params.setMargins(2,10,2,2);
                selectFacility.setLayoutParams(params);
                selectFacility.setHint("Select Department");
                selectFacility.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_baseline_keyboard_arrow_down_24, 0);
                selectFacility.setBackground(context.getResources().getDrawable(android.R.drawable.editbox_background));
                selectFacility.setPadding(35,35,35,35);
//                selectFacility.getBackground().setTint(Color.valueOf(11000000));
                selectFacility.setTextColor(Color.GRAY);
                selected_rl.addView(selectFacility);

                departmentList = new ArrayList<>();
                StringRequest purposesRequest = new StringRequest(Request.Method.GET, Constant.DEPARTMENTS, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")) {
                            JSONArray array = new JSONArray(object.getString("departments"));
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject purposeObject = array.getJSONObject(i);
                                departmentList.add(purposeObject.getString("code"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    error.printStackTrace();
                }) {
                    // provide token in header
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = userPref.getString("token", "");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + token);
                        return map;
                    }
                };

                RequestQueue purposeQueue = Volley.newRequestQueue(context);
                purposeQueue.add(purposesRequest);

                selectFacility.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        facilityDialog.setContentView(R.layout.searchable_spinner);
                        facilityDialog.getWindow().setLayout(800, 800);
                        facilityDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        facilityDialog.show();

                        EditText search_box = facilityDialog.findViewById(R.id.search_box);
                        TextView title = facilityDialog.findViewById(R.id.title);
                        ListView departmentListView = facilityDialog.findViewById(R.id.searchable_list);

                        title.setText("Select Department");
                        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, departmentList);
                        departmentListView.setAdapter(departmentAdapter);

                        search_box.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                departmentAdapter.getFilter().filter(charSequence);
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });

                        departmentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                selectFacility.setText(departmentAdapter.getItem(i));
                                SharedPreferences deptPref = context.getSharedPreferences("department", Context.MODE_PRIVATE);
                                SharedPreferences.Editor deptEditor = deptPref.edit();
                                StringBuilder idSb = new StringBuilder();
                                StringBuilder titleSb = new StringBuilder();

                                deptEditor.putInt("purpose_id", purpose.getId());
                                deptEditor.putString("code", departmentAdapter.getItem(i));
                                deptEditor.apply();
//                                logEditor.putString("purpose", String.valueOf(purposeText.getText()));
//                                logEditor.apply();
                                facilityDialog.dismiss();
                            }
                        });
                    }
                });
            }
        }
    }
}

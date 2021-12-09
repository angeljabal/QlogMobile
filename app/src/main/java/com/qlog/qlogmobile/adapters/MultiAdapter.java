package com.qlog.qlogmobile.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qlog.qlogmobile.ConfirmDataActivity;
import com.qlog.qlogmobile.R;
import com.qlog.qlogmobile.constants.Constant;
import com.qlog.qlogmobile.model.Purpose;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MultiAdapter extends RecyclerView.Adapter<MultiAdapter.ViewHolder> {

    public ArrayList<Purpose> purposeList;
    public Context context;

    public MultiAdapter(ArrayList<Purpose> purposeList, Context context) {
        this.purposeList = purposeList;
        this.context = context;
    }

    @NonNull
    @Override
    public MultiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerview_selected, parent, false);
        MultiAdapter.ViewHolder viewHolder = new MultiAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MultiAdapter.ViewHolder holder, int position) {
        holder.bind(purposeList.get(position));
    }

    @Override
    public int getItemCount() {
        return purposeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView purposeText, facilityText;
        private RelativeLayout selected_rl;
        private RelativeLayout.LayoutParams params;
        private ArrayList<String> facilities;
        private SharedPreferences userPref;
        private ProgressDialog dialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            purposeText = (TextView) itemView.findViewById(R.id.purposeText);
            selected_rl = itemView.findViewById(R.id.selected_layout);
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            userPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            facilities = new ArrayList<>();
            facilityText = new TextView(context);
            dialog = new ProgressDialog(context);

        }

        void bind(final Purpose purpose){
            dialog.show();
            purposeText.setText("\u2023 " +purpose.getTitle());
            purposeText.setTextColor(ContextCompat.getColor(context, R.color.teal_600));
            params.addRule(RelativeLayout.BELOW, purposeText.getId());
            params.setMargins(20,0,2,2);
            facilityText.setLayoutParams(params);
            facilityText.setTextColor(ContextCompat.getColor(context, R.color.teal_600));
            facilityText.setTextSize(13);
            StringRequest purposesRequest = new StringRequest(Request.Method.POST, Constant.FACILITIES, response -> {
                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")) {
                        JSONArray facilitiesArray = new JSONArray(object.getString("facilities"));
                        StringBuilder idSb = new StringBuilder();
                        StringBuilder nameSb = new StringBuilder();
                        for (int i = 0; i < facilitiesArray.length(); i++) {
                            JSONObject facilitiesObject = facilitiesArray.getJSONObject(i);
                            if(purpose.hasRequiredFacility()){
                                SharedPreferences deptPref = context.getSharedPreferences("department", Context.MODE_PRIVATE);
                                    if(facilitiesObject.getString("code").equals(deptPref.getString("code", null))){
                                        nameSb.append("\u2022 ").append(facilitiesObject.getString("name")).append("\n");
                                    }
                            }
                            else{
                                nameSb.append("\u2022 ").append(facilitiesObject.getString("name")).append("\n");
                            }
//                            if (i != facilitiesArray.length() - 1) {
//                                nameSb.append("\n");
//                            }
                        }
                        facilityText.setText(nameSb.toString());
                        selected_rl.addView(facilityText);
                        dialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                }
            }, error -> {
                error.printStackTrace();
                dialog.dismiss();
            }) {
                // provide token in header
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
                    map.put("id", String.valueOf(purpose.getId()));
                    return map;
                }
            };

            RequestQueue purposeQueue = Volley.newRequestQueue(context);
            purposeQueue.add(purposesRequest);
        }
    }
}

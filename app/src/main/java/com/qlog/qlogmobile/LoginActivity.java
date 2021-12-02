package com.qlog.qlogmobile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.qlog.qlogmobile.constants.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailField, passwordField;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    private void init() {
        emailLayout = (TextInputLayout) findViewById(R.id.emailLayout);
        passwordLayout = (TextInputLayout) findViewById(R.id.passwordLayout);
        CardView loginButton = (CardView) findViewById(R.id.loginButton);
        emailField = (TextInputEditText) findViewById(R.id.emailField);
        passwordField = (TextInputEditText) findViewById(R.id.passwordField);
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setCancelable(false);
        loginButton.setOnClickListener(view -> {
            closeKeyBoard();
            if (validate()) {
                dialog.setMessage("Logging in");
                dialog.show();
                StringRequest request = new StringRequest(Request.Method.POST, Constant.LOGIN, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (object.getBoolean("success")) {
                            if (object.getBoolean("hasPermission")) {
                                SharedPreferences userPref = this.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = userPref.edit();

                                editor.putString("token", object.getString("token"));
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();

                                if (TextUtils.equals(object.getString("facility"), "null")) {
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    editor.putString("facility", object.getString("facility"));
                                    editor.apply();
                                    startActivity(new Intent(LoginActivity.this, ScanQR.class));
                                    Toast.makeText(this, "Login Success", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            } else {
                                Snackbar.make(view, "You are not authorized to access this application", Snackbar.LENGTH_LONG).show();
                            }
                        }
                        dialog.dismiss();
                    } catch (JSONException e) {
                        Snackbar.make(view, "These credentials do not match our records.", Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Snackbar.make(view, error.toString(), Snackbar.LENGTH_LONG).show();
                        error.printStackTrace();
                    }
                }) {
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("email", emailField.getText().toString().trim());
                        map.put("password", passwordField.getText().toString().trim());
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                request.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(request);
            }
        });

        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!emailField.getText().toString().isEmpty()) {
                    emailLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!passwordField.getText().toString().isEmpty()) {
                    passwordLayout.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean validate() {
        if (emailField.getText().toString().isEmpty()) {
            emailLayout.setErrorEnabled(true);
            emailLayout.setError("Email is required");
            return false;
        }

        if (passwordField.getText().toString().isEmpty()) {
            passwordLayout.setErrorEnabled(true);
            passwordLayout.setError("Password is required");
            return false;
        }
        return true;
    }

    private void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
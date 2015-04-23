package com.github.hiiyl.mmuhub;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.gc.materialdesign.views.ButtonRectangle;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.sync.MMUSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends ActionBarActivity {
    private EditText student_id;
//    private EditText camsys_password;
    private EditText mmls_password;
//    private EditText icems_password;
    private ButtonRectangle login_btn;
    private Context mContext;
    private String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String LOGGED_IN_PREF_TAG = "logged_in";
        mContext = this;
        boolean logged_in = prefs.getBoolean(LOGGED_IN_PREF_TAG, false);
        if(logged_in) {
            finish();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupVariables();
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_btn.setEnabled(false);
                login_btn.setText("Logging In");
                authenticatePassword();
            }
        });

    }

    private void setupVariables() {
        student_id = (EditText) findViewById(R.id.student_id_field);
//        camsys_password = (EditText) findViewById(R.id.camsys_pass_field);
        mmls_password = (EditText) findViewById(R.id.mmls_pass_field);
//        icems_password = (EditText) findViewById(R.id.icems_pass_field);
        login_btn = (ButtonRectangle) findViewById(R.id.loginBtn);

    }
    public void requestAuthentication() {
        RequestQueue queue = MySingleton.getInstance(this).
                getRequestQueue();
        String url = "https://mmu-api.herokuapp.com/login_mmls";
        final ProgressDialog mProgressDialog = new ProgressDialog(this);

        mProgressDialog.setTitle("Signing you in...");
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressDialog.dismiss();

                String profile = Utility.trimMessage(response, "profile");
                String name = Utility.trimMessage(profile, "name");
                String faculty = Utility.trimMessage(profile, "faculty");
                String cookie = Utility.trimMessage(response, "cookie");
                String token = Utility.trimMessage(response, "token");
                try {
                    MMUDbHelper helper = new MMUDbHelper(LoginActivity.this);
                    JSONArray subjectArray = new JSONArray(Utility.trimMessage(response, "subjects"));
                    for (int i = 0; i < subjectArray.length(); i++) {
                        JSONObject subject = subjectArray.getJSONObject(i);
                        String subject_uri = subject.getString("uri");
                        String subject_name = subject.getString("name");
                        ContentValues subjectValues = new ContentValues();
                        subjectValues.put(MMUContract.SubjectEntry.COLUMN_NAME, subject_name);
                        subjectValues.put(MMUContract.SubjectEntry.COLUMN_URL, subject_uri);
                        MySingleton.getInstance(LoginActivity.this).getDatabase().insert(MMUContract.SubjectEntry.TABLE_NAME, null, subjectValues);
                        Log.d("LoginActivity", "Inserted " + subject_name + " AND " + subject_uri);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", true);
                editor.putString("student_id", student_id.getText().toString());
                editor.putString("cookie", cookie);
                editor.putString("token", token);
//                editor.putString("camsys_password", camsys_password.getText().toString());
                editor.putString("mmls_password", mmls_password.getText().toString());
//                editor.putString("icems_password", icems_password.getText().toString());
                editor.putString("name", name);
                editor.putString("faculty", faculty);
                editor.apply();
                MMUSyncAdapter.syncImmediately(LoginActivity.this);
                Intent intent = new Intent(mContext, MMLSActivity.class);
                startActivity(intent);
                finish();
            }
        }, new Response.ErrorListener() {

            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressDialog.dismiss();
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null && networkResponse.data != null){
                    switch(networkResponse.statusCode){
                        case 400:
                            SnackBar snackbar = new SnackBar(LoginActivity.this, "Wrong Username or Password");
                            snackbar.show();

                            break;
                        default:
                            SnackBar new_snackbar = new SnackBar(LoginActivity.this, "Internal Server Error",
                                    "Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    requestAuthentication();
                                    finish();
                                }
                            });
                            new_snackbar.show();
                    }
                    //Additional cases
                }
                else
                {
                    SnackBar snackbar = new SnackBar(LoginActivity.this, "No Internet Connection",
                            "Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestAuthentication();
                            finish();
                        }
                    });
                    snackbar.show();
                }
                login_btn.setText("Submit");
                login_btn.setEnabled(true);
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Log.d(TAG, "" + error.getMessage() + "," + error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("student_id", student_id.getText().toString());
//                params.put("camsys_password", camsys_password.getText().toString());
                params.put("mmls_password", mmls_password.getText().toString());

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Content-Type","application/x-www-form-urlencoded");
                headers.put("abc", "value");
                return headers;
            }
        };
        sr.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(sr);
    }

    public void authenticatePassword() {
        if(isEmpty(student_id) || isEmpty(mmls_password))
        {
            Toast.makeText(getApplicationContext(), "All fields must be filled",
                    Toast.LENGTH_SHORT).show();
            login_btn.setText("Submit");
            login_btn.setEnabled(true);
            return;
        }
        requestAuthentication();
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

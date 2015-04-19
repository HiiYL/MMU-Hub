package com.github.hiiyl.mmuhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {
    public static final String LOGGED_IN_PREF_TAG = "logged_in";
    private TextView welcome_text;
    private TextView faculty_text;
    private TextView student_id_textview;
    private Button pager_button;

    static List<String> announcement_list = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean logged_in = prefs.getBoolean(LOGGED_IN_PREF_TAG, false);

        if(!logged_in) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_main);
        welcome_text = (TextView) findViewById(R.id.welcome_text);
        faculty_text = (TextView) findViewById(R.id.faculty_text);
        student_id_textview = (TextView) findViewById(R.id.student_id_textview);
        pager_button = (Button) findViewById(R.id.pager_button);


        welcome_text.setText("Welcome, " + prefs.getString("name", ""));
        student_id_textview.setText(prefs.getString("student_id", ""));
        faculty_text.setText(prefs.getString("faculty", ""));
        pager_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MMLSActivity.class);
                startActivity(intent);
            }
        });
        getAnnouncementJSON();
    }
    private void setupVariables() {

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if (id == R.id.action_logout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.commit();
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public List<String> getAnnouncementJSON() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://mmu-api.herokuapp.com/mmls_api";
//        String url = "https://mmu-api.herokuapp.com/login_test.json";
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    Toast.makeText(MainActivity.this, "TRYING TO SUBMIT", Toast.LENGTH_SHORT);
                    JSONArray jarray = new JSONArray(response);
                    for(int i = 0; i < jarray.length(); i++)
                    {
                        JSONObject jobj = jarray.getJSONObject(i);
                        JSONArray weeks = jobj.getJSONArray("weeks");
                        if(weeks != null) {
                            for(int j = 0; j < weeks.length(); j++) {
                                JSONObject week_obj = weeks.getJSONObject(j);
                                JSONArray announcements = week_obj.getJSONArray("announcements");
                                if (announcements != null) {
                                    for (int k = 0; k < announcements.length(); k++) {
                                        JSONObject announcement = announcements.getJSONObject(k);
                                        String announcement_contents = announcement.getString("contents");
                                        announcement_list.add(announcement_contents);
                                    }
                                }
                            }
                        }
                    }
                    Toast.makeText(MainActivity.this, "POST DATA DONE", Toast.LENGTH_SHORT);

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null && networkResponse.data != null){
                    switch(networkResponse.statusCode){
                        case 400:
                            json = new String(networkResponse.data);
                            json = Utility.trimMessage(json, "message");
                            if(json != null)
                                Toast.makeText(MainActivity.this, json, Toast.LENGTH_SHORT);
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT);
                    }
                    //Additional cases
                }
                else
                {
                    Toast.makeText(MainActivity.this, "NO INTERNET CONNECTION", Toast.LENGTH_SHORT);
                }
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                params.put("student_id", prefs.getString("student_id", ""));
                params.put("password", prefs.getString("mmls_password", ""));
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
        return announcement_list;
    };
}

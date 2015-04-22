package com.github.hiiyl.mmuhub;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hii on 4/13/15.
 */
public class Utility {
    private static SharedPreferences prefs;
    public static String trimMessage(String json, String key){
        String trimmedString = null;

        try{
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch(JSONException e){
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }
    public static void refreshToken(Context context) {
        Context mContext = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        RequestQueue queue = Volley.newRequestQueue(context);
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Refreshing Token & Cookie...");
        progressDialog.setMessage("Please Wait");
        progressDialog.show();
        String url = "https://mmu-api.herokuapp.com/refresh_token";
//        String url = "https://mmu-api.herokuapp.com/login_test.json";
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                SharedPreferences.Editor editor = prefs.edit();
                String cookie = Utility.trimMessage(response, "cookie");
                String token = Utility.trimMessage(response, "token");
                editor.putString("cookie", cookie);
                editor.putString("token", token);
                editor.commit();
                Log.d("Token", "Successful");
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("Token", "Refresh unsuccessful");
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("student_id", prefs.getString("student_id", ""));
                params.put("password", prefs.getString("mmls_password",""));
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
}

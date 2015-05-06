package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.helper.RefreshTokenEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by Hii on 4/13/15.
 */
public class Utility {
    private static SharedPreferences prefs;
    public static final String SYNC_FINISHED = "sync finish";
    public static final String SYNC_BEGIN = "sync begin";
    public static final String DOWNLOAD_FOLDER = "MMUHub Downloads";

    static final String REFRESH_TOKEN_STARTING = "Session Cookie Expired. Refreshing ...";
    static final String REFRESH_TOKEN_COMPLETE = "Session Refreshed. Please Retry Download";
    static final String REFRESH_TOKEN_FAILED = "Session Refresh Failed";

    static final String VIEW_ANNOUNCEMENT_EVENT = "view announcement";

    static final String VIEW_BULLETIN_EVENT = "view bulletin";

    static final String ANNOUNCEMENT_ATTACHMENT_FOLDER = "Announcement Attachments";

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

    public static int getSubjectCount(Context context) {
        Cursor cursor = MySingleton.getInstance(context).getDatabase().query(MMUContract.SubjectEntry.TABLE_NAME, null,null,null,null,null,null);
        int subject_count = cursor.getCount();
        cursor.close();
        return subject_count;
    }
    public static String getSubjectName(Context context, String subject_id) {
        Cursor cursor = MySingleton.getInstance(context).getDatabase().query(MMUContract.SubjectEntry.TABLE_NAME,
                null, MMUContract.SubjectEntry._ID + " = ?",
                new String[]{subject_id},null,null,null);
        cursor.moveToFirst();
        String subject_name = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_NAME));
        cursor.close();
        return subject_name;
    }
    public static void refreshToken(Context context) {
        EventBus.getDefault().postSticky(new RefreshTokenEvent(REFRESH_TOKEN_STARTING));
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://mmu-api.co/refresh_token";
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                EventBus.getDefault().postSticky(new RefreshTokenEvent(REFRESH_TOKEN_COMPLETE));
                SharedPreferences.Editor editor = prefs.edit();
                String cookie = Utility.trimMessage(response, "cookie");
                String token = Utility.trimMessage(response, "token");
                editor.putString("cookie", cookie);
                editor.putString("token", token);
                editor.apply();
                Log.d("Token", "Successful");
            }
        }, new Response.ErrorListener() {
            String json = null;
            @Override
            public void onErrorResponse(VolleyError error) {
                EventBus.getDefault().postSticky(new RefreshTokenEvent(REFRESH_TOKEN_FAILED));
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
        queue.add(sr);
    }
    public static boolean isNetworksAvailable(Context context) {
        ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnMgr != null)  {
            NetworkInfo[] mNetInfo = mConnMgr.getAllNetworkInfo();
            if (mNetInfo != null) {
                for (int i = 0; i < mNetInfo.length; i++) {
                    if (mNetInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        Log.e("isNetworkAvailable", "NO INTERNET");
        SnackBar snackBar = new SnackBar((android.app.Activity) context, "No Internet Connection");
        snackBar.show();
        return false;
    }
    public static boolean getNotificationsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("notifications_enabled", true);
    }
    public static boolean isFirstSync(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("first_sync", true);
    }
    public static void setFirstSync(Context context, boolean is_first_sync) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("first_sync", is_first_sync);
        editor.apply();
    }
    public static String humanizeDate(String date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("MMM dd");
        try {
            Date parsed_date = sdf.parse(date);
            return shortenedDateFormat.format(parsed_date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }
    public static void updateLastSyncDate(Context context) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        String date = df.format(Calendar.getInstance().getTime());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Log.d("Last Sync Date", date);
        editor.putString("last_sync", date);
        editor.apply();
    }
    public static String getLastSyncDate(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("last_sync", "");
    }
}

package com.github.hiiyl.mmuhub;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.hiiyl.mmuhub.data.MMUContract;
import com.github.hiiyl.mmuhub.helper.AttendanceCompleteEvent;
import com.github.hiiyl.mmuhub.helper.RefreshTokenEvent;
import com.github.hiiyl.mmuhub.helper.StartPreviousActivityEvent;

import org.json.JSONArray;
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
        final SharedPreferences.Editor editor = prefs.edit();

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://mmu-api.co/refresh_token";
        StringRequest sr = new StringRequest(Request.Method.POST, url , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                EventBus.getDefault().postSticky(new RefreshTokenEvent(REFRESH_TOKEN_COMPLETE));
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
        sr.setRetryPolicy(new DefaultRetryPolicy(
                0,
                0,
                0));
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
        Log.d("GET LAST SYNC", prefs.getString("last_sync", ""));
        return prefs.getString("last_sync", "");
    }
    public static boolean isCamsysLoggedIn(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains("camsys_password");
    }
    public static String getStudentID(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("student_id", "");

    }
    public static boolean camsysAuthenticate(final Context context) {
        String m_Text;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Additional Credentials Required");
        builder.setMessage("Camsys Password");

        builder.setCancelable(false);
// Set up the input
        final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                final SharedPreferences.Editor editor = prefs.edit();
                final String camsys_password = input.getText().toString();
                final String TAG = "camsysAuthenticate";
                RequestQueue queue = MySingleton.getInstance(context).
                        getRequestQueue();
                String url = "https://mmu-api.co/login_camsys_v2";
                final ProgressDialog mProgressDialog = new ProgressDialog(context);

                mProgressDialog.setTitle("Signing you in...");
                mProgressDialog.setMessage("Please wait");
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jObj = new JSONObject(response);
                            try {
                                JSONArray jArray = jObj.getJSONArray("subjects_attendance");
                                for (int i = 0; i < jArray.length(); i++) {
                                    JSONObject attendanceObj = jArray.getJSONObject(i);
                                    String subject_area = attendanceObj.getString("Subject Area");
                                    String subject_number = attendanceObj.getString("Subject/Catalogue#");
                                    String subject_descrption = attendanceObj.getString("Course Description");
                                    String current_attendance = attendanceObj.getString("Current Attendance %");
                                    String course_component = attendanceObj.getString("Course Component");
                                    String subject_name = subject_area + subject_number + " - " + subject_descrption;
                                    Cursor cursor = MySingleton.getInstance(context).getDatabase().query(
                                            MMUContract.SubjectEntry.TABLE_NAME,
                                            new String[]{MMUContract.SubjectEntry._ID},
                                            MMUContract.SubjectEntry.COLUMN_NAME + " = ? ", new String[]{subject_name}, null, null, null
                                    );

                                    Log.d("SUBJECT_NAME", subject_name);
                                    Log.d("REQUEST", subject_name);
                                    if (cursor.moveToFirst()) {
                                        String subject_id = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry._ID));
                                        Log.d("SUBJECT_ID", cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry._ID)));

                                        ContentValues attendanceValues = new ContentValues();
                                        if (course_component.equals("Lecture")) {
                                            attendanceValues.put(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LECTURE, current_attendance);
                                        } else if (course_component.equals("Tutorial")) {
                                            attendanceValues.put(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL, current_attendance);
                                        }
                                        MySingleton.getInstance(context).getDatabase().update(MMUContract.SubjectEntry.TABLE_NAME,
                                                attendanceValues, MMUContract.SubjectEntry._ID + " = ? ", new String[]{subject_id});
                                    } else {
                                        Log.d("SUBJECT", "SUBJECT NOT FOUND");
                                    }
                                }
                            }catch (JSONException e) {
                                Log.d("NO ATTENDANCE", "FINANCIALLY BARRED?");

                            }
                            try {
                                String feesDue = jObj.getString("amount_due");
                                editor.putString("fees_due", feesDue);
                                editor.apply();
                            }catch (JSONException e) {
                                Log.d("NO FEES DUE", "ALL PAID?");
                                editor.remove("fees_due");
                                editor.apply();
                            }
                            try {
                                JSONArray examTimetableArray = jObj.getJSONArray("exam_timetable");
                                for(int i = 0; i < examTimetableArray.length(); i++) {
                                    JSONObject examTimetableObj = examTimetableArray.getJSONObject(i);
                                    String subject_code = examTimetableObj.getString("Catalog Nbr");
                                    String subject_name = examTimetableObj.getString("Description");
                                    String exam_date = examTimetableObj.getString("Exam Date");
                                    String exam_start_time = examTimetableObj.getString("Exam Start Time");
                                    String exam_end_time = examTimetableObj.getString("Exam End Time");
                                    String full_subject_name = subject_code + " - " + subject_name;
                                    Cursor cursor = MySingleton.getInstance(context).getDatabase().query(
                                            MMUContract.SubjectEntry.TABLE_NAME,
                                            new String[]{MMUContract.SubjectEntry._ID},
                                            MMUContract.SubjectEntry.COLUMN_NAME + " = ? ",
                                            new String[] {full_subject_name},
                                            null,
                                            null,
                                            null
                                    );
                                    if(cursor.moveToFirst()) {
                                        String full_start_time = exam_date + " " + exam_start_time;
                                        String full_end_time = exam_date + " " + exam_end_time;
                                        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy h:mma");
                                        Date date_start_time = format.parse(full_start_time);
                                        Date date_end_time = format.parse(full_end_time);

                                        ContentValues examTimetableValues = new ContentValues();
                                        examTimetableValues.put(MMUContract.SubjectEntry.COLUMN_FINALS_START_DATETIME, date_start_time.getTime());
                                        examTimetableValues.put(MMUContract.SubjectEntry.COLUMN_FINALS_END_DATETIME, date_end_time.getTime());
                                        MySingleton.getInstance(context).getDatabase().update(
                                                MMUContract.SubjectEntry.TABLE_NAME,
                                                examTimetableValues,
                                                MMUContract.SubjectEntry._ID + " = ? ",
                                                new String[]{cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry._ID))}
                                        );

                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mProgressDialog.dismiss();
                        editor.putString("camsys_password", camsys_password);
                        editor.apply();
                        EventBus.getDefault().post(new AttendanceCompleteEvent("first_sync"));
                    }
                }, new Response.ErrorListener() {

                    String json = null;

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mProgressDialog.dismiss();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            switch (networkResponse.statusCode) {
                                case 400:
                                    SnackBar snackbar = new SnackBar((Activity) context, "Wrong Username or Password");
                                    snackbar.show();

                                    break;
                                default:
                                    SnackBar new_snackbar = new SnackBar((Activity) context, "Camsys server did not respond in time");
                                    new_snackbar.show();
                            }
                            //Additional cases
                        } else {
                            SnackBar snackbar = new SnackBar((Activity) context, "No Internet Connection");
                            snackbar.show();
                        }
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Log.d(TAG, "" + error.getMessage() + "," + error.toString());
                        camsysAuthenticate(context);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("student_id", getStudentID(context));
//                params.put("camsys_password", camsys_password.getText().toString());
                        params.put("camsys_password", camsys_password);

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        headers.put("abc", "value");
                        return headers;
                    }
                };
                sr.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        0,
                        0));
                queue.add(sr);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new StartPreviousActivityEvent());
            }
        });
        builder.show();
        return true;
    }
    public static void refreshAttendance(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        if(prefs.contains("camsys_password")) {
            final String camsys_password = prefs.getString("camsys_password", "");
            final String TAG = "AttendanceSync";
            RequestQueue queue = MySingleton.getInstance(context).
                    getRequestQueue();
            String url = "https://mmu-api.co/login_camsys_v2";
            StringRequest sr = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        try {
                            JSONArray jArray = jObj.getJSONArray("subjects_attendance");
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject attendanceObj = jArray.getJSONObject(i);
                                String subject_area = attendanceObj.getString("Subject Area");
                                String subject_number = attendanceObj.getString("Subject/Catalogue#");
                                String subject_descrption = attendanceObj.getString("Course Description");
                                String current_attendance = attendanceObj.getString("Current Attendance %");
                                String course_component = attendanceObj.getString("Course Component");
                                String subject_name = subject_area + subject_number + " - " + subject_descrption;
                                Cursor cursor = MySingleton.getInstance(context).getDatabase().query(
                                        MMUContract.SubjectEntry.TABLE_NAME,
                                        new String[]{MMUContract.SubjectEntry._ID},
                                        MMUContract.SubjectEntry.COLUMN_NAME + " = ? ", new String[]{subject_name}, null, null, null
                                );

                                Log.d("SUBJECT_NAME", subject_name);
                                Log.d("REQUEST", subject_name);
                                if (cursor.moveToFirst()) {
                                    String subject_id = cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry._ID));
                                    Log.d("SUBJECT_ID", cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry._ID)));

                                    ContentValues attendanceValues = new ContentValues();
                                    if (course_component.equals("Lecture")) {
                                        attendanceValues.put(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_LECTURE, current_attendance);
                                    } else if (course_component.equals("Tutorial")) {
                                        attendanceValues.put(MMUContract.SubjectEntry.COLUMN_ATTENDANCE_TUTORIAL, current_attendance);
                                    }
                                    MySingleton.getInstance(context).getDatabase().update(MMUContract.SubjectEntry.TABLE_NAME,
                                            attendanceValues, MMUContract.SubjectEntry._ID + " = ? ", new String[]{subject_id});
                                } else {
                                    Log.d("SUBJECT", "SUBJECT NOT FOUND");
                                }
                            }
                        }catch (JSONException e) {
                            Log.d("NO ATTENDANCE", "FINANCIALLY BARRED?");

                        }
                        try {
                            String feesDue = jObj.getString("amount_due");
                            editor.putString("fees_due", feesDue);
                            editor.apply();
                        }catch (JSONException e) {
                            Log.d("NO FEES DUE", "ALL PAID?");
                            editor.remove("fees_due");
                            editor.apply();
                        }
                        try {
                            JSONArray examTimetableArray = jObj.getJSONArray("exam_timetable");
                            for(int i = 0; i < examTimetableArray.length(); i++) {
                                JSONObject examTimetableObj = examTimetableArray.getJSONObject(i);
                                String subject_code = examTimetableObj.getString("Catalog Nbr");
                                String subject_name = examTimetableObj.getString("Description");
                                String exam_date = examTimetableObj.getString("Exam Date");
                                String exam_start_time = examTimetableObj.getString("Exam Start Time");
                                String exam_end_time = examTimetableObj.getString("Exam End Time");
                                String full_subject_name = subject_code + " - " + subject_name;
                                Cursor cursor = MySingleton.getInstance(context).getDatabase().query(
                                        MMUContract.SubjectEntry.TABLE_NAME,
                                        new String[]{MMUContract.SubjectEntry._ID},
                                        MMUContract.SubjectEntry.COLUMN_NAME + " = ? ",
                                        new String[] {full_subject_name},
                                        null,
                                        null,
                                        null
                                );
                                if(cursor.moveToFirst()) {
                                    String full_start_time = exam_date + " " + exam_start_time;
                                    String full_end_time = exam_date + " " + exam_end_time;
                                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy h:mma");
                                    Date date_start_time = format.parse(full_start_time);
                                    Date date_end_time = format.parse(full_end_time);

                                    ContentValues examTimetableValues = new ContentValues();
                                    examTimetableValues.put(MMUContract.SubjectEntry.COLUMN_FINALS_START_DATETIME, date_start_time.getTime());
                                    examTimetableValues.put(MMUContract.SubjectEntry.COLUMN_FINALS_END_DATETIME, date_end_time.getTime());
                                    MySingleton.getInstance(context).getDatabase().update(
                                            MMUContract.SubjectEntry.TABLE_NAME,
                                            examTimetableValues,
                                            MMUContract.SubjectEntry._ID + " = ? ",
                                            new String[]{cursor.getString(cursor.getColumnIndex(MMUContract.SubjectEntry._ID))}
                                    );

                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    EventBus.getDefault().post(new AttendanceCompleteEvent(""));
                }
            }, new Response.ErrorListener() {

                String json = null;

                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    if (networkResponse != null && networkResponse.data != null) {
                        switch (networkResponse.statusCode) {
                            case 400:
                                SnackBar snackbar = new SnackBar((Activity) context, "Wrong Username or Password");
                                snackbar.show();
                                break;
                            default:
                                SnackBar new_snackbar = new SnackBar((Activity) context, "Camsys server did not respond in time");
                                new_snackbar.show();
                        }
                        //Additional cases
                    } else {
                        SnackBar snackbar = new SnackBar((Activity) context, "No Internet Connection");
                        snackbar.show();
                    }
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Log.d(TAG, "" + error.getMessage() + "," + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("student_id", getStudentID(context));
                    params.put("camsys_password", camsys_password);

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/x-www-form-urlencoded");
                    headers.put("abc", "value");
                    return headers;
                }
            };
            sr.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    0,
                    0));
            queue.add(sr);
        }
    }
    public static boolean isExamInCal(Context context, String subject_name) {
        Cursor exam_cursor = MySingleton.getInstance(context).getDatabase().query(
                MMUContract.SubjectEntry.TABLE_NAME,
                new String[] {MMUContract.SubjectEntry.COLUMN_FINALS_START_DATETIME,
                        MMUContract.SubjectEntry.COLUMN_FINALS_END_DATETIME},
                MMUContract.SubjectEntry.COLUMN_NAME + " = ? ",
                new String[] {subject_name},
                null,
                null,
                null
        );
        if(exam_cursor.moveToFirst()) {
            Log.d("EXAM CURSOR", "EXISTS");
            long begin = exam_cursor.getLong(exam_cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_FINALS_START_DATETIME));
            long end = exam_cursor.getLong(exam_cursor.getColumnIndex(MMUContract.SubjectEntry.COLUMN_FINALS_END_DATETIME));
            String subject_exam = subject_name + " Finals";
            String[] proj =
                    new String[]{
                            CalendarContract.Instances._ID,
                            CalendarContract.Instances.BEGIN,
                            CalendarContract.Instances.END,
                            CalendarContract.Instances.EVENT_ID};
            Cursor cursor =
                    CalendarContract.Instances.query(context.getContentResolver(), null, begin, end);
            if (cursor.getCount() > 0) {
                Log.d("INSTANCE", "EXIST");
                return true;
            }else {
                Log.d("INSTANCE", "DOES NOT EXISTS");
                return false;
            }
        }
        Log.d("EXAM CURSOR", "DOES NOT EXISTS");
        return false;
    }
}

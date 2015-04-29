package com.github.hiiyl.mmuhub;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.github.hiiyl.mmuhub.data.MMUProvider;
import com.github.hiiyl.mmuhub.sync.MMUSyncAdapter;


public class SettingsActivity extends Activity {
    SharedPreferences.OnSharedPreferenceChangeListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d("ON CHANGE", "wow");
                if(key.equals("sync_enabled")) {
                    boolean sync_enabled = prefs.getBoolean("sync_enabled", true);
                    int sync_enabled_int = (sync_enabled) ? 1 : 0;
                    ContentResolver.setIsSyncable(MMUSyncAdapter.getSyncAccount(SettingsActivity.this), MMUProvider.getAuthority(), sync_enabled_int);
                }
                if(key.equals("sync_interval")) {
                    String  interval = prefs.getString("sync_interval", "");
                    int interval_in_seconds = Integer.parseInt(interval) * 60;
                    Log.d("SYNC SETTING","NEW INTERVAL IS " + interval);
                    MMUSyncAdapter.configurePeriodicSync(SettingsActivity.this, interval_in_seconds, interval_in_seconds/3);

                }
                // Implementation
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);

    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}

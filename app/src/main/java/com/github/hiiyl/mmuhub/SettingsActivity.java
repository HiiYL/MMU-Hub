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
    public final String SYNC_INTERVAL = "sync_interval";
    public final String SYNC_ENABLED = "sync_enabled";
    SharedPreferences.OnSharedPreferenceChangeListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key.equals(SYNC_ENABLED)) {
                    boolean sync_enabled = prefs.getBoolean(SYNC_ENABLED, true);
                    int sync_enabled_int = (sync_enabled) ? 1 : 0;
                    ContentResolver.setSyncAutomatically(MMUSyncAdapter.getSyncAccount(SettingsActivity.this), MMUProvider.getAuthority(), sync_enabled);
//                    ContentResolver.setIsSyncable(MMUSyncAdapter.getSyncAccount(SettingsActivity.this), MMUProvider.getAuthority(), sync_enabled_int);
                }
                if(key.equals(SYNC_INTERVAL)) {
                    String  interval = prefs.getString(SYNC_INTERVAL, "");
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

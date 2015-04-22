package com.github.hiiyl.mmuhub.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Hii on 4/22/15.
 */
public class MMUSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static MMUSyncAdapter sMMUSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sMMUSyncAdapter == null) {
                sMMUSyncAdapter = new MMUSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sMMUSyncAdapter.getSyncAdapterBinder();
    }
}

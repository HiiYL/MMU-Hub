package com.github.hiiyl.mmuhub.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Hii on 4/22/15.
 */
public class MMUAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private MMUAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new MMUAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
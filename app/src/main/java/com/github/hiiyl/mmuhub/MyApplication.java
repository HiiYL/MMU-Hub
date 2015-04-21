package com.github.hiiyl.mmuhub;

import android.app.Application;
import android.content.Context;

/**
 * Created by Hii on 4/21/15.
 */
public class MyApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}

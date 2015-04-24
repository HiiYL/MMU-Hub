package com.github.hiiyl.mmuhub;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.github.hiiyl.mmuhub.data.MMUDbHelper;
import com.github.hiiyl.mmuhub.helper.MainThreadBus;

/**
 * Created by Hii on 4/23/15.
 */
public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;
    private SQLiteDatabase database;
    private MainThreadBus mainThreadBus;
    private MMUDbHelper mMMUDbHelper;

    private MySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        mainThreadBus = getBus();
        mMMUDbHelper = getMMUDbHelper();
        database = getDatabase();



        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }

    public MMUDbHelper getMMUDbHelper() {
        if(mMMUDbHelper == null) {
            mMMUDbHelper = new MMUDbHelper(mCtx.getApplicationContext());
        }
        return mMMUDbHelper;
    }
    public SQLiteDatabase getDatabase() {
        if(database == null) {
            database = getMMUDbHelper().getWritableDatabase();
        }
        return database;
    }

    public MainThreadBus getBus() {
        if(mainThreadBus == null) {
            mainThreadBus = new MainThreadBus();
        }
        return mainThreadBus;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }
}

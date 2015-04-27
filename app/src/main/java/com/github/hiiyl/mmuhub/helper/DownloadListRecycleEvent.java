package com.github.hiiyl.mmuhub.helper;

import android.widget.ListView;

/**
 * Created by Hii on 4/26/15.
 */
public class DownloadListRecycleEvent {
    public final ListView download_list;

    public DownloadListRecycleEvent(ListView list) {
        this.download_list = list;
    }
}

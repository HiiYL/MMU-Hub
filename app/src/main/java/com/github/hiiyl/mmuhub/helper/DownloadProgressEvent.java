package com.github.hiiyl.mmuhub.helper;

/**
 * Created by Hii on 4/26/15.
 */
public class DownloadProgressEvent {
    public final int progress;
    public final int position;

    public DownloadProgressEvent(int position, int  progress) {
        this.position = position;
        this.progress = progress;
    }
}

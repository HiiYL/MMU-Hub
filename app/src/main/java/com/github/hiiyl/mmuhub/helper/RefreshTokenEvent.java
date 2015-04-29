package com.github.hiiyl.mmuhub.helper;

/**
 * Created by Hii on 4/28/15.
 */
public class RefreshTokenEvent {

    public String status;
    public RefreshTokenEvent(String message) {
        this.status = message;
    }
}

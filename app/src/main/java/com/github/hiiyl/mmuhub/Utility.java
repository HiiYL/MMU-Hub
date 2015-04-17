package com.github.hiiyl.mmuhub;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Hii on 4/13/15.
 */
public class Utility {
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
}

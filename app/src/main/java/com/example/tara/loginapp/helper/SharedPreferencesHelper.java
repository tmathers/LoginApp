package com.example.tara.loginapp.helper;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Manages profile data storage.
 *
 * @author tmath
 */

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "profile";

    /**
     * Sets a key/value pair in the Shared Preferences object.
     *
     * @param activity
     * @param key
     * @param value
     */
    public static void storeString(Activity activity, String key, String value) {


        // All objects are from android.context.Context
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);

        // Commit the edits
        editor.commit();

    }

    /**
     * Gets a value pair from the Shared Preferences object
     * @param activity
     * @param key
     * @return      The String value
     */
    public static String getString(Activity activity, String key) {

        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(key, null);

    }

}

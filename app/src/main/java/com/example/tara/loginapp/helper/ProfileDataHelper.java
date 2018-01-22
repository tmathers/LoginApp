package com.example.tara.loginapp.helper;

import android.app.Activity;

import com.example.tara.loginapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Gets data for the user's profile. Tries to get the data from local storage, and falls back
 * to google account. Assumes a user is signed in.
 *
 * @author tmath
 */

public class ProfileDataHelper {

    /**
     * Get first name
     * @param activity
     * @return      String
     */
    public static String getFirstName(Activity activity) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);

        return getField(activity, acct, activity.getString(R.string.first_name_key), acct.getGivenName());

    }

    /**
     * Get last name
     * @param activity
     * @return      String
     */
    public static String getLastName(Activity activity) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);

        return getField(activity, acct, activity.getString(R.string.last_name_key), acct.getFamilyName());

    }

    /**
     * Get email
     * @param activity
     * @return  String
     */
    public static String getEmail(Activity activity) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);

        return getField(activity, acct, activity.getString(R.string.email_key), acct.getEmail());

    }

    /**
     * Stores a field for the logged in user.
     * @param activity
     * @param fieldName
     * @param value
     */
    public static void storeField(Activity activity, String fieldName, String value) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(activity);

        SharedPreferencesHelper.storeString(activity, acct.getId() + "_" + fieldName, value);
    }

    /**
     * Private helper function to get the field value
     * @param activity
     * @param acct
     * @param fieldName
     * @param fallback
     * @return     String
     */
    private static String getField(Activity activity, GoogleSignInAccount acct, String fieldName, String fallback) {


        String acctId = acct.getId();

        String name = SharedPreferencesHelper.getString(activity, acctId + "_" + fieldName);

        if (name != null) {
            return name;
        }

        return fallback;

    }

}

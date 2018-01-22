package com.example.tara.loginapp.helper;

import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/**
 * Makes the GoogleSignInClient instance globally available.
 *
 * @author tmath
 */

public class GoogleSignInHelper {


    private static GoogleSignInClient mGoogleSignInClient = null;

    public static GoogleSignInClient getClient(AppCompatActivity activity) {

        if (mGoogleSignInClient == null) {

            synchronized(GoogleSignInHelper.class) {

                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                // Build a GoogleSignInClient with the options specified by gso.
                mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
            }

        }

        return mGoogleSignInClient;
    }

}

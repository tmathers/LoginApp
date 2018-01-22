package com.example.tara.loginapp;

import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


import com.example.tara.loginapp.helper.ProfileDataHelper;


/**
 * Profile Settings fragment
 *
 * @author tmath
 */
public class ProfileSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState,
                                    String rootKey) {

        setPreferencesFromResource(R.xml.pref_general, rootKey);

        setPreferenceValue(getString(R.string.first_name_key), ProfileDataHelper.getFirstName(this.getActivity()));
        setPreferenceValue(getString(R.string.last_name_key), ProfileDataHelper.getLastName(this.getActivity()));
        setPreferenceValue(getString(R.string.email_key), ProfileDataHelper.getEmail(this.getActivity()));

    }

    /**
     * Helper to set a preference summary and value from stored 'profile' SharedPreferences data
     * store.
     * @param key   The preference key
     */
    private void setPreferenceValue(String key, String value) {

        Preference pref = getPreferenceManager().findPreference(key);
        pref.setSummary(value);
        pref.setDefaultValue(value);
        ((EditTextPreference)pref).setText("");


        // set the change listener that will listen for changes
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.toString());

                // store it in profile data
                ProfileDataHelper.storeField(getActivity(), preference.getKey(), newValue.toString());

                return true;
            }
        });
    }
}

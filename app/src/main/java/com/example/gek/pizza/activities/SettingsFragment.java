package com.example.gek.pizza.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.gek.pizza.R;
import com.example.gek.pizza.data.Const;

import static com.example.gek.pizza.data.Const.db;

/**
 * Settings for SHOP user. Configure of global settings
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        fillSettings(Const.SETTINGS_PHONE_KEY);
        fillSettings(Const.SETTINGS_EMAIL_KEY);
        fillSettings(Const.SETTINGS_ADDRESS_KEY);
        fillSettings(Const.SETTINGS_LATITUDE_KEY);
        fillSettings(Const.SETTINGS_LONGITUDE_KEY);

    }

    public void fillSettings(String settingsKey){
        String textPhone = sharedPref.getString(settingsKey, "");;
        findPreference(settingsKey).setSummary(textPhone);
        findPreference(settingsKey).setDefaultValue(textPhone);
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        switch (key) {
            case Const.SETTINGS_PHONE_KEY:
                updateSummary(pref);
                break;
            case Const.SETTINGS_ADDRESS_KEY:
                updateSummary(pref);
                break;
            case Const.SETTINGS_EMAIL_KEY:
                updateSummary(pref);
                break;
            case Const.SETTINGS_LATITUDE_KEY:
                updateSummary(pref);
                break;
            case Const.SETTINGS_LONGITUDE_KEY:
                updateSummary(pref);
                break;
            default:
                break;
        }
    }

    public void updateSummary(Preference pref){
        EditTextPreference etTextSetting = (EditTextPreference) pref;
        String textPhone = etTextSetting.getText();
        pref.setSummary(textPhone);

        sendToServer(textPhone,pref);
    }

    // Save settings
    private void sendToServer(String settingsValue, Preference pref){
        db.child(Const.CHILD_SETTINGS).child(pref.getKey()).setValue(settingsValue);
    }

}

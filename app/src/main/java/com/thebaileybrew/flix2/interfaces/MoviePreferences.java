package com.thebaileybrew.flix2.interfaces;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.thebaileybrew.flix2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MoviePreferences extends AppCompatActivity {
    private static final String TAG = MoviePreferences.class.getSimpleName();
    private static String startDate;
    private static String endDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }


    public static class MoviePreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference clearSettings = findPreference("clear_settings");
            clearSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
                    prefsEditor.clear();
                    prefsEditor.apply();
                    return false;
                }
            });

            Preference sortOrder = findPreference(getString(R.string.preference_sort_key));
            bindPreferenceSummaryToValue(sortOrder);

            Preference sortLanguage = findPreference(getString(R.string.preference_sort_language_key));
            bindPreferenceSummaryToValue(sortLanguage);

            Preference yearFilter = findPreference(getString(R.string.preference_year_key));
            bindPreferenceSummaryToValue(yearFilter);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (stringValue.equals(String.valueOf(R.string.preference_sort_most_recent))) {
                Preference yearFilter = findPreference("primary_release_year");
                bindPreferenceSummaryToValue(yearFilter);
                yearFilter.setSummary(setMaxYear());
            }
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPreference = (EditTextPreference) preference;
                String preferenceString = editTextPreference.getText();
                preference.setSummary(preferenceString);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private String setMaxYear() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.US);
            Date curDate = Calendar.getInstance(Locale.US).getTime();
            return dateFormat.format(curDate);
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }
    }
}

/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Justin R. Hall
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.wkovacs64.nipthetip.ui.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;

import com.wkovacs64.nipthetip.App;
import com.wkovacs64.nipthetip.R;

import java.util.regex.Pattern;

import javax.inject.Inject;

import static butterknife.ButterKnife.findById;

public final class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Preference mTipPercentPref;
    private Preference mNumberOfPeoplePref;

    @Inject
    SharedPreferences sPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // Initialize Dagger dependency injections
        App.getAppComponent().inject(this);

        // Note: ButterKnife doesn't support Preference binding (yet)
        mTipPercentPref = findPreference(App.KEY_PREF_TIP_PERCENT);
        mNumberOfPeoplePref = findPreference(App.KEY_PREF_NUM_PEOPLE);
        // Set the preference summary to be the current value
        mTipPercentPref.setSummary(sPrefs.getString(App.KEY_PREF_TIP_PERCENT, null));
        mNumberOfPeoplePref.setSummary(sPrefs.getString(App.KEY_PREF_NUM_PEOPLE, null));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for successful preference changes
        sPrefs.registerOnSharedPreferenceChangeListener(this);

        // Validate user input, discarding changes if invalid
        final Activity activity = getActivity();
        mTipPercentPref.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean valid = Pattern.matches("^(?:100|[1-9]?[0-9])$", (String) newValue);
                        if (!valid) {
                            Snackbar.make(findById(activity, android.R.id.content),
                                    R.string.snack_tip_percent_invalid,
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        return valid;
                    }
                });
        mNumberOfPeoplePref.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean valid = Pattern.matches("^[1-9]\\d*$", (String) newValue);
                        if (!valid) {
                            Snackbar.make(findById(activity, android.R.id.content),
                                    R.string.snack_number_of_people_invalid,
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        return valid;
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        sPrefs.unregisterOnSharedPreferenceChangeListener(this);
        mTipPercentPref.setOnPreferenceChangeListener(null);
        mNumberOfPeoplePref.setOnPreferenceChangeListener(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Update the preference summary to reflect the new value
        switch (key) {
            case App.KEY_PREF_TIP_PERCENT:
                mTipPercentPref.setSummary(sPrefs.getString(App.KEY_PREF_TIP_PERCENT, null));
                break;
            case App.KEY_PREF_NUM_PEOPLE:
                mNumberOfPeoplePref.setSummary(sPrefs.getString(App.KEY_PREF_NUM_PEOPLE, null));
                break;
            default:
                break;
        }
    }
}

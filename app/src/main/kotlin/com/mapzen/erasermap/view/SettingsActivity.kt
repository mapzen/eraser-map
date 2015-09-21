package com.mapzen.erasermap.view

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import javax.inject.Inject

public class SettingsActivity : HomeAsUpActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
        var prefs: SharedPreferences? = null
            @Inject set

        override fun onCreate(savedInstanceState: Bundle?) {
            super<PreferenceFragment>.onCreate(savedInstanceState)
            (getActivity().getApplication() as EraserMapApplication).component()?.inject(this)
            addPreferencesFromResource(R.xml.preferences)
            initDistanceUnitsPref()
        }

        override fun onPreferenceChange(preference: Preference?, value: Any?): Boolean {
            if (preference is Preference && value is String) {
                if (getString(R.string.distance_units_key).equals(preference.getKey())) {
                    updateDistanceUnitsPref(preference, value)
                    return true
                }
            }

            return false
        }

        private fun initDistanceUnitsPref() {
            val key = getString(R.string.distance_units_key)
            val value = prefs?.getString(key, getString(R.string.distance_units_default))
                    ?: getString(R.string.distance_units_default)

            updateDistanceUnitsPref(findPreference(key), value)
            findPreference(key).setOnPreferenceChangeListener(this)
        }

        private fun updateDistanceUnitsPref(preference: Preference, value: String) {
            val index = getResources().getStringArray(R.array.distance_units_values).indexOf(value)
            val text = getResources().getStringArray(R.array.distance_units_entries)[index]
            preference.setSummary(text)
        }
    }
}

package com.mapzen.erasermap.view

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AndroidAppSettings
import com.mapzen.erasermap.model.AppSettings
import javax.inject.Inject

public class SettingsActivity : HomeAsUpActivity() {
    var settings: AppSettings? = null
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as EraserMapApplication).component()?.inject(this)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    inner class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            initDistanceUnitsPref()
        }

        override fun onPreferenceChange(preference: Preference?, value: Any?): Boolean {
            if (preference is Preference && value is String) {
                if (AndroidAppSettings.KEY_DISTANCE_UNITS.equals(preference.key)) {
                    updateDistanceUnitsPref(preference, value)
                    return true
                }
            }

            return false
        }

        private fun initDistanceUnitsPref() {
            val key = AndroidAppSettings.KEY_DISTANCE_UNITS
            val value = settings?.distanceUnits
            updateDistanceUnitsPref(findPreference(key), value.toString())
            findPreference(key).onPreferenceChangeListener = this
        }

        private fun updateDistanceUnitsPref(preference: Preference, value: String) {
            val index = resources.getStringArray(R.array.distance_units_values).indexOf(value)
            val text = resources.getStringArray(R.array.distance_units_entries)[index]
            preference.summary = text
        }
    }
}

package com.mapzen.erasermap.view

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AndroidAppSettings
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.util.StringConstants
import com.mapzen.tangram.DebugFlags
import com.mapzen.tangram.Tangram

public class SettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    var settings: AppSettings? = null

    companion object {
        @JvmStatic public fun newInstance(settings: AppSettings?): SettingsFragment {
            val fragment = SettingsFragment()
            fragment.settings = settings
            fragment.retainInstance = true
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        if (sharedPreferences.getBoolean(StringConstants.Settings.SHOW_DEBUG_SETTINGS, false)) {
            addPreferencesFromResource(R.xml.debug_preferences)
            initBuildNumberPref()
            initTileDebugPref()
            initLabelDebugPref()
            initTangramInfosDebugPref()
        }
        initDistanceUnitsPref()
        initEraseHistoryPref()
    }

    override fun onPreferenceChange(preference: Preference?, value: Any?): Boolean {
        if (preference is Preference && value is String) {
            if (AndroidAppSettings.KEY_DISTANCE_UNITS.equals(preference.key)) {
                updateDistanceUnitsPref(preference, value)
                return true
            }
        }
        if (preference is Preference && value is Boolean) {
            if (AndroidAppSettings.KEY_TILE_DEBUG_ENABLED.equals(preference.key)) {
                updateTileDebugPref(value)
                return true
            }
            if (AndroidAppSettings.KEY_LABEL_DEBUG_ENABLED.equals(preference.key)) {
                updateLabelDebugPref(value)
                return true
            }
            if (AndroidAppSettings.KEY_TANGRAM_INFOS_DEBUG_ENABLED.equals(preference.key)) {
                updateTangramInfosDebugPref(value)
                return true
            }
        }

        return false
    }

    override fun onPreferenceClick(p0: Preference?): Boolean {
        if(AndroidAppSettings.KEY_ERASE_HISTORY.equals(p0?.key)) {
            (getActivity() as SettingsActivity).clearHistory()
            return true
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

    private fun initTileDebugPref() {
        findPreference(AndroidAppSettings.KEY_TILE_DEBUG_ENABLED).onPreferenceChangeListener = this
        updateTileDebugPref(settings?.isTileDebugEnabled ?: false)
    }

    private fun updateTileDebugPref(value: Boolean) {
        Tangram.setDebugFlag(DebugFlags.TILE_BOUNDS, value)
        Tangram.setDebugFlag(DebugFlags.TILE_INFOS, value)
    }

    private fun initTangramInfosDebugPref() {
        findPreference(AndroidAppSettings.KEY_TANGRAM_INFOS_DEBUG_ENABLED)
                .onPreferenceChangeListener = this
        updateTangramInfosDebugPref(settings?.isTileDebugEnabled ?: false)
    }

    private fun updateTangramInfosDebugPref(value: Boolean) {
        Tangram.setDebugFlag(DebugFlags.TANGRAM_INFOS, value)
    }

    private fun initLabelDebugPref() {
        findPreference(AndroidAppSettings.KEY_LABEL_DEBUG_ENABLED).onPreferenceChangeListener = this
        updateLabelDebugPref(settings?.isLabelDebugEnabled ?: false)
    }

    private fun updateLabelDebugPref(value: Boolean) {
        Tangram.setDebugFlag(DebugFlags.LABELS, value)
    }

    private fun initBuildNumberPref() {
        findPreference(AndroidAppSettings.KEY_BUILD_NUMBER).summary = BuildConfig.BUILD_NUMBER
    }

    private fun initEraseHistoryPref() {
        findPreference(AndroidAppSettings.KEY_ERASE_HISTORY).onPreferenceClickListener = this
    }
}

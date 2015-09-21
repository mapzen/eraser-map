package com.mapzen.erasermap.view

import android.preference.Preference
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import com.mapzen.erasermap.view.SettingsActivity.SettingsFragment
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.setupActivity
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class SettingsActivityTest {
    val settingsActivity = setupActivity(javaClass<SettingsActivity>())
    val settingsFragment = settingsActivity.getFragmentManager()
            .findFragmentById(android.R.id.content) as SettingsFragment

    fun findPreference(keyId: Int): Preference = settingsFragment.findPreference(getString(keyId))

    fun getString(resId: Int): String = application.getString(resId)

    @Test fun shouldNotBeNull() {
        assertThat(settingsActivity).isNotNull()
        assertThat(settingsFragment).isNotNull()
    }

    @Test fun onCreate_shouldSetDistanceUnitsSummaryDefaultValue() {
        settingsFragment.onCreate(null)
        assertThat(findPreference(R.string.distance_units_key).getSummary()).isEqualTo("Miles")
    }

    @Test fun onCreate_shouldSetDistanceUnitsSummaryStoredValue() {
        settingsFragment.prefs
                ?.edit()
                ?.putString(getString(R.string.distance_units_key), "km")
                ?.commit()

        settingsFragment.onCreate(null)
        assertThat(findPreference(R.string.distance_units_key).getSummary()).isEqualTo("Kilometers")
    }

    @Test fun onPreferenceChange_shouldUpdateDistanceUnitsSummary() {
        settingsFragment.onPreferenceChange(findPreference(R.string.distance_units_key), "mi")
        assertThat(findPreference(R.string.distance_units_key).getSummary()).isEqualTo("Miles")

        settingsFragment.onPreferenceChange(findPreference(R.string.distance_units_key), "km")
        assertThat(findPreference(R.string.distance_units_key).getSummary()).isEqualTo("Kilometers")
    }
}

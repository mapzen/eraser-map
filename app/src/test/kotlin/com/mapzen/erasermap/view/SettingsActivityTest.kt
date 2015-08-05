package com.mapzen.erasermap.view

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class SettingsActivityTest {
    var settingsActivity = Robolectric.setupActivity(javaClass<SettingsActivity>())

    @Test
    public fun shouldNotBeNull() {
        assertThat(settingsActivity).isNotNull()
    }

    @Test
    public fun shouldHaveSettingsFragment() {
        assertThat(settingsActivity.getFragmentManager().findFragmentById(android.R.id.content))
                .isInstanceOf(javaClass<SettingsActivity.SettingsFragment>())
    }
}

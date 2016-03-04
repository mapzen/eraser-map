package com.mapzen.erasermap.view

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.controller.MainActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class InitActivityTest {
    val activity = Robolectric.setupActivity<InitActivity>(InitActivity::class.java)

    @Test
    public fun shouldNotBeNull() {
        assertThat(activity).isNotNull()
    }

    @Test
    public fun onCreate_shouldLaunchMainActivityAfterStartDelay() {
        ShadowLooper.idleMainLooper(InitActivity.START_DELAY_IN_MS)
        val intent = shadowOf(activity).nextStartedActivity
        assertThat(shadowOf(intent).intentClass).isEqualTo(MainActivity::class.java)
    }
}

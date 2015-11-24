package com.mapzen.erasermap.view

import android.content.Intent
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class InitActivityTest {
    val activity = Robolectric.setupActivity<InitActivity>(InitActivity::class.java)

    @Test
    public fun shouldNotBeNull() {
        assertThat(activity).isNotNull()
    }

    @Test
    public fun shouldReturnAppName() {
        assertThat(activity.getString(R.string.app_name)).isEqualTo("Eraser Map")
    }

    @Test
    public fun onApiKeyFetchComplete_shouldLaunchMainActivity() {
        activity.apiKeyFetchComplete = true
        activity.startMainActivity()
        var startedIntent: Intent = shadowOf(activity).nextStartedActivity;
            assertThat(startedIntent.getComponent().toString())
                    .isEqualTo("ComponentInfo{com.mapzen.erasermap/com.mapzen.erasermap.view.MainActivity}");
    }
}

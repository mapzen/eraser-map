package com.mapzen.erasermap.view

import android.app.ActionBar
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.setupActivity
import org.robolectric.annotation.Config
import android.app.ActionBar.DISPLAY_HOME_AS_UP
import com.mapzen.erasermap.R
import org.robolectric.fakes.RoboMenuItem

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class HomeAsUpActivityTest {
    var homeAsUpActivity = setupActivity(javaClass<HomeAsUpActivity>())

    @Test
    public fun shouldNotBeNull() {
        assertThat(homeAsUpActivity).isNotNull()
    }

    @Test
    public fun shouldDisplayHomeAsUpEnabled() {
        val displayOptions = homeAsUpActivity.getSupportActionBar().getDisplayOptions()
        assertThat(displayOptions and DISPLAY_HOME_AS_UP).isEqualTo(DISPLAY_HOME_AS_UP)
    }

    @Test
    public fun shouldFinishOnHomeSelected() {
        homeAsUpActivity.onOptionsItemSelected(RoboMenuItem(android.R.id.home))
        assertThat(homeAsUpActivity.isFinishing()).isTrue()
    }

    @Test
    public fun shouldNotFinishIfOtherOptionSelected() {
        homeAsUpActivity.onOptionsItemSelected(RoboMenuItem(R.id.action_clear))
        assertThat(homeAsUpActivity.isFinishing()).isFalse()
    }
}

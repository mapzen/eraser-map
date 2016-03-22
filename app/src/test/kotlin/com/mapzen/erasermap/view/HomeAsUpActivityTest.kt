package com.mapzen.erasermap.view

import android.app.ActionBar.DISPLAY_HOME_AS_UP
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.setupActivity
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
class HomeAsUpActivityTest {
    var homeAsUpActivity = setupActivity(HomeAsUpActivity::class.java)

    @Test fun shouldNotBeNull() {
        assertThat(homeAsUpActivity).isNotNull()
    }

    @Test fun shouldDisplayHomeAsUpEnabled() {
        val displayOptions = homeAsUpActivity.supportActionBar!!.displayOptions
        assertThat(displayOptions and DISPLAY_HOME_AS_UP).isEqualTo(DISPLAY_HOME_AS_UP)
    }

    @Test fun shouldFinishOnHomeSelected() {
        homeAsUpActivity.onOptionsItemSelected(RoboMenuItem(android.R.id.home))
        assertThat(homeAsUpActivity.isFinishing).isTrue()
    }

    @Test fun shouldNotFinishIfOtherOptionSelected() {
        homeAsUpActivity.onOptionsItemSelected(RoboMenuItem(R.id.action_settings))
        assertThat(homeAsUpActivity.isFinishing).isFalse()
    }
}

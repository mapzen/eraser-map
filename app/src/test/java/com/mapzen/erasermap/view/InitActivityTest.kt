package com.mapzen.erasermap.view

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.v7.widget.SearchView
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.shadows.ShadowMapData
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapData
import com.mapzen.tangram.MapView
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.internal.ShadowExtractor
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowApplication
import java.util.ArrayList

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class InitActivityTest {
    val activity = Robolectric.setupActivity<InitActivity>(javaClass<InitActivity>())

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
        var startedIntent: Intent = shadowOf(activity).getNextStartedActivity();
            assertThat(startedIntent.getComponent().toString())
                    .isEqualTo("ComponentInfo{com.mapzen.erasermap/com.mapzen.erasermap.view.MainActivity}");
      }
    }


@file:JvmName("MockLocationReceiver")

package com.mapzen.erasermap.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.AppSettings
import javax.inject.Inject

/**
 * Receiver for debug purposes. Update mock location from the command line using
 *
 * adb shell am broadcast -a com.mapzen.erasermap.SET_MOCK_LOCATION --es lat "40.6681024" --es lng "-73.9808297"
 */
public class MockLocationReceiver : BroadcastReceiver() {

    val TAG = MockLocationReceiver::class.java.simpleName

    @Inject lateinit var settings: AppSettings

    companion object {
        const val LAT = "lat"
        const val LNG = "lng"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val app = context?.applicationContext as EraserMapApplication
        app.component().inject(this)

        val lat = intent?.getStringExtra(LAT)
        val lng = intent?.getStringExtra(LNG)
        if (lat != null && lng != null) {
            val location = Location("mock")
            location.latitude = lat.toDouble()
            location.longitude = lng.toDouble()
            settings.mockLocation = location
            Log.d(TAG, "[mock location set] ($lat, $lng)")
        }
    }

}
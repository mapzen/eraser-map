package com.mapzen.erasermap.view

import android.os.Bundle
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.AppSettings
import javax.inject.Inject

public class SettingsActivity : HomeAsUpActivity() {
    var settings: AppSettings? = null
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as EraserMapApplication).component()?.inject(this)
        val fragment = SettingsFragment.newInstance(settings)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
    }
}

package com.mapzen.erasermap.view

import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.pelias.SavedSearch
import javax.inject.Inject

class SettingsActivity : HomeAsUpActivity() {
    @Inject lateinit var savedSearch: SavedSearch
    @Inject lateinit var settings: AppSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as EraserMapApplication).component()?.inject(this)
        val fragment = SettingsFragment.newInstance(settings)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()

    }

    fun clearHistory(title: String) {
        savedSearch.clear()
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SavedSearch.TAG, savedSearch.serialize())
                .commit()
        Toast.makeText(this@SettingsActivity, title, Toast.LENGTH_SHORT).show()
    }
}

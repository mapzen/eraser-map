package com.mapzen.erasermap.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.util.IntentFactory
import javax.inject.Inject

open class ViewAboutPreference(context: Context?, attrs: AttributeSet?) :
        ReadOnlyPreference(context, attrs) {

    companion object {
        public const val ABOUT_URL = "https://mapzen.com/erasermap/about"
    }

    @Inject lateinit var intentFactory: IntentFactory

    init {
        (context?.applicationContext as EraserMapApplication).component().inject(this)
    }

    override public fun onClick() {
        context.startActivity(intentFactory.newIntent(Intent.ACTION_VIEW, ABOUT_URL))
    }
}

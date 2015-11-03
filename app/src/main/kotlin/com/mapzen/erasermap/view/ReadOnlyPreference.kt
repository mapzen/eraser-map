package com.mapzen.erasermap.view

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet

open class ReadOnlyPreference(context: Context?, attrs: AttributeSet?) :
        EditTextPreference(context, attrs) {

    override fun onClick() {
        // Do nothing.
    }
}

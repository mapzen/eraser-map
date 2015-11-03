package com.mapzen.erasermap.view

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet

public class ReadOnlyEditTextPreference(context: Context?, attrs: AttributeSet?) :
        EditTextPreference(context, attrs) {

    override fun onClick() {
        // Do nothing.
    }
}

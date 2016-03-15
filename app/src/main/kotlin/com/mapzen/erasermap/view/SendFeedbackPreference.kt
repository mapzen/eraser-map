package com.mapzen.erasermap.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import com.mapzen.erasermap.BuildConfig

open class SendFeedbackPreference(context: Context?, attrs: AttributeSet?) :
        ReadOnlyPreference(context, attrs) {

    override fun onClick() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("android-support@mapzen.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Eraser Map Feedback")
        intent.putExtra(Intent.EXTRA_TEXT, getBody())
        context.startActivity(intent);
    }

    private fun getBody(): String {
        var body = "Build number: " + BuildConfig.BUILD_NUMBER + "\n\n"
        body += "How are things?\n\n\n"
        return body
    }
}

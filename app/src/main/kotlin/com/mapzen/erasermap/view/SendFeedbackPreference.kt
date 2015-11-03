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
        intent.putExtra(Intent.EXTRA_SUBJECT, "[EM Bug Report] Enter a short summary of the issue here")
        intent.putExtra(Intent.EXTRA_TEXT, getBody())
        context.startActivity(intent);
    }

    private fun getBody(): String {
        var body = "Manufacturer: " + Build.MANUFACTURER + "\n"
        body += "Model: " + Build.MODEL + "\n"
        body += "Release: " + Build.VERSION.RELEASE + "\n"
        body += "Code name: " + Build.VERSION.CODENAME + "\n"
        body += "SDK: " + Build.VERSION.SDK_INT + "\n"
        body += "Build number: " + BuildConfig.BUILD_NUMBER + "\n\n"
        body += "What did you expect to happen?\n\n\n"
        body += "What happened instead?\n\n\n"
        body += "Steps to reproduce:\n\n"
        return body
    }
}

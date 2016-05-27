package com.mapzen.erasermap.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity
import javax.inject.Inject

class InitActivity : AppCompatActivity() {
    companion object {
        @JvmStatic public val START_DELAY_IN_MS: Long = 1200
    }

    @Inject lateinit var crashReportService: CrashReportService

    lateinit var app: EraserMapApplication

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as EraserMapApplication
        app.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.splash_screen)
        supportActionBar?.hide()

        if (BuildConfig.DEBUG) {
            (findViewById(R.id.build_number) as TextView).text = BuildConfig.BUILD_NUMBER
        }

        Handler().postDelayed({ startMainActivityAndFinish() }, START_DELAY_IN_MS)

        val data = intent.data
        if (data != null) {
            Log.i("Eraser Map", "Incoming implicit intent: " + Uri.decode(data.toString()))
        }
    }

    private fun startMainActivityAndFinish() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.data = this.intent.data
        startActivity(intent)
        finish()
    }

    private fun initCrashReportService() {
        crashReportService.initAndStartSession(this)
    }
}

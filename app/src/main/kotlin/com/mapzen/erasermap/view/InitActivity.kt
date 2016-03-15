package com.mapzen.erasermap.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity
import com.mapzen.erasermap.model.ApiKeys
import com.mapzen.erasermap.model.SimpleCrypt
import javax.inject.Inject

public class InitActivity : AppCompatActivity() {
    companion object {
        @JvmStatic public val START_DELAY_IN_MS: Long = 1200
    }

    @Inject lateinit var crashReportService: CrashReportService
    @Inject lateinit var keys: ApiKeys

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as EraserMapApplication
        app.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.splash_screen)
        supportActionBar.hide()

        if (BuildConfig.DEBUG) {
            (findViewById(R.id.build_number) as TextView).text = BuildConfig.BUILD_NUMBER
        }

        if (keys.tilesKey.length > 0) {
            startMainActivityAndFinish()
        } else {
            Handler().postDelayed({ configureKeys() }, START_DELAY_IN_MS)
        }
    }

    private fun configureKeys() {
        if (BuildConfig.VECTOR_TILE_API_KEY == null ||
                BuildConfig.PELIAS_API_KEY == null ||
                BuildConfig.VALHALLA_API_KEY == null) {
            showApiKeyDialog()
        } else {
            if (BuildConfig.DEBUG) {
                keys.tilesKey = BuildConfig.VECTOR_TILE_API_KEY
                keys.searchKey = BuildConfig.PELIAS_API_KEY
                keys.routingKey = BuildConfig.VALHALLA_API_KEY
            } else {
                val crypt = SimpleCrypt(application)
                keys.tilesKey = crypt.decode(BuildConfig.VECTOR_TILE_API_KEY)
                keys.searchKey = crypt.decode(BuildConfig.PELIAS_API_KEY)
                keys.routingKey = crypt.decode(BuildConfig.VALHALLA_API_KEY)
            }
            startMainActivityAndFinish()
        }
    }

    private fun startMainActivityAndFinish() {
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    private fun initCrashReportService() {
        crashReportService.initAndStartSession(this)
    }

    private fun showApiKeyDialog() {
        var message = "The following API keys are not set: "
        if (BuildConfig.VECTOR_TILE_API_KEY == null) {
            message += "\n* VECTOR TILE"
        }
        if (BuildConfig.PELIAS_API_KEY == null) {
            message += "\n* PELIAS"
        }
        if (BuildConfig.VALHALLA_API_KEY == null) {
            message += "\n* VALHALLA"
        }

        AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Exit Application", { dialogInterface, i -> finish() })
                .show()
    }
}

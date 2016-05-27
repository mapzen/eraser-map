package com.mapzen.erasermap.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity
import com.mapzen.erasermap.model.ApiKeys
import com.mapzen.erasermap.model.SimpleCrypt
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

        val data = intent.data
        if (data != null) {
            Log.i("Eraser Map", "Incoming implicit intent: " + Uri.decode(data.toString()))
        }

        val decodeButton = findViewById(R.id.decode) as Button
        decodeButton.setOnClickListener() { configureKeys() }
    }

    private fun configureKeys() {
        var tilesKey: String? = null
        var searchKey: String? = null
        var routingKey: String? = null

        if (BuildConfig.VECTOR_TILE_API_KEY == null ||
                BuildConfig.PELIAS_API_KEY == null ||
                BuildConfig.VALHALLA_API_KEY == null) {
            showApiKeyDialog()
        } else {
            if (BuildConfig.DEBUG) {
                tilesKey = BuildConfig.VECTOR_TILE_API_KEY
                searchKey = BuildConfig.PELIAS_API_KEY
                routingKey = BuildConfig.VALHALLA_API_KEY
//                app.setApiKeys(ApiKeys(tilesKey, searchKey, routingKey))
            } else {
                val crypt = SimpleCrypt(application)
                tilesKey = crypt.decode(BuildConfig.VECTOR_TILE_API_KEY)
                searchKey = crypt.decode(BuildConfig.PELIAS_API_KEY)
                routingKey = crypt.decode(BuildConfig.VALHALLA_API_KEY)
//                app.setApiKeys(ApiKeys(tilesKey, searchKey, routingKey))
            }
        }

        System.out.println("[LEYNDO] tilesKey = " + tilesKey)
        System.out.println("[LEYNDO] searchKey = " + searchKey)
        System.out.println("[LEYNDO] routingKey = " + routingKey)
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

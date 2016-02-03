package com.mapzen.erasermap.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.erasermap.model.TileHttpHandler
import javax.inject.Inject

public class InitActivity : AppCompatActivity() {

    var crashReportService: CrashReportService? = null
        @Inject set
    var routeManager: RouteManager? = null
        @Inject set
    var tileHttpHandler: TileHttpHandler? = null
        @Inject set

    var app: EraserMapApplication? = null
    var apiKeyFetchComplete: Boolean = false;

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as EraserMapApplication
        app?.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.splash_screen)
        animate(0)
        (findViewById(R.id.build_number) as TextView).text = BuildConfig.BUILD_NUMBER
    }

    private fun animate(letterIndex: Int) {
        val imageRoots = arrayOf("e1","r1","a1","s1","e2","r2")
        if(letterIndex == 0) {
            var arrowAnimation: ObjectAnimator  = ObjectAnimator.ofFloat(findViewById(R.id.arrow),
                    "translationX", 0f, 900f).setDuration(1625)
            arrowAnimation.addListener( object: AnimatorListenerAdapter() {
                override fun onAnimationStart(anim: Animator) {
                    separationAnimation(imageRoots[letterIndex], letterIndex)
                }
            } )
            arrowAnimation.start()
        } else {
            separationAnimation(imageRoots[letterIndex], letterIndex)
        }
    }

    private fun separationAnimation(root: String, letterIndex: Int) {
        var top: Int = resources.getIdentifier(root+ "_top", "id", packageName)
        var bottom: Int = resources.getIdentifier(root+ "_bottom", "id", packageName)

        ObjectAnimator.ofFloat(findViewById(top), "translationY", 0f, -20f).setDuration(250).start()
        var bottomAnimation = ObjectAnimator.ofFloat(findViewById(bottom), "translationY", 0f, 20f)
                .setDuration(250)
        bottomAnimation.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator) {
                if(letterIndex < 5) {
                    animate(letterIndex + 1)
                }
                if(letterIndex == 5) {
                    startMainActivity()
                }
            }
        })
        bottomAnimation.start()
    }

    public fun startMainActivity() {
        if (BuildConfig.VECTOR_TILE_API_KEY == null ||
                BuildConfig.PELIAS_API_KEY == null ||
                BuildConfig.VALHALLA_API_KEY == null) {
            showApiKeyDialog()
        } else {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }

    private fun initCrashReportService() {
        crashReportService?.initAndStartSession(this)
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

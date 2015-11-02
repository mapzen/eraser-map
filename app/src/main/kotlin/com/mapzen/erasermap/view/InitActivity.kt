package com.mapzen.erasermap.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.erasermap.model.TileHttpHandler
import com.mapzen.leyndo.ManifestDownLoader
import com.mapzen.leyndo.ManifestModel
import javax.inject.Inject

public class InitActivity : AppCompatActivity() {

    var apiKeys: ManifestModel? = null
        @Inject set
    var crashReportService: CrashReportService? = null
        @Inject set
    var routeManager: RouteManager? = null
        @Inject set
    var tileHttpHandler: TileHttpHandler? = null
        @Inject set

    var app: EraserMapApplication? = null
    var tempKeys: ManifestModel? = null
    var manifestRequestCount: Int = 0
    var apiKeyFetchComplete: Boolean = false;

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as EraserMapApplication
        app?.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.splash_screen)
        getSupportActionBar().hide()
        tempKeys = ManifestModel()
        getApiKeys()
        animate(0)
        (findViewById(R.id.build_number) as TextView).text = BuildConfig.BUILD_NUMBER
    }

    private fun getApiKeys() {
        manifestRequestCount++;
        try {
            var dl: ManifestDownLoader = ManifestDownLoader()
            tempKeys = dl.getManifestModel( {
                checkForNullKeys()
            })
        } catch (e: UnsatisfiedLinkError) {
            checkForNullKeys()
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
                throw e;
            }
        }
    }

    public fun checkForNullKeys() {
        if(apiKeys?.valhallaApiKey.isNullOrEmpty() && manifestRequestCount < 2) {
            getApiKeys()
        } else if(apiKeys?.valhallaApiKey.isNullOrEmpty()
                .and(BuildConfig.VALHALLA_API_KEY.isNullOrEmpty())) {
            var builder: AlertDialog.Builder = AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.manifest_error))
                    .setNegativeButton(getString(R.string.decline_update),
                            DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                    .setCancelable(false)
            builder.create().show()
        } else {
            if (apiKeys?.valhallaApiKey == null) {
                apiKeys?.valhallaApiKey = BuildConfig.VALHALLA_API_KEY
            }
            if (apiKeys?.vectorTileApiKeyReleaseProp == null) {
                apiKeys?.vectorTileApiKeyReleaseProp = BuildConfig.VECTOR_TILE_API_KEY
            }
            routeManager?.apiKey = apiKeys?.valhallaApiKey
            tileHttpHandler?.apiKey = apiKeys?.vectorTileApiKeyReleaseProp
            apiKeyFetchComplete = true;
        }
    }


    private fun animate(letterIndex: Int) {
        val imageRoots = arrayOf("e1","r1","a1","s1","e2","r2")
        if(letterIndex == 0) {
            var arrowAnimation: ObjectAnimator  = ObjectAnimator.ofFloat(findViewById(R.id.arrow),
                    "translationX", 0f, 900f).setDuration(1625)
            arrowAnimation.addListener( object: AnimatorListenerAdapter() {
                override fun onAnimationStart(anim: Animator) {
                    seperationAnimation(imageRoots[letterIndex], letterIndex)
                }
            } )
            arrowAnimation.start()
        } else {
            seperationAnimation(imageRoots[letterIndex], letterIndex)
        }
    }

    private fun seperationAnimation(root: String, letterIndex: Int) {
        var top: Int = getResources().getIdentifier(root+ "_top",
                "id", getPackageName())
        var bottom: Int = getResources().getIdentifier(root+ "_bottom",
                "id", getPackageName())

        ObjectAnimator.ofFloat(findViewById(top), "translationY", 0f, -20f)
                .setDuration(250).start()
        var bottomAnimation: ObjectAnimator = ObjectAnimator
                .ofFloat(findViewById(bottom), "translationY", 0f, 20f).setDuration(250)
        bottomAnimation.addListener( object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator) {
                if(letterIndex < 5) {
                    animate(letterIndex + 1)
                }
                if(letterIndex == 5) {
                    startMainActivity()
                }
            }
        } )
        bottomAnimation.start()
    }

    public fun startMainActivity() {
        if(apiKeyFetchComplete) {
            apiKeys?.peliasApiKey = tempKeys?.peliasApiKey
            apiKeys?.valhallaApiKey = tempKeys?.valhallaApiKey
            apiKeys?.vectorTileApiKeyReleaseProp = tempKeys?.vectorTileApiKeyReleaseProp
            apiKeys?.minVersion = tempKeys?.minVersion

            var intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            android.os.SystemClock.sleep(500)
            startMainActivity()
        }
    }

    private fun initCrashReportService() {
        crashReportService?.initAndStartSession(this)
    }
}

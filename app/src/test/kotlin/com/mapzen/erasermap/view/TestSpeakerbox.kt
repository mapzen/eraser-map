@file:JvmName("TestSpeakerbox")
package com.mapzen.erasermap.view

import android.app.Activity

class TestSpeakerbox : Speaker {

    var finishOnSpeak: Boolean = false
    var errorOnSpeak: Boolean = false

    override fun play(text: String, onStart: Runnable, onDone: Runnable, onError: Runnable) {
        onStart?.run()
        if (finishOnSpeak) {
            onDone?.run()
        }
        if (errorOnSpeak) {
            onError?.run()
        }
    }

    override fun stop() {
    }

    override fun mute() {
    }

    override fun unmute() {
    }

    override fun isMuted(): Boolean {
        return false
    }

    override fun enableVolumeControl(activity: Activity) {
    }

    override fun disableVolumeControl(activity: Activity) {
    }

    override fun setQueueMode(queueMode: Int) {
    }

    override fun requestAudioFocus() {
    }

    override fun abandonAudioFocus() {
    }

    override fun shutdown() {
    }

}

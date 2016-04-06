@file:JvmName("SpeakerboxSpeaker")
package com.mapzen.erasermap.view

import android.app.Activity
import android.app.Application
import com.mapzen.speakerbox.Speakerbox

class SpeakerboxSpeaker(val application: Application) : Speaker {

    lateinit var speakerbox: Speakerbox;

    init {
        speakerbox = Speakerbox(application)
    }

    override fun play(text: String, onStart: Runnable, onDone: Runnable, onError: Runnable) {
        speakerbox.play(text, onStart, onDone, onError)
    }

    override fun stop() {
        speakerbox.stop()
    }

    override fun mute() {
        speakerbox.mute()
    }

    override fun unmute() {
        speakerbox.unmute()
    }

    override fun isMuted(): Boolean {
        return speakerbox.isMuted
    }

    override fun enableVolumeControl(activity: Activity) {
        speakerbox.enableVolumeControl(activity)
    }

    override fun disableVolumeControl(activity: Activity) {
        speakerbox.disableVolumeControl(activity)
    }

    override fun setQueueMode(queueMode: Int) {
        speakerbox.setQueueMode(queueMode)
    }

    override fun requestAudioFocus() {
        speakerbox.requestAudioFocus()
    }

    override fun abandonAudioFocus() {
        speakerbox.abandonAudioFocus()
    }

    override fun shutdown() {
        speakerbox.shutdown()
    }

}

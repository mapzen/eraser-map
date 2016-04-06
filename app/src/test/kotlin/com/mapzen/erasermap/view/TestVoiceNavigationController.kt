package com.mapzen.erasermap.view

import android.app.Activity


class TestVoiceNavigationController(activity: Activity, speakerbox: TestSpeakerbox) : VoiceNavigationController(activity, speakerbox) {

    var audioFocused: Boolean = false;
    var focusRequested: Boolean = false;

    override fun requestAudioFocus() {
        focusRequested = true
        audioFocused = true
    }

    override fun abandonAudioFocus() {
        focusRequested = true
        audioFocused = false
    }


}

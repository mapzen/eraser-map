package com.mapzen.erasermap.view

import android.app.Activity

interface Speaker {

    fun play(text: String, onStart: Runnable, onDone: Runnable, onError: Runnable);

    fun stop();

    fun mute();

    fun unmute();

    fun isMuted(): Boolean;

    fun enableVolumeControl(activity: Activity);

    fun disableVolumeControl(activity: Activity);

    fun setQueueMode(queueMode: Int);

    fun requestAudioFocus();

    fun abandonAudioFocus();

    fun shutdown();
}

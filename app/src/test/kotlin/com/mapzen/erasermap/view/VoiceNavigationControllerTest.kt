package com.mapzen.erasermap.view

import android.app.Activity
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.TestEraserMapApplication
import com.mapzen.erasermap.dummy.TestHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class VoiceNavigationControllerTest {

    val activity = Robolectric.buildActivity(Activity::class.java).create().start().resume().get()
    lateinit var voiceNavigationController: TestVoiceNavigationController
    lateinit var speaker: TestSpeakerbox

    @Before
    fun setup() {
        speaker = TestSpeakerbox()
        voiceNavigationController = TestVoiceNavigationController(activity, speaker);
    }

    @Test
    fun play_shouldDuckAudioOnStart() {
        val instruction = TestHelper.getTestInstruction()
        speaker.finishOnSpeak = true
        voiceNavigationController.playStart(instruction)
        assertThat(voiceNavigationController.focusRequested).isTrue()
    }

    @Test
    fun play_shouldReturnAudioLevelToNormalOnDone() {
        val instruction = TestHelper.getTestInstruction()
        speaker.finishOnSpeak = true
        speaker.errorOnSpeak = true
        voiceNavigationController.playStart(instruction)
        assertThat(voiceNavigationController.audioFocused).isFalse()
    }
}
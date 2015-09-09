package com.mapzen.erasermap.view

import android.app.Activity
import android.speech.tts.TextToSpeech
import com.mapzen.helpers.RouteEngine
import com.mapzen.speakerbox.Speakerbox
import com.mapzen.valhalla.Instruction

public class VoiceNavigationController(val activity: Activity) {
    private val speakerbox = Speakerbox(activity)

    init {
        speakerbox.setQueueMode(TextToSpeech.QUEUE_ADD)
    }

    public fun playMilestone(instruction: Instruction, milestone: RouteEngine.Milestone) {
        when (milestone) {
            RouteEngine.Milestone.ONE_MILE -> play("In one mile")
            RouteEngine.Milestone.QUARTER_MILE -> play("In a quarter mile")
        }

        play(instruction.getVerbalTransitionAlertInstruction())
    }

    public fun playPre(instruction: Instruction): Unit =
            play(instruction.getVerbalPreTransitionInstruction())

    public fun playPost(instruction: Instruction): Unit =
            play(instruction.getVerbalPostTransitionInstruction())

    private fun play(text: String): Unit = speakerbox.play(text)
}

package com.mapzen.erasermap.view

import android.app.Activity
import android.speech.tts.TextToSpeech
import com.mapzen.erasermap.R
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Router

open class VoiceNavigationController(val activity: Activity, val speakerbox: Speaker) {

    val requestFocusRunnable = Runnable { requestAudioFocus() }
    val abandonFocusRunnable = Runnable { abandonAudioFocus() }

    init {
        speakerbox.enableVolumeControl(activity)
        speakerbox.setQueueMode(TextToSpeech.QUEUE_ADD)
    }

    fun playStart(instruction: Instruction): Unit =
        play(instruction.getVerbalPreTransitionInstruction())

    fun playMilestone(instruction: Instruction,
            milestone: RouteEngine.Milestone,
            units: Router.DistanceUnits) {

        if (instruction.getVerbalTransitionAlertInstruction().length == 0) {
            return
        }
        when (units) {
            Router.DistanceUnits.MILES -> playMilestoneMiles(instruction, milestone)
            Router.DistanceUnits.KILOMETERS -> playMilestoneKilometers(instruction, milestone)
        }
    }

    private fun playMilestoneMiles(instruction: Instruction, milestone: RouteEngine.Milestone) {
        when (milestone) {
            RouteEngine.Milestone.TWO_MILE ->
                play(activity.getString(R.string.milestone_two_mile))
            RouteEngine.Milestone.ONE_MILE ->
                play(activity.getString(R.string.milestone_one_mile))
            RouteEngine.Milestone.QUARTER_MILE ->
                play(activity.getString(R.string.milestone_quarter_mile))
        }

        play(instruction.getVerbalTransitionAlertInstruction())
    }

    private fun playMilestoneKilometers(instruction: Instruction,
            milestone: RouteEngine.Milestone) {
        when (milestone) {
            RouteEngine.Milestone.TWO_MILE ->
                play(activity.getString(R.string.milestone_three_km))
            RouteEngine.Milestone.ONE_MILE ->
                play(activity.getString(R.string.milestone_one_and_a_half_km))
            RouteEngine.Milestone.QUARTER_MILE ->
                play(activity.getString(R.string.milestone_half_km))
        }

        play(instruction.getVerbalTransitionAlertInstruction())
    }

    fun stop() {
        speakerbox.stop()
    }

    fun playPre(instruction: Instruction): Unit =
            play(instruction.getVerbalPreTransitionInstruction())

    fun playPost(instruction: Instruction): Unit =
            play(instruction.getVerbalPostTransitionInstruction())

    private fun play(text: String) {
        speakerbox.play(text, requestFocusRunnable, abandonFocusRunnable, abandonFocusRunnable)
    }

    fun mute() {
        speakerbox.mute()
    }

    fun unmute() {
        speakerbox.unmute()
    }

    fun isMuted(): Boolean {
        return speakerbox.isMuted()
    }

    fun shutdown() {
        speakerbox.disableVolumeControl(activity)
        abandonAudioFocus()
    }

    open protected fun requestAudioFocus() {
        speakerbox.requestAudioFocus()
    }

    open protected fun abandonAudioFocus() {
        speakerbox.abandonAudioFocus()
    }

}

package com.mapzen.erasermap.view

import android.app.Activity
import android.speech.tts.TextToSpeech
import com.mapzen.erasermap.R
import com.mapzen.helpers.RouteEngine
import com.mapzen.speakerbox.Speakerbox
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Router

public class VoiceNavigationController(val activity: Activity) {
    private val speakerbox = Speakerbox(activity)

    init {
        speakerbox.setQueueMode(TextToSpeech.QUEUE_ADD)
    }

    public fun playStart(instruction: Instruction): Unit =
        play(instruction.getVerbalPreTransitionInstruction())

    public fun playRecalculating() {
        speakerbox.textToSpeech.stop()
        play(activity.getString(R.string.recalculating))
    }

    public fun playMilestone(instruction: Instruction,
            milestone: RouteEngine.Milestone,
            units: Router.DistanceUnits) {

        when (units) {
            Router.DistanceUnits.MILES -> playMilestoneMiles(instruction, milestone)
            Router.DistanceUnits.KILOMETERS -> playMilestoneKilometers(instruction, milestone)
        }
    }

    private fun playMilestoneMiles(instruction: Instruction, milestone: RouteEngine.Milestone) {
        when (milestone) {
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
            RouteEngine.Milestone.ONE_MILE ->
                play(activity.getString(R.string.milestone_one_and_a_half_km))
            RouteEngine.Milestone.QUARTER_MILE ->
                play(activity.getString(R.string.milestone_half_km))
        }

        play(instruction.getVerbalTransitionAlertInstruction())
    }

    public fun stop() {
        speakerbox.stop()
    }

    public fun playPre(instruction: Instruction): Unit =
            play(instruction.getVerbalPreTransitionInstruction())

    public fun playPost(instruction: Instruction): Unit =
            play(instruction.getVerbalPostTransitionInstruction())

    private fun play(text: String): Unit = speakerbox.play(text)

    public fun mute() {
        speakerbox.mute()
    }

    public fun unmute() {
        speakerbox.unmute()
    }

}

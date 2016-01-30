package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView
import com.mapzen.valhalla.Instruction
import java.util.ArrayList

public class DirectionListView(context: Context?, attrs: AttributeSet?) : ListView(context, attrs) {
    public fun setInstructions(instructions: List<Instruction>) {
        val strings = ArrayList<String>()
        val types = ArrayList<Int>()
        val distances = ArrayList<Int>()

        for (instruction in instructions) {
            val humanInstruction = instruction.getHumanTurnInstruction()
            strings.add(humanInstruction ?: "")
            types.add(instruction.turnInstruction)
            distances.add(instruction.distance)
        }

        adapter = DirectionListAdapter(context, strings, types, distances, false, false)
    }

    public fun setCurrent(index: Int) {
        if (adapter != null) {
            val directionListAdapter = adapter as DirectionListAdapter
            directionListAdapter.currentInstructionIndex = index
            directionListAdapter.notifyDataSetChanged()
        }
    }
}

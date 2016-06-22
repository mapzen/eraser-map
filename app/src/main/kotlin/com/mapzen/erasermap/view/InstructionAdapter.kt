package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.valhalla.Instruction
import java.util.ArrayList

class InstructionAdapter(private val context: Context,
        private val instructions: ArrayList<Instruction>) : PagerAdapter() {

    companion object {
        @JvmStatic val TAG = InstructionAdapter::class.java.simpleName
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val instruction = instructions.get(position)
        val view = View.inflate(context, R.layout.pager_item_instruction, null)
        val title = view.findViewById(R.id.instruction_text) as TextView
        val distance = view.findViewById(R.id.distance) as DistanceView
        val icon = view.findViewById(R.id.icon) as ImageView
        val turn = instruction.getIntegerInstruction()
        val iconId: Int = DisplayHelper.getRouteDrawable(context, turn, true)
        distance.realTime = true
        distance.distanceInMeters = instruction.distance
        title.text = instruction.getName()
        icon.setImageResource(iconId)
        setDistanceViewVisibility(distance, position)
        setTagId(view, position)
        container?.addView(view)
        return view
    }

    private fun setDistanceViewVisibility(distanceView: DistanceView, position: Int) {
        if (position == count - 1) {
            distanceView.visibility = View.GONE
        } else {
            distanceView.visibility = View.VISIBLE
        }
    }

    override fun getCount(): Int {
        return instructions.size
    }

    private fun setTagId(view: View, position: Int) {
        view.tag = RouteModeView.VIEW_TAG + position
    }

    fun setBackgroundColorActive(view: View?) {
        view?.setBackgroundColor(context.resources.getColor(android.R.color.white))
    }

    fun setBackgroundColorInactive(view: View?) {
        view?.setBackgroundColor(context.resources.getColor(R.color.light_gray))
    }

    fun setBackgroundColorArrived(view: View?) {
        view?.setBackgroundColor(context.resources.getColor(R.color.light_gray))
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }

    fun setBeginText(view: View?, instruction: Instruction) {
        val title = view?.findViewById(R.id.instruction_text) as TextView
        if (instruction.getBeginStreetNames().length > 0) {
            title.text = instruction.getBeginStreetNames()
        }
    }

    fun setPostText(view: View?, instruction: Instruction) {
        val title = view?.findViewById(R.id.instruction_text) as TextView
        title.text = instruction.getName()
    }
}

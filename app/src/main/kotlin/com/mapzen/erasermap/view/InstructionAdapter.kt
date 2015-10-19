package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.valhalla.Instruction
import java.util.*

public class InstructionAdapter(val context: Context, val instructions: ArrayList<Instruction>,
        val pager: RouteModeView) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val instruction = instructions.get(position)
        val view = View.inflate(context, R.layout.pager_item_instruction, null)
        val title = view.findViewById(R.id.instruction_text) as TextView
        val distance = view.findViewById(R.id.distance) as DistanceView
        val icon = view.findViewById(R.id.icon) as ImageView
        var iconId: Int = DisplayHelper.getRouteDrawable(context,
                instruction.getIntegerInstruction())
        distance.distanceInMeters = instruction.distance
        title.setText(instruction.getName())
        icon.setImageResource(iconId)
        if (position == 0) {
            view.findViewById(R.id.left_arrow).setVisibility(View.INVISIBLE)
        }
        if (position == getCount() - 1) {
            view.findViewById(R.id.right_arrow).setVisibility(View.INVISIBLE)
        }
        initArrowOnClickListeners(view, position)
        setTagId(view, position)
        container?.addView(view)
        return view
    }

    fun getView(): View {
        return this.getView()
    }

    override fun getCount(): Int {
        return instructions.size()
    }

    private fun setTagId(view: View, position: Int) {
        view.setTag(RouteModeView.VIEW_TAG + position)
    }

    private fun initArrowOnClickListeners(view: View, position: Int) {
        view.findViewById(R.id.right_arrow).setOnClickListener({
            pager.pageForward(position)
        })
        view.findViewById(R.id.left_arrow).setOnClickListener({
            pager.pageBackwards(position)
        })
    }

    public fun setBackgroundColorActive(view: View?) {
        view?.setBackgroundColor(context.getResources().getColor(R.color.transparent_white))
    }

    public fun setBackgroundColorInactive(view: View?) {
        view?.setBackgroundColor(context.getResources().getColor(R.color.transparent_light_gray))
    }

    public fun setBackgroundColorArrived(view: View?) {
        view?.setBackgroundColor(context.getResources().getColor(R.color.you_have_arrived))
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }
}

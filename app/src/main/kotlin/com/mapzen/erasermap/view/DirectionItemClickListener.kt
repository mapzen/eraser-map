package com.mapzen.erasermap.view

/**
 * Used by [DirectionListAdapter] when instantiating items
 */
interface DirectionItemClickListener {
    fun onDirectionItemClicked(position: Int)
}

package com.mapzen.privatemaps

import com.mapzen.pelias.gson.Feature

public trait ViewController {
    public fun showSearchResults(features: List<Feature>)
    public fun hideSearchResults()
}

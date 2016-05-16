package com.mapzen.erasermap.model

/**
 * Wrangle API keys for Mapzen services
 */
data class ApiKeys(
        val tilesKey: String,
        val searchKey: String,
        val routingKey: String) {

    init {
        if (tilesKey.isEmpty()) throw IllegalArgumentException("Tiles key cannot be empty.")
        if (searchKey.isEmpty()) throw IllegalArgumentException("Search key cannot be empty.")
        if (routingKey.isEmpty()) throw IllegalArgumentException("Routing key cannot be empty.")
    }
}

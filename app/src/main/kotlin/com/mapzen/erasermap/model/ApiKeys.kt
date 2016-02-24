package com.mapzen.erasermap.model

/**
 * Wrangle API keys for Mapzen services
 */
data class ApiKeys(
        public var tilesKey: String,
        public var searchKey: String,
        public var routingKey: String)

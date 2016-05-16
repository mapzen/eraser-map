package com.mapzen.erasermap.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApiKeysTest {
    val apiKeys = ApiKeys("vector-tiles-test", "search-test", "valhalla-test")

    @Test fun shouldNotBeNull() {
        assertThat(apiKeys).isNotNull()
    }

    @Test(expected = IllegalArgumentException::class) fun shouldNotAcceptEmptyTilesKey() {
        ApiKeys("", "search-test", "valhalla-test")
    }

    @Test(expected = IllegalArgumentException::class) fun shouldNotAcceptEmptySearchKey() {
        ApiKeys("vector-tiles-test", "", "valhalla-test")
    }

    @Test(expected = IllegalArgumentException::class) fun shouldNotAcceptEmptyRoutingKey() {
        ApiKeys("vector-tiles-test", "search-test", "")
    }
}

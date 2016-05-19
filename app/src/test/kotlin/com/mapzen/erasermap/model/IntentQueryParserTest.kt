package com.mapzen.erasermap.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IntentQueryParserTest {
    companion object {
        val TEST_QUERY = "q=350 5th Ave, New York, NY 10118"
        val TEST_QUERY_WITH_LATLNG = "q=350 5th Ave, New York, NY 10118&sll=40.7484,-73.9857"
        val TEST_QUERY_WITH_LATLNG_AND_RADIUS = "q=350 5th Ave, New York, NY 10118&sll=40.7484,-73.9857&radius=5"
        val MALFORMED_QUERY = "wtf=what a terrible failure"
    }

    val intentQueryParser = IntentQueryParser()

    @Test fun shouldNotBeNull() {
        assertThat(intentQueryParser).isNotNull()
    }

    @Test fun parse_shouldParseQueryString() {
        assertThat(intentQueryParser.parse(TEST_QUERY)?.queryString)
                .isEqualTo("350 5th Ave, New York, NY 10118")
    }

    @Test fun parse_shouldParseQueryStringWithFocusPoint() {
        assertThat(intentQueryParser.parse(TEST_QUERY_WITH_LATLNG)?.queryString)
                .isEqualTo("350 5th Ave, New York, NY 10118")
    }

    @Test fun parse_shouldParseQueryStringWithFocusPointAndRadius() {
        assertThat(intentQueryParser.parse(TEST_QUERY_WITH_LATLNG_AND_RADIUS)?.queryString)
                .isEqualTo("350 5th Ave, New York, NY 10118")
    }

    @Test fun parse_shouldReturnNullForMalformedQuery() {
        assertThat(intentQueryParser.parse(MALFORMED_QUERY)).isNull()
    }

    @Test fun parse_shouldParseFocusPoint() {
        val focusPoint = intentQueryParser.parse(TEST_QUERY_WITH_LATLNG)?.focusPoint
        assertThat(focusPoint?.latitude).isEqualTo(40.7484)
        assertThat(focusPoint?.longitude).isEqualTo(-73.9857)
    }
}

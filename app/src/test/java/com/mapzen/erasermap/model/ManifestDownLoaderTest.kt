package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.model.ManifestDownLoader

import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import com.squareup.okhttp.mockwebserver.RecordedRequest

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

import org.assertj.core.api.Assertions.assertThat
import java.util.concurrent.TimeUnit

RunWith(PrivateMapsTestRunner::class)
Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class ManifestDownLoaderTest {
    var downLoader: ManifestDownLoader? = null
    var server: MockWebServer? = null
    var sampleResponse: String = "{\r\n\"minVersion\": 1.1,\r\n\"VectorTileApiKeyReleaseProp\":" +
            " \"vectorkey\",\r\n\"valhallaApiKey\":" +
            " \"routekey\",\r\n\"mintApiKey\": \"mintkey\",\r\n\"peliasApiKey\":\"peliasKey\" \"\"\r\n}\r\n"

    Before
    throws(Exception::class)
    public fun setup() {
        downLoader = ManifestDownLoader()
        server = MockWebServer()
        server?.play()
       downLoader?.host = server?.getUrl("/").toString()

    }

    After
    throws(Exception::class)
    public fun teardown() {
        server?.shutdown()
    }

    Test
    throws(Exception::class)
    public fun  shouldRequestManifest() {
        server?.enqueue(MockResponse().setBody(sampleResponse))
        downLoader?.download(ManifestModel(), {})
        var request = server?.takeRequest(1000, TimeUnit.MILLISECONDS);
        assertThat(request?.getPath().toString()).isEqualTo("/erasermap_manifest")
    }

    Test
    throws(Exception::class)
    public fun  shouldSetManifestModelObject() {
        var keys: ManifestModel = ManifestModel()
        server?.enqueue(MockResponse().setBody(sampleResponse))
        downLoader?.download(keys, {})
        server?.takeRequest(1000, TimeUnit.MILLISECONDS);
        assertThat(keys.minVersion).isEqualTo(1.1)
        assertThat(keys.mintApiKey).isEqualTo("mintkey")
        assertThat(keys.valhallaApiKey).isEqualTo("routekey")
        assertThat(keys.vectorTileApiKeyReleaseProp).isEqualTo("vectorkey")
        assertThat(keys.peliasApiKey).isEqualTo("peliasKey")

    }
}
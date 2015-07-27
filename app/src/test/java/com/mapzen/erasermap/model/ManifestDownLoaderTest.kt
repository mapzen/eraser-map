package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

RunWith(PrivateMapsTestRunner::class)
Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class ManifestDownLoaderTest {
    var downLoader: ManifestDownLoader? = null
    var server: MockWebServer? = null
    var sampleResponse: String = "{\"minVersion\": 0.1,\r\n" +
            "    \"vectorTileApiKeyReleaseProp\": \"vectorKey\",\r\n " +
            "   \"valhallaApiKey\": \"routeKey\",\r\n    " +
            "\"peliasApiKey\": \"peliasKey\"}\r\n"

    @Before
    throws(Exception::class)
    public fun setup() {
        downLoader = ManifestDownLoader()
        server = MockWebServer()
        server?.play()
       downLoader?.host = server?.getUrl("/").toString()

    }

    @After
    throws(Exception::class)
    public fun teardown() {
        server?.shutdown()
    }

    @Test
    throws(Exception::class)
    public fun  shouldRequestManifest() {
        server?.enqueue(MockResponse().setBody(sampleResponse))
        downLoader?.download(ManifestModel(), {})
        var request = server?.takeRequest(1000, TimeUnit.MILLISECONDS);
        assertThat(request?.getPath().toString()).isEqualTo("/erasermap_manifest")
    }

    @Test
    throws(Exception::class)
    public fun  shouldSetManifestModelObject() {
        var keys: ManifestModel = ManifestModel()
        server?.enqueue(MockResponse().setBody(sampleResponse))
        downLoader?.download(keys, {
        })
        server?.takeRequest(1000, TimeUnit.MILLISECONDS);
        assertThat(keys.getValhallaApiKey()).isEqualTo("routeKey")
        assertThat(keys.getVectorTileApiKeyReleaseProp()).isEqualTo("vectorKey")
        assertThat(keys.getPeliasApiKey()).isEqualTo("peliasKey")
        assertThat(keys.getMinVersion()).isEqualTo(0.1)
    }
}


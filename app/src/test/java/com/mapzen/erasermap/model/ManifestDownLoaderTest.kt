package com.mapzen.erasermap.model

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyObject
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

RunWith(PrivateMapsTestRunner::class)
Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class ManifestDownLoaderTest {
    var downLoader: ManifestDownLoader? = null
    var mocks3Client: AmazonS3Client? = null;
    var sampleResponse: String = "{\"minVersion\": 0.1,\r\n" +
            "    \"vectorTileApiKeyReleaseProp\": \"vectorKey\",\r\n " +
            "   \"valhallaApiKey\": \"routeKey\",\r\n    " +
            "\"peliasApiKey\": \"peliasKey\"}\r\n"

    @Before
    throws(Exception::class)
    public fun setup() {
        downLoader = ManifestDownLoader()
        initMockS3Client()
        downLoader?.key = "fakeawskey"
        downLoader?.secret = "fakeawssecret"
        downLoader?.s3Client = mocks3Client
    }

    private fun initMockS3Client() {
        mocks3Client = mock(javaClass<AmazonS3Client>())
        var mockS3Object = mock(javaClass<S3Object>())
        `when`(mockS3Object.getObjectContent()).thenReturn(
                S3ObjectInputStream(ByteArrayInputStream(sampleResponse.getBytes()))
        )
        `when`(mocks3Client?.getObject(anyObject())).thenReturn(
                mockS3Object
        )
    }

    @Test
    public fun  shouldSetManifestModelObject() {
        var keys: ManifestModel = ManifestModel()
        downLoader?.download(keys, {})
        assertThat(keys.getValhallaApiKey()).isEqualTo("routeKey")
        assertThat(keys.getVectorTileApiKeyReleaseProp()).isEqualTo("vectorKey")
        assertThat(keys.getPeliasApiKey()).isEqualTo("peliasKey")
        assertThat(keys.getMinVersion()).isEqualTo(2)
    }
}

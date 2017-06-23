package com.mapzen.erasermap.model

import com.mapzen.android.core.MapzenManager
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@PrepareForTest(MapzenManager::class)
@RunWith(PowerMockRunner::class)
class ApiKeysTest {
    val application = EraserMapApplication()
    var apiKeys: ApiKeys? = null

    @Before fun setup() {
        PowerMockito.mockStatic(MapzenManager::class.java)
        PowerMockito.`when`(MapzenManager.instance(application)).thenReturn(Mockito.mock(
            MapzenManager::class.java))
        apiKeys = ApiKeys.Companion.sharedInstance(application)
    }

    @Test fun shouldNotBeNull() {
        assertThat(apiKeys).isNotNull()
    }

    @Test(expected = IllegalArgumentException::class) fun shouldNotAcceptEmptyApiKey() {
        PowerMockito.mockStatic(BuildConfig::class.java)
        Mockito.doReturn(null).`when`(BuildConfig.API_KEY)
        ApiKeys.Companion.sharedInstance(application)
    }
}

package com.mapzen.erasermap.view

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.dummy.TestAttributeSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment.application
import org.robolectric.annotation.Config

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class CompassViewTest {
    val compassView = CompassView(application, TestAttributeSet())

    @Test fun shouldNotBeNull() {
        assertThat(compassView).isNotNull()
    }

    @Test fun shouldInflateBackground() {
        assertThat(compassView.background).isNotNull()
    }

    @Test fun shouldInflateCompass() {
        assertThat(compassView.image).isNotNull()
    }
}

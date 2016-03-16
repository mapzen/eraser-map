package com.mapzen.erasermap.view

import android.content.Intent
import com.mapzen.erasermap.TestEraserMapApplication
import com.mapzen.erasermap.mock.MockContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ViewAboutPreferenceTest {
    @Mock lateinit private var viewAboutPref: ViewAboutPreference

    private val context = MockContext()

    @Before fun setUp() {
        val app = TestEraserMapApplication()
        app.onCreate()
        app.component().inject(viewAboutPref)
    }

    @Test fun shouldNotBeNull() {
        assertThat(viewAboutPref).isNotNull()
    }

    @Test fun onClick_shouldSendIntentToViewAboutPage() {
        Mockito.`when`(viewAboutPref.context).thenReturn(context)
        Mockito.`when`(viewAboutPref.onClick()).thenCallRealMethod()
        viewAboutPref.onClick()
        assertThat(context.startedActivity?.action).isEqualTo(Intent.ACTION_VIEW)
        assertThat(context.startedActivity?.data.toString())
                .isEqualTo(ViewAboutPreference.ABOUT_URL)
    }
}

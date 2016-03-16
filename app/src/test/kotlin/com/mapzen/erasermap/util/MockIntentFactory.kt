package com.mapzen.erasermap.util

import android.content.Intent
import android.net.Uri
import org.mockito.Mockito

class MockIntentFactory : IntentFactory() {
    override fun newIntent(action: String, data: String): Intent {
        val intent = Mockito.mock(Intent::class.java)
        val uri = Mockito.mock(Uri::class.java)

        Mockito.`when`(intent.action).thenReturn(action)
        Mockito.`when`(intent.data).thenReturn(uri)
        Mockito.`when`(uri.toString()).thenReturn(data)
        return intent
    }
}

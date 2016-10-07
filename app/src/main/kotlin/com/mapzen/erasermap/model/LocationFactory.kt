package com.mapzen.erasermap.model

import android.app.Application
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.android.lost.api.LostApiClient.ConnectionCallbacks

/**
 * Interface used by [LocationClientManager] implementations to create [LostApiClient]
 */
interface LocationFactory {
  fun createClient(application: Application, callbacks: ConnectionCallbacks): LostApiClient
}

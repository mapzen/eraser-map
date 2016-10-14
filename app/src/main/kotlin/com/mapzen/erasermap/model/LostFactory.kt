package com.mapzen.erasermap.model

import android.app.Application
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.android.lost.api.LostApiClient.ConnectionCallbacks

/**
 * Used by [LostClientManager] to create [LostApiClient]
 */
class LostFactory: LocationFactory {

  override fun createClient(application: Application, connectionCallbacks: ConnectionCallbacks):
      LostApiClient {
    return LostApiClient.Builder(application)
        .addConnectionCallbacks(connectionCallbacks)
        .build();
  }

}

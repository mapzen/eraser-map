package com.mapzen.erasermap.model

import android.app.Application
import com.mapzen.android.lost.api.LostApiClient
import java.util.ArrayList

/**
 * Manages [LostApiClient] and its [ConnectionCallback] flow
 */
class LostClientManager(val application: Application, locationFactory: LocationFactory):
    LocationClientManager {

  private var lostClient: LostApiClient
  private val connectionCallbacks: LostApiClient.ConnectionCallbacks =
      object: LostApiClient.ConnectionCallbacks {
        override fun onConnected() {
          for (runnable in runnables) {
            runnable.run()
          }
          runnables.clear()
        }

        override fun onConnectionSuspended() {
        }
  }
  private var runnables = ArrayList<Runnable>()

  init {
    lostClient = locationFactory.createClient(application, connectionCallbacks)
  }

  override fun getClient() = lostClient

  override fun connect()  = lostClient.connect()

  override fun disconnect() = lostClient.disconnect()

  override fun addRunnableToRunOnConnect(runnable: Runnable) {
    runnables.add(runnable)
  }
}

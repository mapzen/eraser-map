package com.mapzen.erasermap.model

import android.app.Application
import com.mapzen.android.lost.api.LostApiClient
import java.util.ArrayList

/**
 * Manages [LostApiClient] and its [ConnectionCallback] flow
 */
class LostClientManager(val application: Application, locationFactory: LocationFactory): LocationClientManager {

  private var connecting = false
  private var connected = false
  private lateinit var lostClient: LostApiClient
  private val connectionCallbacks: LostApiClient.ConnectionCallbacks =
      object: LostApiClient.ConnectionCallbacks {

        override fun onConnected() {
          connected = true
          connecting = false
          for (runnable in runnables) {
            runnable.run()
          }
          runnables.clear()
        }

        override fun onConnectionSuspended() {
          connected = false
          connecting = false
        }
  }
  private var runnables = ArrayList<Runnable>()

  init {
    lostClient = locationFactory.createClient(application, connectionCallbacks)
  }

  override fun getClient(): LostApiClient? {
    if (!connected) {
      return null
    }
    return lostClient
  }

  override fun connect() {
    if (connecting) {
      return
    }
    connecting = true
    lostClient.connect()
  }

  override fun addRunnableToRunOnConnect(runnable: Runnable) {
    runnables.add(runnable)
  }
}

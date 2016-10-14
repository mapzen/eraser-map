package com.mapzen.erasermap.model

import android.app.Application
import com.mapzen.android.lost.api.LostApiClient
import java.util.ArrayList

/**
 * Manages [LostApiClient] and its [ConnectionCallback] flow
 */
class LostClientManager(val application: Application, locationFactory: LocationFactory): LocationClientManager {

  private enum class State {
    CONNECTING, CONNECTED, IDLE
  }

  private var state = State.IDLE
  private lateinit var lostClient: LostApiClient
  private val connectionCallbacks: LostApiClient.ConnectionCallbacks =
      object: LostApiClient.ConnectionCallbacks {

        override fun onConnected() {
          state = State.CONNECTED
          for (runnable in runnables) {
            runnable.run()
          }
          runnables.clear()
        }

        override fun onConnectionSuspended() {
          state = State.IDLE
        }
  }
  private var runnables = ArrayList<Runnable>()

  init {
    lostClient = locationFactory.createClient(application, connectionCallbacks)
  }

  override fun getClient(): LostApiClient? {
    if (state != State.CONNECTED) {
      return null
    }
    return lostClient
  }

  override fun connect() {
    if (state == State.CONNECTING) {
      return
    }
    state = State.CONNECTING
    lostClient.connect()
  }

  override fun disconnect() {
    lostClient.disconnect()
    state = State.IDLE
  }

  override fun addRunnableToRunOnConnect(runnable: Runnable) {
    runnables.add(runnable)
  }
}

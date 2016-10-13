package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.LostApiClient

interface LocationClientManager {

  fun getClient(): LostApiClient?
  fun connect()
  fun disconnect()
  fun addRunnableToRunOnConnect(runnable: Runnable)
}

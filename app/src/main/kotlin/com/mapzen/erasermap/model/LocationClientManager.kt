package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.LostApiClient

interface LocationClientManager {

  fun getClient(): LostApiClient?
  fun connect()
  fun addRunnableToRunOnConnect(runnable: Runnable)
}

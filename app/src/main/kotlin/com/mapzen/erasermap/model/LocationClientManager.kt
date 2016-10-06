package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.LostApiClient

interface LocationClientManager {

  abstract fun getClient(): LostApiClient?
  abstract fun connect()
  abstract fun addRunnableToRunOnConnect(runnable: Runnable)
}
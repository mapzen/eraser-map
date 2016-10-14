package com.mapzen.erasermap.presenter

import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.model.LocationClientManager
import org.mockito.Mockito

class TestLostClientManager: LocationClientManager {

  override fun getClient(): LostApiClient? {
    return Mockito.mock(LostApiClient::class.java)
  }

  override fun connect() {

  }

  override fun disconnect() {

  }
  
  override fun addRunnableToRunOnConnect(runnable: Runnable) {

  }

}

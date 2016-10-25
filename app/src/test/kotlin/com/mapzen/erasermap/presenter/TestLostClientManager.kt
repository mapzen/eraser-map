package com.mapzen.erasermap.presenter

import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.model.LocationClientManager
import org.mockito.Mockito
import org.mockito.Mockito.`when`

class TestLostClientManager: LocationClientManager {

  override fun getClient(): LostApiClient {
    val client = Mockito.mock(LostApiClient::class.java)
    `when`(client.isConnected).thenReturn(true)
    return client
  }

  override fun connect() {
  }

  override fun disconnect() {
  }
  
  override fun addRunnableToRunOnConnect(runnable: Runnable) {
  }

}

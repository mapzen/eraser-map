package com.mapzen.erasermap.model

import android.app.Application
import com.mapzen.android.lost.api.LostApiClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class LostClientManagerTest {

  var clientManager: LostClientManager? = null
  val testLostFactory = TestLostFactory()

  @Before fun setup() {
    val application = Mockito.mock(Application::class.java)
    clientManager = LostClientManager(application, testLostFactory)
  }

  @Test fun getClient_shouldReturnNull() {
    assertThat(clientManager?.getClient()).isNull()
  }

  @Test fun getClient_shouldReturnClient() {
    testLostFactory.callbacks?.onConnected()
    assertThat(clientManager?.getClient()).isNotNull()
  }

  @Test fun connect_shouldInvokeClientConnect() {
    clientManager?.connect()
    assertThat(testLostFactory.lostClient.connected).isTrue()
  }

  @Test fun addRunnableToRunOnConnect_shouldRunRunnable() {
    val testRunnable = TestRunnable()
    clientManager?.addRunnableToRunOnConnect(
        testRunnable
    )
    testLostFactory.callbacks?.onConnected()
    assertThat(testRunnable.ran).isTrue()
  }

  class TestLostFactory: LocationFactory {

    var callbacks: LostApiClient.ConnectionCallbacks? = null
    var lostClient = TestLostClient()

    override fun createClient(application: Application,
        callbacks: LostApiClient.ConnectionCallbacks): LostApiClient {
      this.callbacks = callbacks
      lostClient.callbacks = callbacks
      return lostClient
    }

  }

  class TestLostClient: LostApiClient {

    var connected = false
    var callbacks: LostApiClient.ConnectionCallbacks? = null

    override fun disconnect() {
      connected = false
      callbacks?.onConnectionSuspended()
    }

    override fun connect() {
      connected = true
      callbacks?.onConnected()
    }

    override fun isConnected(): Boolean {
      return connected
    }

  }
  class TestRunnable: Runnable {

    var ran = false

    override fun run() {
      ran = true
    }

  }
}

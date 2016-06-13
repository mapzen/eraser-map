@file:JvmName("NotificationService")
package com.mapzen.erasermap.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * Stub service to facilitate killing notification when app is killed from app tray
 */
class NotificationService : Service() {

    internal var binder: IBinder = NotificationBinder(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    class NotificationBinder(val service: NotificationService) : Binder()
}

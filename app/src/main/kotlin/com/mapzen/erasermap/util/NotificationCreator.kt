package com.mapzen.erasermap.util

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.mapzen.erasermap.R
import com.mapzen.erasermap.service.NotificationService
import com.mapzen.erasermap.controller.MainActivity

class NotificationCreator(private val mainActivity: Activity) {
    private var builder: NotificationCompat.Builder? = null
    private var bigTextStyle: NotificationCompat.BigTextStyle? = null
    private var stackBuilder: TaskStackBuilder? = null
    private var notificationIntent: Intent? = null
    private var exitNavigationIntent: Intent? = null
    private var pendingNotificationIntent: PendingIntent? = null
    private var pendingExitNavigationIntent: PendingIntent? = null
    private val notificationManager: NotificationManager
    private val serviceConnection: NotificationServiceConnection
    private val preferences: SharedPreferences
    private val serviceIntent: Intent

    companion object {
        const val EXIT_NAVIGATION = "exit_navigation"
        const val NOTIFICATION_TAG_ROUTE = "route"
    }

    init {
        notificationManager = mainActivity.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
        serviceConnection = NotificationServiceConnection(mainActivity)
        preferences = PreferenceManager.getDefaultSharedPreferences(mainActivity)
        serviceIntent = Intent(mainActivity, NotificationService::class.java)
    }

    /**
     * All notifications created through this class should be killed using the
     * {@link NotificationCreator#killNotification()}, do not call
     * {@link NotificationManager#cancelAll()} directly
     *
     * Before we create a notification, we bind to a stub service so that when app is killed
     * {@link MainActivity#onDestroy} is reliably called. This triggers a
     * call to {@link NotificationCreator#killNotification} which removes notification from manager
     */
    fun createNewNotification(title: String, content: String) {
        initBuilder(title, content)
        initBigTextStyle(title, content)
        builder?.setStyle(bigTextStyle)
        initNotificationIntent()
        initExitNavigationIntent()
        initStackBuilder(notificationIntent)
        builder?.addAction(R.drawable.ic_dismiss, mainActivity.getString(R.string.exit_navigation),
                pendingExitNavigationIntent)
        builder?.setContentIntent(PendingIntent.getActivity(
                mainActivity.applicationContext, 0, notificationIntent, 0))
        mainActivity.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        notificationManager.notify(NOTIFICATION_TAG_ROUTE, 0, builder!!.build())
    }

    private fun initExitNavigationIntent() {
        exitNavigationIntent = Intent(mainActivity, NotificationBroadcastReceiver::class.java)
        exitNavigationIntent?.putExtra(EXIT_NAVIGATION, true)
        pendingExitNavigationIntent = PendingIntent.getBroadcast(
                mainActivity, 0, exitNavigationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun initNotificationIntent() {
        notificationIntent = Intent(mainActivity, MainActivity::class.java)
        notificationIntent?.setAction(Intent.ACTION_MAIN)
        notificationIntent?.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        pendingNotificationIntent = PendingIntent.getActivity(
                mainActivity, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun initStackBuilder(intent: Intent?) {
        stackBuilder = TaskStackBuilder.create(mainActivity)
        stackBuilder?.addParentStack(mainActivity.javaClass)
        stackBuilder?.addNextIntent(intent)
    }

    private fun initBigTextStyle(title: String, content: String) {
        bigTextStyle = NotificationCompat.BigTextStyle()
        bigTextStyle?.setBigContentTitle(title)
        bigTextStyle?.bigText(content)
    }

    private fun initBuilder(title: String, content: String) {
        builder = NotificationCompat.Builder(mainActivity.baseContext)
        builder?.setContentTitle(title)
        builder?.setContentText(content)
        builder?.setSmallIcon(R.drawable.ic_notif)
        builder?.setPriority(NotificationCompat.PRIORITY_MAX)
        builder?.setOngoing(true)
    }

    fun killNotification() {
        notificationManager.cancelAll()
        serviceConnection.service?.stopService(serviceIntent)
    }

    /**
     * In charge of starting the stub service we bind to
     */
    private class NotificationServiceConnection: ServiceConnection {

        val activity: Activity
        var service: NotificationService? = null

        constructor(activity: Activity) {
            this.activity = activity
        }

        override fun onServiceConnected(component: ComponentName?, inBinder: IBinder?) {
            if (inBinder == null) {
                return
            }
            val binder = inBinder as NotificationService.NotificationBinder
            val intent: Intent = Intent(activity, NotificationService::class.java)
            this.service = binder.service
            binder.service.startService(intent)
        }

        override fun onServiceDisconnected(component: ComponentName?) {
        }

    }
}

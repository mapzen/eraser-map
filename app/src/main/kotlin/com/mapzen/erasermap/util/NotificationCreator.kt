package com.mapzen.erasermap.util

import com.mapzen.erasermap.R

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.mapzen.erasermap.view.MainActivity

public class NotificationCreator(private val mainActivity: Activity) {
    private var builder: NotificationCompat.Builder? = null
    private var bigTextStyle: NotificationCompat.BigTextStyle? = null
    private var stackBuilder: TaskStackBuilder? = null
    private var notificationIntent: Intent? = null
    private var exitNavigationIntent: Intent? = null
    private var pendingNotificationIntent: PendingIntent? = null
    private var pendingExitNavigationIntent: PendingIntent? = null
    private val mNotificationManager: NotificationManager

    init {
        mNotificationManager = mainActivity.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun createNewNotification(title: String, content: String) {
        initBuilder(title, content)
        initBigTextStyle(title, content)
        builder?.setStyle(bigTextStyle)
        initNotificationIntent()
        initExitNavigationIntent()
        initStackBuilder(notificationIntent)
        builder?.addAction(R.drawable.ic_dismiss, "Exit Navigation", pendingExitNavigationIntent)
        builder?.setContentIntent(PendingIntent.getActivity(
                mainActivity.applicationContext, 0, notificationIntent, 0))
        mNotificationManager.notify("route", 0, builder!!.build())
    }

    private fun initExitNavigationIntent() {
        exitNavigationIntent = Intent(mainActivity, NotificationBroadcastReceiver::class.java)
        exitNavigationIntent?.putExtra(EXIT_NAVIGATION, true)
        pendingExitNavigationIntent = PendingIntent.getBroadcast(
                mainActivity, 0, exitNavigationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun initNotificationIntent() {
        notificationIntent = Intent(mainActivity, MainActivity  ::class.java)
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

    public fun killNotification() {
        val notificationManager = mainActivity.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    companion object {
        val EXIT_NAVIGATION = "exit_navigation"
    }
}

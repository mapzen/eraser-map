package com.mapzen.erasermap.util

import android.app.Activity
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mapzen.erasermap.view.MainActivity

public class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(NotificationCreator.EXIT_NAVIGATION, false)) {
            cancelAllNotifications(context)
            startBaseActivityWithExitExtra(context)
        }
    }

    private fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun startBaseActivityWithExitExtra(context: Context) {
        var exitRoutingIntent: Intent? = Intent(context, MainActivity::class.java)
        exitRoutingIntent?.putExtra(NotificationCreator.EXIT_NAVIGATION, true);
        exitRoutingIntent?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK );
        context.getApplicationContext().startActivity(exitRoutingIntent);
    }


}

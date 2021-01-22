package com.enecuum.wl.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.enecuum.wl.R
import com.enecuum.wl.utils.LocaleHelper
import com.enecuum.wl.vvm.host_main.MainActivity
import java.util.*

object ServiceNotification {

    const val NOTIFICATION_ID = 666013

    fun setUpChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Socket"
            val description = "Socket"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Socket", name, importance)
            channel.description = description
            val notificationManager =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun clear(context: Context?) {
        val mNotificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        mNotificationManager?.cancelAll()
    }

    fun build(service: DateService): Notification {
        val res = service.resources

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val conf = res.configuration
            conf.locale = Locale.forLanguageTag(LocaleHelper.getLocale()) // whatever you want here
            res.updateConfiguration(conf, null) // second arg null means don't change
        }

        return baseBuilder(service)
            .setContentTitle(res.getString(R.string.loading_balance))
            .setContentText(res.getString(R.string.connecting))
            .build()
    }

    fun update(service: DateService, text: String) {
        val pendingIntent = PendingIntent.getActivity(
            service,
            0,
            Intent(service, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val newNotification = baseBuilder(service)
            .setContentTitle(text)
            .setContentText(service.resources.getString(R.string.continue_mining))
            .setContentIntent(pendingIntent)
            .build()

        val mNotificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(NOTIFICATION_ID, newNotification)
    }

    private fun baseBuilder(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, "Socket")
            .setSmallIcon(R.drawable.notification_icon)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(
                R.drawable.notification_icon,
                context.resources.getString(R.string.stop_activity).capitalize(),
                stopIntent(context)
            )
            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
    }

    private fun stopIntent(context: Context): PendingIntent {
        val stopSelf = Intent(context, DateService::class.java)
        stopSelf.action = DateService.STOP_ACTION
        return PendingIntent.getService(context, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}
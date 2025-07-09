package com.theweek.plan.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.theweek.plan.MainActivity
import com.theweek.plan.R
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "the_week_plan_channel"
        private const val NOTIFICATION_ID = 1001
        private const val DAILY_REMINDER_REQUEST_CODE = 2001

        fun scheduleDailyReminder(context: Context) {
            // Check if notifications are enabled
            val sharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val notificationsEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
            
            if (!notificationsEnabled) {
                cancelDailyReminder(context)
                return
            }
            
            // Get reminder time
            val reminderHour = sharedPrefs.getInt("reminder_hour", 8)
            val reminderMinute = sharedPrefs.getInt("reminder_minute", 0)
            
            // Create calendar for reminder time
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderHour)
                set(Calendar.MINUTE, reminderMinute)
                set(Calendar.SECOND, 0)
            }
            
            // If the time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            // Create intent for the notification
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule the alarm
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        fun cancelDailyReminder(context: Context) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule notifications after device reboot
            scheduleDailyReminder(context)
        } else {
            // Show notification
            showTaskReminderNotification(context)
        }
    }

    private fun showTaskReminderNotification(context: Context) {
        // Create notification channel for Android O and above
        createNotificationChannel(context)
        
        // Create intent for when notification is tapped
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("The Week Plan")
            .setContentText("Don't forget to check your tasks for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val descriptionText = "Channel for daily task reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

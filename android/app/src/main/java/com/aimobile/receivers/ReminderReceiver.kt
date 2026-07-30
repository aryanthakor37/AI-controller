package com.aimobile.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aimobile.MainActivity
import com.aimobile.handlers.ReminderHandler

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Special Event Reminder"
        val contact = intent.getStringExtra("contact") ?: ""
        val repeat = intent.getStringExtra("repeat") ?: "NONE"
        val reminderId = intent.getIntExtra("reminderId", System.currentTimeMillis().toInt())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Birthdays & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled birthdays and special day events"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open App Intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            reminderId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🎉 $title")
            .setContentText(if (contact.isNotEmpty()) "Special day for $contact!" else "It's time for your scheduled reminder!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)

        // Action 1: Call Contact
        if (contact.isNotEmpty()) {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$contact")
            }
            val callPendingIntent = PendingIntent.getActivity(
                context,
                reminderId + 1,
                callIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_call, "Call", callPendingIntent)
        }

        // Action 2: Send SMS
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(if (contact.isNotEmpty()) "smsto:$contact" else "smsto:")
            putExtra("sms_body", "Happy Birthday! Wishing you a wonderful day! 🎉🎂")
        }
        val smsPendingIntent = PendingIntent.getActivity(
            context,
            reminderId + 2,
            smsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_menu_send, "Send SMS", smsPendingIntent)

        // Action 3: Send WhatsApp
        val waIntent = Intent(Intent.ACTION_VIEW).apply {
            val encodedMsg = Uri.encode("Happy Birthday! Wishing you a wonderful day! 🎉🎂")
            data = Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
        }
        val waPendingIntent = PendingIntent.getActivity(
            context,
            reminderId + 3,
            waIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(android.R.drawable.ic_menu_share, "WhatsApp", waPendingIntent)

        notificationManager.notify(reminderId, builder.build())

        // If YEARLY recurring (e.g. Birthday), reschedule for next year
        if (repeat == "YEARLY") {
            ReminderHandler(context).rescheduleYearly(reminderId, title, contact)
        }
    }
}

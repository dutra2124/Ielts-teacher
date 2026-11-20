package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.IeltsModule
import com.example.data.ReminderTone
import com.example.data.StudyTab

object ApexNotificationHelper {

    const val CHANNEL_ID = "apex_ielts_daily_reminders"
    const val EXTRA_TARGET_TAB_INDEX = "extra_target_tab_index"
    private const val NOTIFICATION_ID_BASE = 8500

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc học IELTS hằng ngày",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Thông báo khung giờ học từ vựng và luyện đề Band 8.5 từ trợ giảng AI."
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun buildReminderContent(
        tone: ReminderTone,
        slotLabelVi: String,
        activeModule: IeltsModule
    ): Pair<String, String> {
        val sampleCollocation = activeModule.keyCollocations.firstOrNull() ?: "academic precision"
        return when (tone) {
            ReminderTone.GENTLE -> {
                val title = "Cô Minh Anh AI • $slotLabelVi"
                val body = "Chào bạn! Dành vài phút ôn chủ đề \"${activeModule.titleEn}\" và cụm \"$sampleCollocation\" để giữ chuỗi học nhé!"
                title to body
            }
            ReminderTone.STRICT -> {
                val title = "Kỷ luật Band 8.5 • $slotLabelVi"
                val body = "Đến giờ học rồi! Vào ngay \"${activeModule.titleEn}\" — thành thạo \"$sampleCollocation\" hôm nay, không trì hoãn!"
                title to body
            }
        }
    }

    fun postStudyReminderNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIFICATION_ID_BASE
    ): Boolean {
        ensureNotificationChannel(context)
        if (!hasNotificationPermission(context)) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TAB_INDEX, StudyTab.LEARN.index)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}

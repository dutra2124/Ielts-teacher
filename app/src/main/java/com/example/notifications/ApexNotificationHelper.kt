package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.IeltsModule
import com.example.data.NotificationSoundPreset
import com.example.data.ReminderTone
import com.example.data.StudyTab

object ApexNotificationHelper {

    private const val LEGACY_CHANNEL_ID = "apex_ielts_daily_reminders"
    private const val CHANNEL_PREFIX = "master_ielts_study_"
    const val EXTRA_TARGET_TAB_INDEX = "extra_target_tab_index"
    private const val NOTIFICATION_ID_BASE = 8500

    /**
     * Generates a deterministic channel ID incorporating sound enabled/muted state and sound URI hash.
     * Required on Android 8.0+ (API 26+) because channel sound settings are immutable once created.
     */
    fun channelIdFor(soundEnabled: Boolean, soundUriString: String?): String {
        val normalizedKey = soundUriString?.takeIf { it.isNotBlank() } ?: "default"
        return "${CHANNEL_PREFIX}${soundEnabled}_${normalizedKey.hashCode()}"
    }

    /**
     * Resolves a persisted URI string or built-in preset identifier to a playable Android [Uri].
     */
    fun resolveSoundUri(context: Context, soundUriString: String?): Uri? {
        val defaultNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (soundUriString.isNullOrBlank()) {
            return defaultNotification
        }

        return when (soundUriString) {
            NotificationSoundPreset.GENTLE_REMINDER.storedUri -> {
                findGentleNotificationUri(context) ?: defaultNotification
            }
            NotificationSoundPreset.ALARM_CHIME.storedUri -> {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: defaultNotification
            }
            else -> runCatching { Uri.parse(soundUriString) }.getOrNull() ?: defaultNotification
        }
    }

    private fun findGentleNotificationUri(context: Context): Uri? {
        return runCatching {
            val manager = RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_NOTIFICATION)
            }
            val cursor = manager.cursor
            var selected: Uri? = null
            if (cursor != null) {
                if (cursor.count > 1) {
                    // Pick a distinct secondary notification tone when available for gentle reminders
                    selected = manager.getRingtoneUri(1)
                } else if (cursor.count == 1) {
                    selected = manager.getRingtoneUri(0)
                }
                cursor.close()
            }
            selected ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
        }.getOrNull()
    }

    /**
     * Resolves a human-readable ringtone/notification tone title via Android [RingtoneManager].
     */
    fun resolveSoundTitle(context: Context, uri: Uri?): String {
        if (uri == null) return NotificationSoundPreset.DEFAULT_NOTIFICATION.titleVi
        val title = runCatching {
            RingtoneManager.getRingtone(context, uri)?.getTitle(context)
        }.getOrNull()
        return title?.trim()?.takeIf { it.isNotBlank() } ?: "Âm thanh tùy chỉnh"
    }

    /**
     * Ensures the active Android 8.0+ [NotificationChannel] matches the user's current sound toggle
     * and custom tone URI, deleting stale dynamic channels so the OS Settings stay clean.
     */
    fun ensureNotificationChannel(
        context: Context,
        soundEnabled: Boolean = true,
        soundUriString: String? = null
    ): String {
        val targetChannelId = channelIdFor(soundEnabled, soundUriString)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return targetChannelId

            // Remove legacy static channel and outdated sound-config channels
            runCatching {
                manager.notificationChannels?.forEach { existing ->
                    val id = existing.id
                    if ((id.startsWith(CHANNEL_PREFIX) && id != targetChannelId) || id == LEGACY_CHANNEL_ID) {
                        manager.deleteNotificationChannel(id)
                    }
                }
            }

            if (manager.getNotificationChannel(targetChannelId) == null) {
                val channelName = if (soundEnabled) {
                    "Nhắc học Master Ielts (Có âm thanh)"
                } else {
                    "Nhắc học Master Ielts (Im lặng)"
                }
                val channel = NotificationChannel(
                    targetChannelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Thông báo lịch học từ vựng và luyện đề Band 8.5 từ Master Ielts."
                    if (soundEnabled) {
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        setSound(resolveSoundUri(context, soundUriString), audioAttributes)
                        enableVibration(true)
                    } else {
                        setSound(null, null)
                        enableVibration(false)
                    }
                }
                manager.createNotificationChannel(channel)
            }
        }
        return targetChannelId
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
                val title = "Master Ielts • Cô Minh Anh AI ($slotLabelVi)"
                val body = "Chào bạn! Dành vài phút ôn chủ đề \"${activeModule.titleEn}\" và cụm \"$sampleCollocation\" để giữ chuỗi học nhé!"
                title to body
            }
            ReminderTone.STRICT -> {
                val title = "Master Ielts • Kỷ luật Band 8.5 ($slotLabelVi)"
                val body = "Đến giờ học rồi! Vào ngay \"${activeModule.titleEn}\" — thành thạo \"$sampleCollocation\" hôm nay, không trì hoãn!"
                title to body
            }
        }
    }

    fun postStudyReminderNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = NOTIFICATION_ID_BASE,
        soundEnabled: Boolean = true,
        soundUriString: String? = null
    ): Boolean {
        val activeChannelId = ensureNotificationChannel(
            context = context,
            soundEnabled = soundEnabled,
            soundUriString = soundUriString
        )
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

        val builder = NotificationCompat.Builder(context, activeChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (soundEnabled) {
            builder.setSound(resolveSoundUri(context, soundUriString))
        } else {
            builder.setSilent(true)
            builder.setSound(null)
        }

        val notification = builder.build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}

package com.ailisi.jinianri.domain

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ailisi.jinianri.JinianriApp
import com.ailisi.jinianri.MainActivity
import com.ailisi.jinianri.R
import com.ailisi.jinianri.data.JinianriDatabase
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * 每天检查一次所有设置了提前提醒的事件，
 * 当「距离下一次发生日 = 提醒天数」时发通知。
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = JinianriDatabase.get(applicationContext)
        val today = LocalDate.now()
        val events = db.events().observeAll().first()
        for (event in events) {
            val remind = event.remindDaysBefore ?: continue
            val timing = DateEngine.timingOf(
                dateEpochDay = event.dateEpochDay,
                repeatYearly = event.repeatYearly,
                isLunar = event.isLunar,
                lunarMonth = event.lunarMonth,
                lunarDay = event.lunarDay,
                today = today,
            )
            if (timing.daysUntil == remind.toLong()) {
                notify(event.id, event.title, timing.daysUntil)
            }
        }
        return Result.success()
    }

    private fun notify(eventId: Long, title: String, daysUntil: Long) {
        val context = applicationContext
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("eventId", eventId)
        }
        val pending = PendingIntent.getActivity(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = if (daysUntil == 0L) "就是今天" else "还有 $daysUntil 天"
        val notification = NotificationCompat.Builder(context, JinianriApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(eventId.toInt(), notification)
        } catch (e: SecurityException) {
            // 通知权限未授予时静默跳过
        }
    }

    companion object {
        private const val WORK_NAME = "jinianri_reminder_check"

        /** 每天 09:00 检查（WorkManager 不保证精确时间，误差通常几分钟内） */
        fun schedule(context: Context) {
            val now = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 9)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
            }
            if (target.before(now)) target.add(java.util.Calendar.DAY_OF_YEAR, 1)
            val initialDelay = target.timeInMillis - now.timeInMillis

            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }
    }
}

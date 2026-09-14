package com.ailisi.jinianri

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.ailisi.jinianri.data.JinianriDatabase
import com.ailisi.jinianri.data.JinianriRepository
import com.ailisi.jinianri.domain.ReminderWorker

class JinianriApp : Application() {
    /** 全局依赖容器 */
    lateinit var repository: JinianriRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = JinianriRepository(JinianriDatabase.get(this))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "纪念日提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "纪念日与生日的提前提醒"
            }
            manager.createNotificationChannel(channel)
        }
        ReminderWorker.schedule(this)
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "jinianri_reminders"
    }
}

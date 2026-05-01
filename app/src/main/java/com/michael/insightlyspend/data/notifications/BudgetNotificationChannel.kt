package com.michael.insightlyspend.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetNotificationChannel @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureCreated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        createChannelOreo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannelOreo() {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Budget thresholds",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Fires when a category crosses ~80% of its monthly budget."
        }
        mgr.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "insightly_budget_alerts"
        const val NOTIFICATION_GROUP = "insightly_budget_group"
    }
}
